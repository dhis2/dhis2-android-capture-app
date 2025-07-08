import sys
from toml_file_handler import load_toml, save_toml

file_path = 'gradle/libs.versions.toml'
data = load_toml(file_path)

if len(sys.argv) > 2:
    sdkVersion = sys.argv[1]
    designSystemVersion = sys.argv[2]

    save_toml(file_path, 'dhis2sdk', sdkVersion)
    save_toml(file_path, 'designSystem', designSystemVersion)

    print("File updated successfully!")
else:
    print("two parameters are required. To update the versions, pass each new version as a command-line argument.")
