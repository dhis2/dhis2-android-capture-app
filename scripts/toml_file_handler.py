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

def save_toml(file_path, keyToUpdate, newVersion):
    with open(file_path, 'r') as file:
        lines = file.readlines()

    # Write the data back to the TOML file
    with open(file_path, 'w') as file:
        current_section = None
        for line in lines:
            line = line.strip()
            if line.startswith('[') and line.endswith(']'):
                current_section = line[1:-1]
                file.write(f'{line}\n')
            elif '=' in line and current_section == 'versions':
                key = line.split('=')[0].strip()
                if key == keyToUpdate:
                    # Add quotes around the new version
                    file.write(f'{key} = "{newVersion}"\n')
                else:
                    file.write(f'{line}\n')
            else:
                file.write(f'{line}\n')
