import sys
from toml_file_handler import load_toml, save_toml

file_path = 'gradle/libs.versions.toml'
data = load_toml(file_path)

raw_value = data['versions']['vCode']
cleaned_value = raw_value.strip("'\"")
value = int(cleaned_value) + 1

try:
    save_toml(file_path, 'vCode', value)
    print("File updated successfully!")
except (FileNotFoundError, PermissionError, IOError):
    print("An error occurred while updating the file.")
