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
blocklist_map = {}
for line in blocklist_lines:
    match = re.search(r'(\d+)/(\d+)/(\d+)', line)
    if match:
        r, g, b = int(match.group(1)), int(match.group(2)), int(match.group(3))
        blocklist_colors.add((r, g, b))
        # Get block name
        parts = line.split(';')
        if len(parts) > 1:
            blocklist_map[(r,g,b)] = (parts[0].strip(), parts[1].strip())

# Get color sheet colors
sheet_colors = set(tuple(c['rgb']) for c in color_sheet)
sheet_map = {tuple(c['rgb']): c['label'] for c in color_sheet}

print(f'Color sheet has {len(sheet_colors)} unique colors')
print(f'Blocklist has {len(blocklist_colors)} colors defined')

# Find colors in sheet but not in blocklist
missing = sheet_colors - blocklist_colors
print(f'\nColors in COLOR SHEET but NOT in blocklist: {len(missing)}')

# Find WRONG mappings - colors that exist in both but might have wrong block types
print('\n=== POTENTIAL MISMATCHES (color in both, check labels) ===')
conflicts = []
for rgb in sheet_colors & blocklist_colors:
    sheet_label = sheet_map.get(rgb, 'unknown')
    bl_name, bl_block = blocklist_map.get(rgb, ('unknown', 'unknown'))
    conflicts.append((rgb, sheet_label, bl_name, bl_block))

# Show some interesting conflicts
for rgb, sheet_label, bl_name, bl_block in conflicts[:30]:
    print(f'  RGB{rgb}: Sheet={sheet_label[:30]:30} | BL={bl_name[:20]:20} -> {bl_block}')

# Find all thatch-related in sheet that are missing
print('\n=== MISSING THATCH MAPPINGS ===')
for c in color_sheet:
    rgb = tuple(c['rgb'])
    if rgb in missing and 'thatch' in c['label'].lower():
        print(f'  RGB{rgb}: {c["label"]}')

# Find all timber/frame related missing
print('\n=== MISSING TIMBERFRAME MAPPINGS ===')  
for c in color_sheet:
    rgb = tuple(c['rgb'])
    if rgb in missing and ('timber' in c['label'].lower() or 'frame' in c['label'].lower()):
        print(f'  RGB{rgb}: {c["label"]}')
