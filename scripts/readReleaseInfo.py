import os

print("*** Read version name and code ***")

RELEASE_INFO_PATH = '../release.info'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(":")
        release_versions[y[0]] = str(y[1])

name = release_versions.get('vName')
branch = release_versions.get('vBranch')
sdk_version = release_versions.get('sdkVersion')

os.system(f'envman add --key NAME_RELEASE --value {name}')
os.system(f'envman add --key BRANCH_RELEASE --value {branch}')
os.system(f'envman add --key SDK_VERSION --value {sdk_version}')

print("*** Done! ***")
