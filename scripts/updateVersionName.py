import toml
# import os
import sys

# file_path = os.path.join('gradle', 'libs.versions.toml')
file_path = 'gradle/libs.versions.toml'

# Load the TOML file
with open(file_path, 'r') as file:
    data = toml.load(file)

# Print the current value of vName
current_vName = data['versions']['vName']
print("Current value of vName:", current_vName)

# Check if a new version is provided as a command-line argument
if len(sys.argv) > 1:
    new_vName_value = sys.argv[1]
    # Update the 'vName' value in the data dictionary
    data['versions']['vName'] = new_vName_value
    # Print the updated value
    print("Updated value of vName:", new_vName_value)

    # Save the updated data back to the TOML file
    with open(file_path, 'w') as file:
        toml.dump(data, file)
        print("File updated successfully!")
else:
    print("No new version provided. To update the version, pass the new version as a command-line argument.")
