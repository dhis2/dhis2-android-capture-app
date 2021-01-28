# Copyright 2015 John Reese
# Licensed under the MIT license

from __future__ import absolute_import
from __future__ import print_function
from __future__ import unicode_literals

import re
from os import path

from MarkdownPP.Module import Module
from MarkdownPP.Transform import Transform

BASE="content/submodules/"

class Include(Module):
    """
    Module for recursively including the contents of other files into the
    current document using a command like `!INCLUDE "path/to/filename"`.
    Target paths can be absolute or relative to the file containing the command
    """

    # matches !INCLUDE directives in .mdpp files
    includere = re.compile(r"^!INCLUDE\s+(?:\"([^\"]+)\"|'([^']+)')"
                           "\s*(?:,\s*(\d+))?\s*$")

    # check for any images that need to have their path shifted
    imagepath = re.compile(r"].*\((resources/images/.*)[ )]")
    simpleimagepath = re.compile(r"logo:.*(resources/images/)")

    # matches title lines in Markdown files
    titlere = re.compile(r"^(:?#+.*|={4,}|-{4,})$")

    # includes should happen before anything else
    priority = 0

    def transform(self, data):
        transforms = []

        linenum = 0
        for line in data:

            # a little hack to make a !SUBMODULE directive look like an !INCLUDE directive!
            if line[0:10] == "!SUBMODULE":
                parts=line.strip().replace('\'','').replace('"','').split(" ")
                line='!INCLUDE "'+BASE+parts[1].split('/')[-1]+'/'+parts[3]+'"'


            match = self.includere.search(line)
            if match:
                includedata = self.include(match)

                transform = Transform(linenum=linenum, oper="swap",
                                      data=includedata)
                transforms.append(transform)

            linenum += 1

        return transforms

    def include(self, match, pwd=""):
        # file name is caught in group 1 if it's written with double quotes,
        # or group 2 if written with single quotes
        filename = match.group(1) or match.group(2)

        dirname = path.dirname(filename)
        # print(pwd,filename)

        shift = int(match.group(3) or 0)

        if not path.isabs(filename):
            filename = path.join(pwd, filename)

        try:
            f = open(filename, "r")
            data = f.readlines()
            f.close()

            # line by line, apply shift and recursively include file data
            linenum = 0
            for line in data:
                # print(line)
                match = self.includere.search(line)
                if match:
                    dirname = path.dirname(filename)
                    data[linenum:linenum+1] = self.include(match, dirname)

                image = self.imagepath.search(line)
                if not image:
                    image = self.simpleimagepath.search(line)
                if image:
                    data[linenum] = line.replace("resources/images","resources/images/"+dirname)
                    # print(line)

                if shift:
                    titlematch = self.titlere.search(line)
                    if titlematch:
                        to_del = []
                        for _ in range(shift):
                            if data[linenum][0] == '#':
                                data[linenum] = "#" + data[linenum]
                            elif data[linenum][0] == '=':
                                data[linenum] = data[linenum].replace("=", '-')
                            elif data[linenum][0] == '-':
                                data[linenum] = '### ' + data[linenum - 1]
                                to_del.append(linenum - 1)
                        for l in to_del:
                            del data[l]

                titlematch = self.titlere.search(line)
                if titlematch:
                    try:
                        data[linenum] = re.sub(r'<!-- *{-} *-->', '{-}', data[linenum])
                    except IndexError:
                        pass

                linenum += 1

            # add a blank line to ensure new headings are correctly separated from previous text
            data.append("\n\n")
            return data

        except (IOError, OSError) as exc:
            print(exc)

        return []
