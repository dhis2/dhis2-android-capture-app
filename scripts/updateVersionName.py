import toml
import os


file_path = os.path.join('gradle', 'libs.versions.toml')

# Load the TOML file
with open(file_path, 'r') as file:
    data = toml.load(file)

# print("Data dictionary:", data)

# Print the current value of vName
current_vName = data['versions']['vName']
print("Current value of vName:", current_vName)

# Update the desired value
data['versions']['vName'] = '2.10'

# Save the changes back to the file
with open(file_path, 'w') as file:
    toml.dump(data, file)

# validate changes
with open(file_path, 'r') as file:
    updated_data = toml.load(file)

print("New value of vName:", data['versions']['vName'])
