# DHIS 2 Android Capture App - Documentation

This folder contains several subfolder related to the documentation of the DHIS 2 Android App. The main documentation of the Application is under the specific folder but other folders have been added to include documentation related to the Application. 

Current documentation has only been written in English although the folder structure is already prepared to support different languages.

In order to create new documentation:

* Create a new folder under src/commonmark/en/content/(name) and add all the doc files there
* Create an index in the folder src/commonmark/en as (name)\_INDEX.md
* Create a new index file in the [DHIS2-doc](https://github.com/dhis2/dhis2-docs/tree/master/src/commonmark/en) poiting to the INDEX.md created

*WARNING*
This repository allows building documentation to see a preview of how the documents will look like before commiting the changes. It uses the scripts from [DHIS2-doc](https://github.com/dhis2/dhis2-docs/tree/master/src/commonmark/en) but is not guaranteed to work or show similar results as everything is this repository might be outdated. In order to see a real produced document generate them from the DHIS2-doc repository.
