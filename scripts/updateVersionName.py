import toml


# Specify the path to your TOML file
file_path = 'gradle/libs.versions.toml'


# Load the TOML file
with open(file_path, 'r') as file:
    data = toml.load(file)

print("Current value of vName:", data['vName'])

# Update the desired value
data['vName'] = '2.10'

# Save the changes back to the file
with open(file_path, 'w') as file:
    toml.dump(data, file)

# validate changes
with open(file_path, 'r') as file:
    updated_data = toml.load(file)

print("New value of vName:", data['vName'])
