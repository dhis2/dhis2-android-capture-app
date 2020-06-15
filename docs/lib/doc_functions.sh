
#
# Requirements:
#
# ensure python3 is available:
if [[ ! $(command -v python3) ]]; then
  echo "This script requires python3, and some other libraries:"
  echo "  brew install python3 cairo pango gdk-pixbuf libffi"
  echo "Exiting."
  exit 0
fi
#
# If on mac, ensure we have gnu coreutils for command compatibility
if [[ "$OSTYPE" == "darwin"* ]]; then
  if [[ ! $(command -v gdate) ]] || [[ ! $(command -v gsed) ]]; then
    echo "This script requires coreutils and gnu-sed to be installed on Mac:"
    echo "  brew install coreutils gnu-sed"
    echo "Exiting."
    exit 0
  fi
  # once gnu coreutils and gnu-sed are installed, we can ensure they are
  # first in the path, for convenience
  corepath="/usr/local/opt/coreutils/libexec/gnubin"
  sedpath="/usr/local/opt/gnu-sed/libexec/gnubin"
  export PATH="$sedpath:$corepath:$PATH"
fi
#
# ensure language packs are installed. e.g. for French
#> sudo apt-get install language-pack-fr
#

readonly GIT_BASE="https://github.com/dhis2/"

# set the default locale for the build
export LC_ALL=en_GB.UTF-8
export LANG=en_GB.UTF-8

# pushing docs to localisation platform (transifex) is only done on Jenkins
LOCALISE=0
if [[ `id -un` == "jenkins" ]]; then
  # and only where configured
  if [ -f ~/.transifexrc ]; then LOCALISE=1; fi
fi

# set up the python environment
VENV_VERSION=1.2  # used to force a reset of the venv
cd ${SCRIPT_DIR}
if [ ! -f "venv/${VENV_VERSION}" ]; then
    rm -rf venv
    source venv_setup
    touch "venv/${VENV_VERSION}"
fi
source ./venv/bin/activate

# script variables
src="${SCRIPT_DIR}/src/commonmark/en"
TMPBASE="${SCRIPT_DIR}/tmp"
tmp="${TMPBASE}/en"
localisation_root="${SCRIPT_DIR}/target/commonmark"

# clear the output directories
rm -rf $TMPBASE
mkdir -p $TMPBASE
rm -rf $localisation_root
mkdir -p $localisation_root

