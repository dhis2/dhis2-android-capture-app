import sys
from toml_file_handler import load_toml, save_toml

file_path = 'gradle/libs.versions.toml'
data = load_toml(file_path)

if len(sys.argv) > 1:
    newVersion = sys.argv[1]

    save_toml(file_path, 'vName', newVersion)
    print("File updated successfully!")
else:
    print("No new version provided. To update the version, pass the new version as a command-line argument.")
