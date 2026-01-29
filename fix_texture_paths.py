import os

root_dir = r"C:\Millenaire Revived v0\src\main\resources\assets\millenaire\models"

def fix_paths(filepath):
    try:
        with open(filepath, 'r') as f:
            content = f.read()
        
        new_content = content
        # Replace texture paths
        new_content = new_content.replace('millenaire:blocks/', 'millenaire:block/')
        new_content = new_content.replace('millenaire:items/', 'millenaire:item/')
        
        if new_content != content:
            print(f"Fixed {filepath}")
            with open(filepath, 'w') as f:
                f.write(new_content)
    except Exception as e:
        print(f"Error reading/writing {filepath}: {e}")

for subdir, dirs, files in os.walk(root_dir):
    for file in files:
        if file.endswith(".json"):
            fix_paths(os.path.join(subdir, file))
