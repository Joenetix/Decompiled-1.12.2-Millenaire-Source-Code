import json

with open(r'C:\Users\Joey\.gemini\antigravity\brain\c3cbe1bf-7148-4296-a87c-a6b08dd35e77\color_sheet_with_labels.json') as f:
    colors = json.load(f)

# Check unknown colors from the log
check = [
    (160, 0, 0),
    (220, 220, 12),
    (100, 10, 62),
    (25, 25, 25),
]

print("Unknown colors from game logs - checking color sheet:")
for rgb in check:
    matches = [c for c in colors if tuple(c['rgb']) == rgb]
    if matches:
        label = matches[0]['label']
        print(f"  RGB{rgb} = '{label}'")
    else:
        print(f"  RGB{rgb} = NOT FOUND in color sheet")
