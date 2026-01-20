"""
Analyze the alchemy workshop PNG to find what colors are used
on floors 1+ (the walls), not floor 0 (the foundation).
"""
from PIL import Image
from collections import Counter

# Load the alchemy workshop image
img = Image.open(r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\buildings\norman\buildings\houses\alchemyworkshop_A0.png")
img = img.convert('RGB')

print(f"Image size: {img.width}x{img.height}")
print()

# From the log:
# - Floor 0: columns 0-11
# - Floor 1: columns 13-24
# - Floor 2: columns 26-37
# etc.

# Let's analyze floor 1 specifically (the first level of walls)
floor1_start = 13
floor1_end = 24

print("=== FLOOR 1 COLORS (Wall Level) ===")
floor1_colors = Counter()
for y in range(img.height):
    for x in range(floor1_start, floor1_end + 1):
        if x < img.width:
            pixel = img.getpixel((x, y))
            floor1_colors[pixel] += 1

for color, count in floor1_colors.most_common(20):
    r, g, b = color
    print(f"RGB({r},{g},{b}) - Count: {count}")

# Also check floor 2
print()
print("=== FLOOR 2 COLORS ===")
floor2_start = 26
floor2_end = 37
floor2_colors = Counter()
for y in range(img.height):
    for x in range(floor2_start, floor2_end + 1):
        if x < img.width:
            pixel = img.getpixel((x, y))
            floor2_colors[pixel] += 1

for color, count in floor2_colors.most_common(10):
    r, g, b = color
    print(f"RGB({r},{g},{b}) - Count: {count}")

# Most likely thatch color candidates
print()
print("=== THATCH COLOR CANDIDATES ===")
thatch_colors = [
    (166, 156, 137),  # known thatch
    (200, 200, 120),  # old thatch mapping
    (255, 194, 0),    # current thatch
]
for tc in thatch_colors:
    if tc in floor1_colors:
        print(f"RGB{tc} found on floor 1: {floor1_colors[tc]} pixels")
    elif tc in floor2_colors:
        print(f"RGB{tc} found on floor 2: {floor2_colors[tc]} pixels")