# set up mkdocs
mkdir $tmp
cp -a $src/resources/mkdocs/* $tmp/
myml=$tmp/mkdocs.yml

# generate function called for each document
generate(){
    name=$1
    subdir=$2
    selection=$3
    if [ ! $selection ]
    then
      selection="both"
    fi
    lang=en
    locale=en_GB.UTF-8

    echo "+--------------------------------------------------------"
    echo "| Processing: $name"
    echo "+--------------------------------------------------------"

    assemble $name
    # go to the temp directory and build the documents - put output in target directory
    build_docs $name $subdir $selection $lang $locale

    # update transifex from latest source files
    update_localizations $name

}

# translate function called for each document
translate(){
    name=$1
    subdir=$2
    selection=$3
    lang=$4
    locale=$5

    if [ ! $selection ]
    then
      selection="both"
    fi

    echo "+--------------------------------------------------------"
    echo "| Processing: $name"
    echo "+--------------------------------------------------------"

    # we need to assemble the source documents to consolidate the resources
    assemble $name

    pull_translations $name
    # build localised versions
    build_docs $name $subdir $selection $lang $locale
}


shared_resources() {
    mkdir -p $1/resources/
    cp -r $src/resources/* $1/resources/
}

include_submodules() {

  DONE=false
  until $DONE
  do {
      read line || DONE=true
      # echo "line $line"

      read -r -a words <<< $line
      # echo "words0 ${words[0]}"
      if [ "${words[0]}" == "!SUBMODULE" ]
      then
          submodule_name=${words[1]}
          submodule_branch=${words[2]}
          submodule_file=${words[3]}
          {
            mkdir -p $src/content/submodules
            pushd $src/content/submodules
            if [[ "${submodule_name//\"}" == "http"* ]]; then
              # the repository is defined with a full URL
              echo "git clone -b ${submodule_branch//\"} --depth 1 ${submodule_name//\"}"
              git clone -q -b ${submodule_branch//\"} --depth 1 ${submodule_name//\"}
            else
              # only repository name is given - assume dhis2 project on github
              echo "git clone -b ${submodule_branch//\"} --depth 1 $GIT_BASE${submodule_name//\"}"
              git clone -q -b ${submodule_branch//\"} --depth 1 $GIT_BASE${submodule_name//\"}
            fi

            popd
          }
      fi
  }
  done < $1

}

assemble_content() {
    echo "assembling $1"
    md=`basename $1 | sed 's:_INDEX\.:\.:'`
    # for s in `cat $1 | sed "s/^/'/ ; s/\$/'/"`
    # do
    #     include_submodules $s
    # done

    include_submodules $1

    markdown-pp $1 -o $tmp/$md
}

assemble_resources() {
    new=`echo $1 | sed 's/\(.*\)\/resources\/images\(.*\)/resources\/images\/\1\2/'`
    newfull=$2/$new
    mkdir -p `dirname $newfull`
    cp $1 $newfull
}

make_html() {

    cd $TMPBASE/$lang
    name=$1
    subdir=$2
    mkdir -p ${target}/${subdir}/html
    shared_resources ${target}/${subdir}/html
    for res in `egrep -o '(resources/images[^)]*)' ${name}.md | uniq`; do
        mkdir -p ${target}/${subdir}/html/`dirname $res`
        cp $res ${target}/${subdir}/html/$res
    done
    for res in `egrep -o 'resources/images[^)]*' ${name}_custom_bookinfo.md | uniq`; do
        mkdir -p ${target}/${subdir}/html/`dirname $res`
        cp $res ${target}/${subdir}/html/$res
    done
    echo "compiling ${name}.md to html in $TMPBASE/$lang/"
    chapters="${name}_custom_bookinfo.md ${name}_bookinfo.md ${name}.md"
    css="./resources/css/dhis2.css"
    template="./resources/templates/dhis2_template.html"
    thanks=""
    if [ -f "./resources/i18n/thanks/${name}_${lang}.html" ]; then
        thanks=" -B ./resources/i18n/thanks/${name}_${lang}.html "
    fi

    pandoc ${thanks} ${chapters} -c ${css} --template="${template}" --toc -N --section-divs -t html5 -o ${target}/${subdir}/html/${name}_full.html

    cd ${target}/${subdir}/html/
    # fix the section mappings in the full html file
    echo "remapping the section identifiers"
    id_mapper ${name}_full.html
    # split the full html file into chunks
    echo "splitting the html file into chunks"
    chunked_template="$tmp/resources/templates/dhis2_chunked_template.html"
    chunker ${name}_full.html ${chunked_template}

}

make_pdf() {
    cd $TMPBASE/$lang
    name=$1
    subdir=$2
    echo "making pdf in $PWD"
    mkdir -p ${target}/${subdir}
    echo "compiling $name.md to pdf"
    chapters="${name}_custom_bookinfo.md ${name}_bookinfo.md ${name}.md"
    css="./resources/css/dhis2_pdf.css"
    template="./resources/templates/dhis2_template.html"
    thanks=""
    if [ -f "./resources/i18n/thanks/${name}_${lang}.html" ]; then
        thanks=" -B ./resources/i18n/thanks/${name}_${lang}.html "
    fi
    pandoc ${thanks} ${chapters} -c ${css} --template="${template}" --toc -N --section-divs --pdf-engine=weasyprint -o ${target}/${subdir}/${name}.pdf
}


add_to_mkdocs() {
    cd $TMPBASE/$lang/

    name=$1
    subdir=$2

    echo "preparing temporary files for mkdocs in $PWD"
    mkdir -p docs/${name}
    cp ${name}.md docs/
    pushd docs
      if [ "$lang" != "en" ]
      then
          tx_url="https://www.transifex.com/hisp-uio"
          lang_edit="${tx_url}/dhis-2-documentation-${txproject}/translate/#${lang}/${name//_/-}-md"
          chapterise ${name} ${TMPBASE}/$lang/mkdocs.yml -e ${lang_edit}
      else
          chapterise ${name} ${TMPBASE}/$lang/mkdocs.yml
      fi

      pushd ${name}
        ln -s ../../resources .
      popd
      rm $name.md
    popd
    # rm $name.md

    # Always add the javadoc link after the developer manual
    if [ "${name}" == "dhis2_developer_manual" ]
    then
      echo "        DHIS2 API Javadocs: https://docs.dhis2.org/$gitbranch/javadoc/" >> ${TMPBASE}/$lang/mkdocs.yml
    fi

}

assemble(){
    name=$1

    # copy resources and assembled markdown files to temp directory
    shared_resources $tmp
    cp $src/content/common/bookinfo.md $tmp/
    cd $src

    # copy resources and assembled markdown files to temp directory
    for f in ${name}_INDEX.md; do
        #echo "file: $f"
        assemble_content $f
    done

    for path in `grep "INCLUDE" ${name}_INDEX.md | sed 's/,.*// ; s/[^"]*"// ; s/[^/]*"//' | sort -u`; do
      for r in `find $path -type f | grep "resources/images"`; do
          # echo "resource: $r"
          assemble_resources $r $tmp
      done
    done

    for path in `grep "SUBMODULE" ${name}_INDEX.md | sed 's/[^"]*"/content\/submodules\// ; s/http[^"]*\/// ; s/,.*// ; s/" "[^"]*" "/\// ; s/[^/]*"//' | sort -u`; do
      for r in `find $path -type f | grep "resources/images"`; do
          # echo "resource: $r"
          assemble_resources $r $tmp
      done
    done

    # clean up any "submodule" clones!
    rm -rf $src/content/submodules/*
    # rm $tmp/${name}.md

}

update_localizations(){
    name=$1
    cd $TMPBASE


    # check for the transifex environment
    if [ ${LOCALISE} -eq 1 ]; then
       mkdir -p .tx
       cp en/resources/i18n/transifex-config .tx/config
       # <tx-project>.<resource-name>
       txproject=`git ls-remote --heads origin | grep $(git rev-parse HEAD) | cut -d / -f 3`
       if [ "${txproject}" == "" ]; then
           txproject="UNKNOWN"
       fi
       sed -i "s/<tx-project>/${txproject//.}/" .tx/config
       sed -i "s/<name>/${name}/" .tx/config
       sed -i "s/<resource-name>/${name//_/-}/" .tx/config
       # Use "prettier" to ensure each markdown text block is on a single line
       # This is necessary to ensure that blocks are correctly parsed by transifex.
       prettier --prose-wrap never --write en/${name}.md
       sed -i 's/ *<!-- DHIS2-EDIT:[^>]*-->//' en/${name}.md

       # Push source updates to transifex
       tx push -s

    else
       echo "INFO: Transifex not configured. Source files will not be pushed."
    fi

    rm en/${name}.md
}

pull_translations(){
    name=$1
    cd $TMPBASE
    tmp_name=${lang}/${name}.tmp


    # check for the transifex environment
    if [ ${LOCALISE} -eq 1 ]; then
       mkdir -p .tx
       cp $lang/resources/i18n/transifex-config .tx/config
       # <tx-project>.<resource-name>
       txproject=`git ls-remote --heads origin | grep $(git rev-parse HEAD) | cut -d / -f 3`
       if [ "${txproject}" == "" ]; then
           txproject="master"
       fi
       sed -i "s/<tx-project>/${txproject//.}/" .tx/config
       sed -i "s/<name>/${name}/" .tx/config
       sed -i "s/<resource-name>/${name//_/-}/" .tx/config

       # Pull the translations from transifex
       # only French at the moment - should be refactored for multiple languages
       tx pull --language $lang --force --skip
       # ln -s ../en/resources fr/resources

    else
       echo "INFO: Transifex not configured. Localised versions will not be generated."
    fi

}

make_mkdocs(){
  pushd $tmp

  sed -e "/|--- |--- |--- |--- |/r${tmp}/doc_links.md" -i docs/index.md

  pushd docs
    ln -s ../resources .
  popd
  rm -rf resources/mkdocs
  mkdocs build --dirty
  popd
}

build_docs(){

    name=$1
    subdir=$2
    selection=$3
    lang=$4
    locale=$5

    tmp="$TMPBASE/$lang"
    cd $tmp
    echo "Working in $PWD"

    target="$localisation_root/$lang"
    mkdir -p $target

    gitbranch=`git ls-remote --heads origin | grep $(git rev-parse HEAD) | cut -d / -f 3`

    if [ "$gitbranch" == "" ]
    then
        gitbranch='master'
    fi
    githash=`git rev-parse --short HEAD`
    gitdate=`git show -s --format=%ci $githash`
    gityear=`date -d "${gitdate}" '+%Y'`
    gitmonth=`LC_TIME=${locale}.utf8 date -d "${gitdate}" '+%B'`
    cp bookinfo.md ${name}_bookinfo.md
    sed -i "s/<git-branch>/$gitbranch/" ${name}_bookinfo.md
    sed -i "s/<git-hash>/$githash/" ${name}_bookinfo.md
    sed -i "s/<git-date>/$gitdate/" ${name}_bookinfo.md
    sed -i "s/<git-year>/$gityear/" ${name}_bookinfo.md
    sed -i "s/<git-month>/$gitmonth/" ${name}_bookinfo.md

    sed -i "s/<version>/$gitbranch/" $tmp/mkdocs.yml
    sed -i "s/<language>/$lang/" $tmp/mkdocs.yml

    echo -e "$(head -100 ${tmp}/${name}.md | sed -n '/---$/,/---$/p')" > ${name}_custom_bookinfo.md
    touch ${name}_custom_bookinfo.md
    sed -i 's/\([^ :]*\)\/resources\/images\(.*\)/resources\/images\/\1\2/' ${name}_custom_bookinfo.md

    if [ $selection == "html" ]
    then
      make_html $name $subdir
    elif [ $selection == "pdf" ]
    then
      make_pdf $name $subdir
    else
      make_html $name $subdir
      make_pdf $name $subdir
    fi

    add_to_mkdocs $name $subdir

    mylink=$(grep ${name} ${tmp}/mkdocs.yml | head -1 | sed 's/.* //')

    title=$(grep "title:" ${name}_custom_bookinfo.md | sed 's/title://')
    if [ "$title" == "" ]
    then
      title=${name}
    fi
    touch $tmp/doc_links.md
    echo "|${title//\'/}|[Explore](/${gitbranch}/${lang}/${mylink//md/html})|[View as Single Page](/${gitbranch}/${lang}/${subdir}/html/${name}_full.html)|[Download PDF](/${gitbranch}/${lang}/${subdir}/${name}.pdf)|" >> $tmp/doc_links.md

}
