import os

print("*** Read version name and code ***")

RELEASE_INFO_PATH = '../gradle/libs.versions.toml'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(" = ")
        if len(y) == 2:
            release_versions[y[0]] = str(y[1])

name = "envman add --key NAME_RELEASE --value {}".format(release_versions.get('vName').replace("\"", ""))
branchName = "release/" + release_versions.get('vName').replace("\"", "")
branch = "envman add --key BRANCH_RELEASE --value {}".format(branchName)


os.system(name)
os.system(branch)

print("*** Done! ***")
