#!/bin/bash

#

for f in  `find src/commonmark/en/content/implementation -name '*.md'`;do
  from="`file -bi ${f} | sed -e 's/.*[ ]charset=//' | tr 'a-z' 'A-Z'`"
  if [ $from != "UTF-8" ];then

    echo "convert ${f} from $from"
    iconv --verbose -f $from -t UTF-8 ${f} -o ${f}_conv.md
    #mv ${f}_conv.md ${f}
  fi
done
