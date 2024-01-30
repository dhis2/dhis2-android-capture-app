import os

import requests

print("*** Update Github notes ***")

JIRA_AUTH = os.environ.get('JIRA_AUTH')
JIRA_FILTER = os.environ.get('FILTER_ID')
RELEASE_INFO_PATH = '../gradle/libs.versions.toml'
RELEASE_MD_PATH = '../RELEASE.md'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(" = ")
        if len(y) == 2:
            release_versions[y[0]] = str(y[1])

print("Getting issues from filter version...")
print(JIRA_FILTER)
filtered_issues = requests.get(
    "https://dhis2.atlassian.net/rest/api/latest/search?jql=filter=" + JIRA_FILTER,
    headers={'Authorization': JIRA_AUTH}).json()

print("Writing files in RELEASE.md...")
version_summary = '''This is a patch version of the <strong>DHIS2 Android App</strong> It builds upon the last version including bug fixes that couldn't wait to the next version. 
It includes no functional improvements neither changes in the User Interface. It means that yours users can update without experiencing any change in the UI. '''
content_header = '''<table>
<tr> 
<td> 
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800"> 
</td> 
<td>
''' + version_summary + '''
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">
'''
content_footer = '''
</td>
</tr>
</table>'''
f = open(RELEASE_MD_PATH, "w")
f.write("Android Capture App for DHIS 2 (v" + release_versions.get('vName').replace("\"", "") + ") - Patch version\n")
f.write(content_header)
f.write("\n## Bugs fixed\n")
for issue in filtered_issues['issues']:
    f.write("* [" + issue['key'] + "](https://dhis2.atlassian.net/browse/" + issue['key'] + ") " +
            issue['fields']['summary'] + "\n")
f.write('''* This patch release updates the [Android SDK](https://github.com/dhis2/dhis2-android-sdk) to version ''' + release_versions.get('dhis2sdk').replace("\"", "") + '''.
    
You can find in Jira details on the [bugs fixed](https://dhis2.atlassian.net/issues/?filter=''' + JIRA_FILTER + ''') in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed 
information of the features included in the App and how to configure DHIS2 to use it. 

Please create a [Jira](https://dhis2.atlassian.net) Issue if you find a bug or
you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].''')
f.write(content_footer)
f.close()

print("Done!.")
