import os
import csv
import re

# Paths
FIELDS_CSV = 'mappings_temp/fields.csv'
METHODS_CSV = 'mappings_temp/methods.csv'
SRC_DIR = 'src/main/java'

def load_mappings(csv_path):
    mappings = {}
    if not os.path.exists(csv_path):
        print(f"Warning: {csv_path} not found")
        return mappings
    
    with open(csv_path, 'r') as f:
        reader = csv.reader(f)
        header = next(reader) # Skip header
        for row in reader:
            # Format usually: searge,name,side,desc
            if len(row) >= 2:
                srg = row[0]
                name = row[1]
                mappings[srg] = name
    return mappings

print("Loading mappings...")
field_map = load_mappings(FIELDS_CSV)
method_map = load_mappings(METHODS_CSV)
print(f"Loaded {len(field_map)} fields and {len(method_map)} methods.")

# Regex for SRG matching
# Matches field_12345_a or func_12345_a or func_12345_a_
srg_pattern = re.compile(r'(field_\d+_[a-zA-Z_]+|func_\d+_[a-zA-Z_]+)')

def replace_match(match):
    srg = match.group(1)
    if srg.startswith('field_'):
        return field_map.get(srg, srg)
    elif srg.startswith('func_'):
        return method_map.get(srg, srg)
    return srg

print("Remapping source files...")
count = 0
for root, dirs, files in os.walk(SRC_DIR):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = srg_pattern.sub(replace_match, content)
            
            if new_content != content:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                count += 1
                # print(f"Remapped {file}")

print(f"Done. Remapped {count} files.")
