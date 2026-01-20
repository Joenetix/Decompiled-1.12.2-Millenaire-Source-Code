import json

with open(r'C:\Users\Joey\.gemini\antigravity\brain\c3cbe1bf-7148-4296-a87c-a6b08dd35e77\color_sheet_with_labels.json') as f:
    colors = json.load(f)

# Find mud/brick related
print('=== MUD/BRICK-related ===')
for c in colors:
    label = c['label'].lower()
    if 'mud' in label or 'brick' in label or 'cooked' in label:
        rgb = tuple(c['rgb'])
        print(f'  {rgb}: {c["label"]}')

print('\n=== WALL-related ===')
for c in colors:
    label = c['label'].lower()
    if 'wall' in label:
        rgb = tuple(c['rgb'])
        print(f'  {rgb}: {c["label"]}')

print('\n=== PATH-related ===')
for c in colors:
    label = c['label'].lower()
    if 'path' in label:
        rgb = tuple(c['rgb'])
        print(f'  {rgb}: {c["label"]}')
