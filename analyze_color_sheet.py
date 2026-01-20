"""
Generate color mappings from the extracted color sheet.
Each entry has: rgb color + label describing what block it should be.
"""
import json

with open(r'C:\Users\Joey\.gemini\antigravity\brain\c3cbe1bf-7148-4296-a87c-a6b08dd35e77\color_sheet_with_labels.json') as f:
    colors = json.load(f)

print(f"Total colors in color sheet: {len(colors)}")
print("\n=== SAMPLE OF COLOR SHEET ENTRIES ===")
for c in colors[:50]:
    rgb = tuple(c['rgb'])
    label = c['label']
    print(f"RGB{rgb} = {label}")

print("\n=== CATEGORIZING BY BLOCK TYPE ===")

# Group colors by block category based on label keywords
categories = {
    'thatch': [],
    'timber': [],
    'brick': [],
    'stone': [],
    'wood': [],
    'planks': [],
    'stairs': [],
    'slab': [],
    'path': [],
    'door': [],
    'fence': [],
    'banner': [],
    'torch': [],
    'bed': [],
    'chest': [],
    'sign': [],
    'carpet': [],
    'terracotta': [],
    'glass': [],
    'wool': [],
    'mud': [],
    'sandstone': []
}

for c in colors:
    label = c['label'].lower()
    rgb = tuple(c['rgb'])
    for cat in categories:
        if cat in label:
            categories[cat].append((rgb, c['label']))
            break

for cat, items in categories.items():
    if items:
        print(f"\n{cat.upper()} ({len(items)} colors):")
        for rgb, label in items[:5]:
            print(f"  RGB{rgb} = {label}")
        if len(items) > 5:
            print(f"  ... and {len(items)-5} more")
