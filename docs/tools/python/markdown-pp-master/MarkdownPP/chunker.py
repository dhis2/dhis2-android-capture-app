#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Sep 13 15:56:39 2018

@author: philld
"""
import argparse
from bs4 import BeautifulSoup, NavigableString


class toc(object):

    def __init__(self,level,title,section,id):
        self.level = level
        self.title = title         # title of the item
        self.section = section +".html"  # this is the chunk it is contained in
        self.id = id           # this is the actual section id of the item

        title_bits = self.title.split(" ")
        title_bits[0] = '<span class="toc-section-number">'+title_bits[0]+'</span>'
        toc_title = ' '.join(title_bits)
        self.link = '<li><a href="'+self.section+'#'+self.id+'">'+toc_title+'</a></li>'



class chunk(object):

    def __init__(self, id,chapter,chapter_id,section, content):
        self.id = id+".html"
        self.chapter = chapter
        self.chapter_id = chapter_id+".html"
        self.section = section
        self.content = content
        self.prev_id = None
        self.prev_section = None
        self.next_id = None
        self.next_section = None


def main():
    # setup command line arguments
    parser = argparse.ArgumentParser(description='Postprocessor for generating'
                                     ' chunked html files.')

    parser.add_argument('FILENAME', help='Input file name (or directory if '
                        'watching)')

    parser.add_argument('TEMPLATE', help='Template file name.')

    args = parser.parse_args()

    html_doc = args.FILENAME

    root=html_doc.split('/')[-1].replace('_full.html','')
    template = args.TEMPLATE

    contents = []
    chunks = []


    with open(html_doc) as fp:
        soup = BeautifulSoup(fp,"html5lib")

    root_title = soup.title.string
    #for ban in soup.body.find_all("div", {'class':'banner'}):
    #    ban.decompose()
    first = chunk(root,root_title,root,root_title,soup.body)
    chunks.append(first)

    chapter = root_title
    chapter_id = root
    lastlevel = 0
    for u in soup.find_all('section','level1'):
        # start of a new chapter
        i = u.extract()

        second = []
        second.append(i)
        for j in i.find_all('section','level2')[1:]:
            second.append(j.extract())

        for s in second:
            # Start of a new section
            thislevel = s['class'][0]
            if thislevel == 'level1':
                chapter = s.h1.get_text()
                chapter_id = s['id']
                section = chapter
                #print(section,s['id'],"#")
                toc_entry = toc(1,section,s['id'],"")
                contents.append(toc_entry)

                myChunk = chunk(s['id'],chapter,chapter_id,section,s)
                lastchunk = chunks[-1]
                lastchunk.next_id = myChunk.id
                lastchunk.next_section = myChunk.section
                myChunk.prev_id = lastchunk.id
                myChunk.prev_section = lastchunk.section
                chunks.append(myChunk)

            elif thislevel == 'level2':
                section = s.h2.get_text()
                #print("  ",section,s['id'],"#")
                toc_entry = toc(2,section,s['id'],"")
                contents.append(toc_entry)

                myChunk = chunk(s['id'],chapter,chapter_id,section,s)
                lastchunk = chunks[-1]
                lastchunk.next_id = myChunk.id
                lastchunk.next_section = myChunk.section
                myChunk.prev_id = lastchunk.id
                myChunk.prev_section = lastchunk.section
                chunks.append(myChunk)


            for c in s.find_all('section'):
                thislevel = c['class'][0]
                if thislevel == 'level2':
                    #print("  ",c.h2.get_text(),s['id'],"#",c['id'])
                    toc_entry = toc(2,c.h2.get_text(),s['id'],c['id'])
                    contents.append(toc_entry)
                elif thislevel == 'level3':
                    #print("    ",c.h3.get_text(),s['id'],"#",c['id'])
                    toc_entry = toc(3,c.h3.get_text(),s['id'],c['id'])
                    contents.append(toc_entry)

    # regenerate the TOC and replace it
    newtoc = ""
    lastlevel = 0
    for con in contents:
        if con.level > lastlevel:
            for i in range(con.level - lastlevel):
                newtoc += "<ul>"
        if con.level < lastlevel:
            for i in range(lastlevel - con.level):
                newtoc += "</ul>"
        newtoc += con.link
        lastlevel = con.level
    for i in range(lastlevel):
        newtoc += "</ul>"

    for nt in soup.body.select("#TOC"):
        nt.clear()
        nt.append(BeautifulSoup(newtoc,"html5lib"))


    # output each chunk to a file (based on the chunked template)
    for chu in chunks:
        with open(template) as cfp:
            chunky_soup = BeautifulSoup(cfp,"html5lib")
            chunky_soup.head.title.string = chu.section

            if chu.section != chu.chapter:
                for cn in chunky_soup.body.select("#chaptername"):
                    cn.string = "Chapter "+chu.chapter
                for tn in chunky_soup.body.select("#thisname"):
                    tn.string = chu.section
            else:
                for tn in chunky_soup.body.select("#thisname"):
                    prefix = "Chapter "
                    if chu.section == root_title:
                        prefix = ""
                    tn.string = prefix+chu.section

            for un in chunky_soup.body.select("#upnav"):
                un["href"] = chu.chapter_id

            if chu.prev_section:
                for pl in chunky_soup.head.select("#prevlink"):
                    pl["href"] = chu.prev_id
                    pl["title"] = chu.prev_section
                for pn in chunky_soup.body.select("#prevnav"):
                    pn["href"] = chu.prev_id
                for pn in chunky_soup.body.select("#prevname"):
                    pn.string = chu.prev_section

            if chu.next_section:
                for nl in chunky_soup.head.select("#nextlink"):
                    nl["href"] = chu.next_id
                    nl["title"] = chu.next_section
                for nn in chunky_soup.body.select("#nextnav"):
                    nn["href"] = chu.next_id
                for nn in chunky_soup.body.select("#nextname"):
                    nn.string = chu.next_section

            for ul in chunky_soup.head.select("#uplink"):
                ul["href"] = chu.chapter_id
                ul["title"] = chu.chapter

            for hn in chunky_soup.body.select("#homename"):
                hn["href"] = root+".html"

            chunky_soup.select(".chapter")[0].append(chu.content)

        chw = open(chu.id,'w')
        chw.write(str(chunky_soup))
        chw.close()


if __name__ == "__main__":
    main()
