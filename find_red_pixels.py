from PIL import Image
import sys

# Load the Japanese fort PNG
img_path = r"c:\Millenaire Revived v0\research2\extracted\todeploy\millenaire\cultures\japanese\buildings\townhalls\japanesefort_A0.png"
img = Image.open(img_path)
pixels = img.load()

# Floor 0 is columns 0-36 (first floor)
# Look for any reddish pixels (high R value)
red_colors = {}
for x in range(0, 37):  # Floor 0 columns
    for y in range(img.height):
        r, g, b = pixels[x, y][:3]  # Get RGB, ignore alpha
        
        # Look for reddish colors (R > 150 and R > G and R > B)
        if r > 150 and r > g and r > b:
            color_key = f"RGB({r},{g},{b})"
            if color_key not in red_colors:
                red_colors[color_key] = []
            red_colors[color_key].append((x, y))

print(f"Found {len(red_colors)} reddish colors in floor 0:")
for color, positions in sorted(red_colors.items()):
    print(f"\n{color}: {len(positions)} pixels")
    # Show first few positions
    for pos in positions[:5]:
        print(f"  Position: x={pos[0]}, y={pos[1]}")
    if len(positions) > 5:
        print(f"  ... and {len(positions)-5} more")
