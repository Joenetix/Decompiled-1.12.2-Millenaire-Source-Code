"""
Find colors from color sheet that are missing in blocklist.txt
and generate blocklist entries for them.
"""
import json
import re

# Load color sheet
with open(r'C:\Users\Joey\.gemini\antigravity\brain\c3cbe1bf-7148-4296-a87c-a6b08dd35e77\color_sheet_with_labels.json') as f:
    color_sheet = json.load(f)

# Load blocklist
with open(r'c:\Millenaire Revived v0\src\main\resources\assets\millenaire\blocklist.txt') as f:
    blocklist_lines = f.readlines()

# Parse blocklist colors
blocklist_colors = set()
for line in blocklist_lines:
    match = re.search(r'(\d+)/(\d+)/(\d+)', line)
    if match:
        r, g, b = int(match.group(1)), int(match.group(2)), int(match.group(3))
        blocklist_colors.add((r, g, b))

# Get color sheet colors
sheet_colors = {tuple(c['rgb']): c['label'] for c in color_sheet}

# Find missing colors (in color sheet but not in blocklist)
missing = set(sheet_colors.keys()) - blocklist_colors

print(f"Color sheet has {len(sheet_colors)} colors")
print(f"Blocklist has {len(blocklist_colors)} colors")
print(f"Colors MISSING from blocklist: {len(missing)}")

# Generate blocklist entries for missing colors
# Map label to minecraft block
def label_to_block(label):
    label_lower = label.lower().replace(' ', '')
    
    # Skip special markers
    skip_patterns = ['pos', 'spawn', 'source', 'soil', 'guess', 'spot', 'generated']
    for skip in skip_patterns:
        if skip in label_lower:
            return None
    
    # Map to blocks
    if 'empty' in label_lower:
        return 'minecraft:air;0'
    elif 'thatch' in label_lower:
        return 'millenaire:wood_deco;2'
    elif 'stonebrick' in label_lower:
        return 'minecraft:stonebrick;0'
    elif 'crackedstone' in label_lower:
        return 'minecraft:stonebrick;2'
    elif 'mossystone' in label_lower:
        return 'minecraft:stonebrick;1'
    elif 'cobblestone' in label_lower:
        return 'minecraft:cobblestone;0'
    elif 'sandstone' in label_lower:
        return 'minecraft:sandstone;0'
    elif 'stone' in label_lower:
        return 'minecraft:stone;0'
    elif 'planksoak' in label_lower or 'planks' == label_lower:
        return 'minecraft:planks;variant=oak'
    elif 'plankspine' in label_lower or 'planksspruce' in label_lower:
        return 'minecraft:planks;variant=spruce'
    elif 'planksbirch' in label_lower:
        return 'minecraft:planks;variant=birch'
    elif 'planksjungle' in label_lower:
        return 'minecraft:planks;variant=jungle'
    elif 'planksacacia' in label_lower:
        return 'minecraft:planks;variant=acacia'
    elif 'planksdark' in label_lower:
        return 'minecraft:planks;variant=dark_oak'
    elif 'logoak' in label_lower:
        return 'minecraft:log;variant=oak'
    elif 'logpine' in label_lower or 'logspruce' in label_lower:
        return 'minecraft:log;variant=spruce'
    elif 'logbirch' in label_lower:
        return 'minecraft:log;variant=birch'
    elif 'logjungle' in label_lower:
        return 'minecraft:log;variant=jungle'
    elif 'logacacia' in label_lower:
        return 'minecraft:log2;variant=acacia'
    elif 'logdarkoak' in label_lower:
        return 'minecraft:log2;variant=dark_oak'
    elif 'torch' in label_lower:
        return 'minecraft:torch;0'
    elif 'chest' in label_lower:
        return 'minecraft:chest;0'
    elif 'door' in label_lower:
        return 'minecraft:wooden_door;0'
    elif 'fencegate' in label_lower:
        return 'minecraft:fence_gate;0'
    elif 'fence' in label_lower:
        return 'minecraft:fence;0'
    elif 'wool' in label_lower:
        return 'minecraft:wool;0'
    elif 'glass' in label_lower:
        return 'minecraft:glass;0'
    elif 'carpet' in label_lower:
        return 'minecraft:carpet;0'
    elif 'terracotta' in label_lower:
        return 'minecraft:hardened_clay;0'
    elif 'banner' in label_lower:
        return 'minecraft:standing_banner;0'
    elif 'bed' in label_lower:
        return 'minecraft:bed;0'
    elif 'gravel' in label_lower:
        return 'minecraft:gravel;0'
    elif 'dirt' in label_lower:
        return 'minecraft:dirt;0'
    elif 'grass' in label_lower:
        return 'minecraft:grass;0'
    elif 'pathslabs' in label_lower:
        return 'millenaire:pathslabs_slab;0'
    elif 'pathgravel' in label_lower:
        return 'millenaire:pathgravel_slab;0'
    elif 'pathdirt' in label_lower:
        return 'millenaire:pathdirt_slab;0'
    elif 'pathsnow' in label_lower:
        return 'millenaire:pathsnow_slab;0'
    elif 'path' in label_lower:
        return 'millenaire:pathslabs;0'
    elif 'brick' in label_lower:
        return 'minecraft:brick_block;0'
    elif 'bedrock' in label_lower:
        return 'minecraft:bedrock;0'
    
    return None

# Generate new blocklist entries
new_entries = []
for rgb in sorted(missing):
    label = sheet_colors[rgb]
    block = label_to_block(label)
    if block:
        parts = block.split(';')
        block_id = parts[0]
        meta = parts[1] if len(parts) > 1 else '0'
        color_str = f"{rgb[0]}/{rgb[1]}/{rgb[2]}"
        entry = f"{label};{block_id};{meta};false;{color_str}"
        new_entries.append(entry)

print(f"\nGenerated {len(new_entries)} new blocklist entries")
print("\n=== SAMPLE NEW ENTRIES ===")
for entry in new_entries[:20]:
    print(entry)

# Save to file
with open('missing_blocklist_entries.txt', 'w') as f:
    f.write("// Colors from color sheet that were missing from blocklist.txt\n")
    f.write("// Add these to blocklist.txt\n\n")
    for entry in new_entries:
        f.write(entry + "\n")

print(f"\nSaved {len(new_entries)} entries to missing_blocklist_entries.txt")
