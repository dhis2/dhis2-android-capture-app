import os

print("*** Read version name and code ***")

RELEASE_INFO_PATH = '../release.info'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(":")
        release_versions[y[0]] = str(y[1])

name = "envman add --key NAME_RELEASE --value {}".format(release_versions.get('vName'))
branch = "envman add --key BRANCH_RELEASE --value {}".format(release_versions.get('vBranch'))

os.system(name)
os.system(branch)

print("*** Done! ***")
