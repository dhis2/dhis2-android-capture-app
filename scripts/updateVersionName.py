import sys

def load_toml(file_path):
    # Read the TOML file and parse it manually
    with open(file_path, 'r') as file:
        lines = file.readlines()

    data = {}
    current_section = None

    for line in lines:
        line = line.strip()
        if line.startswith('[') and line.endswith(']'):
            current_section = line[1:-1]
            data[current_section] = {}
        elif '=' in line and current_section:
            key, value = line.split('=', 1)
            data[current_section][key.strip()] = value.strip()

    return data

def save_toml(file_path, data, newVersion):
    # Write the data back to the TOML file
    with open(file_path, 'w') as file:
        for section, section_data in data.items():
            file.write(f'[{section}]\n')
            for key, value in section_data.items():
                # Check if the key is 'vName' and update the value with quotes
                if section == 'versions' and key == 'vName':
                    value = f'"{newVersion}"'
                file.write(f'{key} = {value}\n')


file_path = 'gradle/libs.versions.toml'
data = load_toml(file_path)

if len(sys.argv) > 1:
    newVersion = sys.argv[1]
    # Update the 'vName' value in the data dictionary
    data['versions']['vName'] = newVersion
    save_toml(file_path, data, newVersion)
    print("File updated successfully!")
else:
    print("No new version provided. To update the version, pass the new version as a command-line argument.")
