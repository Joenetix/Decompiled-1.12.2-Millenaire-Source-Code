"""
Analyze the colors in the alchemy workshop PNG to find the color 
that should be stone bricks but is being mapped to thatch.
"""
from PIL import Image
from collections import Counter

# Load the alchemy workshop image
img = Image.open(r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\buildings\norman\buildings\houses\alchemyworkshop_A0.png")
img = img.convert('RGB')

# Count all unique colors
colors = Counter()
for y in range(img.height):
    for x in range(img.width):
        pixel = img.getpixel((x, y))
        colors[pixel] += 1

# Print all unique colors sorted by frequency
print(f"Image size: {img.width}x{img.height}")
print(f"\nUnique colors in alchemy workshop (sorted by frequency):")
print("-" * 60)
for color, count in colors.most_common():
    r, g, b = color
    print(f"RGB({r},{g},{b}) - Count: {count}")

# Known mappings to check
stone_colors = [
    (90, 90, 90),   # stonebricks
    (76, 76, 76),   # cobblestone
    (150, 150, 150), # stone
]

thatch_colors = [
    (166, 156, 137),  # thatch
    (200, 200, 120),  # was old thatch, now trapdoor
]

print("\n" + "=" * 60)
print("Checking for potential stone brick colors:")
for color in stone_colors:
    if color in colors:
        print(f"  Found {color} (count: {colors[color]})")

print("\nChecking for thatch colors:")
for color in thatch_colors:
    if color in colors:
        print(f"  Found {color} (count: {colors[color]})")
