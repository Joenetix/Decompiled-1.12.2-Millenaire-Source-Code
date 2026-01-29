import os
import json
import re

# Direct path to blockstates
state_dir = "C:/Millenaire Revived v0/src/main/resources/assets/millenaire/blockstates"

def fix_json(file_path):
    with open(file_path, 'r') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError:
            print(f"Error parsing {file_path}")
            return

    if 'variants' not in data:
        return

    new_variants = {}
    modified = False

    is_slab = "_slab" in file_path
    is_path_slab = "path" in file_path and is_slab
    # Oriented slabs: gray_tiles_slab, green_tiles_slab, red_tiles_slab
    is_oriented_slab = "tiles_slab" in file_path 

    for key, model in data['variants'].items():
        new_key = key

        # Fix specific single-variant legacy keys
        if new_key == "normal":
            new_key = ""
        
        # Parse logic for complex keys
        parts = new_key.split(',')
        new_parts = []
        
        for part in parts:
            if not part: continue
            
            # Remove 'variant=...'
            if part.startswith('variant='):
                continue
            
            # Replace 'half=' with 'type=' for slabs
            if is_slab and part.startswith('half='):
                val = part.split('=')[1]
                new_parts.append(f"type={val}")
                continue
                
            # Remove 'axis=' for NON-oriented slabs (path slabs)
            if is_path_slab and not is_oriented_slab and part.startswith('axis='):
                continue

            # Remove 'axis=y' for oriented slabs (only x/z valid)
            if is_oriented_slab and part == 'axis=y':
                continue
            
            # Keep other parts
            new_parts.append(part)
        
        # Reconstruct key
        final_key = ",".join(sorted(new_parts)) # Sorted ensures canonical order? Or logic order? 
        # Actually MC mostly doesn't care about order, but let's keep it simple.
        
        if final_key != key:
            modified = True
        
        new_variants[final_key] = model

    if modified:
        data['variants'] = new_variants
        with open(file_path, 'w') as f:
            json.dump(data, f, indent=4)
        print(f"Fixed {file_path}")

for filename in os.listdir(state_dir):
    if filename.endswith(".json"):
        fix_json(os.path.join(state_dir, filename))
