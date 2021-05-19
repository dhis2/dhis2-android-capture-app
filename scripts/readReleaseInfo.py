import os

print("*** Read version name and code ***")

RELEASE_INFO_PATH = '../release.info'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(":")
        release_versions[y[0]] = str(y[1])

os.environ['NAME_RELEASE'] = release_versions.get('vName')
os.environ['BRANCH_RELEASE'] = release_versions.get('vBranch')
os.environ['SDK_VERSION'] = release_versions.get('sdkVersion')


print(os.environ)

print("*** Done! ***")