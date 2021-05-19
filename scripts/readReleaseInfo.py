import os

print("*** Read version name and code ***")

RELEASE_INFO_PATH = '../release.info'

release_versions = {}
with open(RELEASE_INFO_PATH, 'r') as file:
    for line in file:
        y = line.strip().split(":")
        release_versions[y[0]] = str(y[1])

name = f"envman add --key NAME_RELEASE --value {release_versions.get('vName')}"
branch = f"envman add --key NAME_RELEASE --value {release_versions.get('vBranch')}"
sdk_version = f"envman add --key NAME_RELEASE --value {release_versions.get('sdkVersion')}"

os.system(name)
os.system(branch)
os.system(sdk_version)

print("*** Done! ***")
