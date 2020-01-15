readonly GIT_BASE="https://github.com/dhis2/"

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
    for res in `egrep -o 'resources/images[^)]*' custom_bookinfo.md | uniq`; do
        mkdir -p ${target}/${subdir}/html/`dirname $res`
        cp $res ${target}/${subdir}/html/$res
    done
    echo "compiling ${name}.md to html"
    chapters="custom_bookinfo.md bookinfo.md ${name}.md"
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
    chapters="custom_bookinfo.md bookinfo.md ${name}.md"
    css="./resources/css/dhis2_pdf.css"
    template="./resources/templates/dhis2_template.html"
    thanks=""
    if [ -f "./resources/i18n/thanks/${name}_${lang}.html" ]; then
        thanks=" -B ./resources/i18n/thanks/${name}_${lang}.html "
    fi
    pandoc ${thanks} ${chapters} -c ${css} --template="${template}" --toc -N --section-divs --pdf-engine=weasyprint -o ${target}/${subdir}/${name}.pdf
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
       if [ ${txproject} == "" ]; then
           txproject="master"
       fi
       sed -i "s/<tx-project>/${txproject//.}/" .tx/config
       sed -i "s/<name>/${name}/" .tx/config
       sed -i "s/<resource-name>/${name//_/-}/" .tx/config
       # Use "prettier" to ensure each markdown text block is on a single line
       # This is necessary to ensure that blocks are correctly parsed by transifex.
       prettier --prose-wrap never --write en/${name}.md

       # Push source updates to transifex
       tx push -s

    else
       echo "INFO: Transifex not configured. Source files will not be pushed."
    fi

}

pull_translations(){
    name=$1
    cd $TMPBASE


    # check for the transifex environment
    if [ ${LOCALISE} -eq 1 ]; then
       mkdir -p .tx
       cp en/resources/i18n/transifex-config .tx/config
       # <tx-project>.<resource-name>
       txproject=`git ls-remote --heads origin | grep $(git rev-parse HEAD) | cut -d / -f 3`
       if [ ${txproject} == "" ]; then
           txproject="master"
       fi
       sed -i "s/<tx-project>/${txproject//.}/" .tx/config
       sed -i "s/<name>/${name}/" .tx/config
       sed -i "s/<resource-name>/${name//_/-}/" .tx/config
       # Use "prettier" to ensure each markdown text block is on a single line
       # This is necessary to ensure that blocks are correctly parsed by transifex.
       # WE ARE NOT PUSHING TO TRANSIFEX IN THIS FUNCTION, SO STRICTLY SPEAKING
       # WE DON'T NEED THIS HERE
       prettier --prose-wrap never --write en/${name}.md

       # Pull the translations from transifex
       # only French at the moment - should be refactored for multiple languages
       tx pull -l fr
       ln -s ../en/resources fr/resources

    else
       echo "INFO: Transifex not configured. Localised versions will not be generated."
    fi

}

build_docs(){
    name=$1
    subdir=$2
    selection=$3
    lang=$4
    locale=$5

    cd $TMPBASE/$lang
    echo "Working in $PWD"

    target="$localisation_root/$lang"
    mkdir -p $target

    gitbranch=`git ls-remote --heads origin | grep $(git rev-parse HEAD) | cut -d / -f 3`
    githash=`git rev-parse --short HEAD`
    gitdate=`git show -s --format=%ci $githash`
    gityear=`date -d "${gitdate}" '+%Y'`
    gitmonth=`LC_TIME=${locale}.utf8 date -d "${gitdate}" '+%B'`
    sed -i "s/<git-branch>/$gitbranch/" bookinfo.md
    sed -i "s/<git-hash>/$githash/" bookinfo.md
    sed -i "s/<git-date>/$gitdate/" bookinfo.md
    sed -i "s/<git-year>/$gityear/" bookinfo.md
    sed -i "s/<git-month>/$gitmonth/" bookinfo.md

    echo -e "$(head -100 ${tmp}/${name}.md | sed -n '/---/,/---/p')" > custom_bookinfo.md
    touch custom_bookinfo.md
    sed -i ' s/\([^ :]*\)\/resources\/images\(.*\)/resources\/images\/\1\2/' custom_bookinfo.md

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

}
