from PIL import Image

# Load the Japanese fort PNG
img_path = r"c:\Millenaire Revived v0\research2\extracted\todeploy\millenaire\cultures\japanese\buildings\townhalls\japanesefort_A0.png"
img = Image.open(img_path)
pixels = img.load()

# Check for RGB(255,50,53) specifically
target_color = (255, 50, 53)
found_positions = []

for x in range(img.width):
    for y in range(img.height):
        r, g, b = pixels[x, y][:3]
        if (r, g, b) == target_color:
            # Determine which floor this is
            if x < 37:
                floor = 0
            elif x < 75:
                floor = "separator"
            elif x < 75 + 38:
                floor = 1
            else:
                floor = f"column {x}"
            found_positions.append((x, y, floor))

print(f"Searching for RGB(255,50,53)...")
if found_positions:
    print(f"Found {len(found_positions)} pixels with RGB(255,50,53):")
    for x, y, floor in found_positions[:20]:
        print(f"  x={x}, y={y}, floor={floor}")
    if len(found_positions) > 20:
        print(f"  ... and {len(found_positions)-20} more")
else:
    print("RGB(255,50,53) NOT FOUND in the image")

# Also check for RGB(255,50,61) for comparison
target_color2 = (255, 50, 61)
found_positions2 = []
for x in range(img.width):
    for y in range(img.height):
        r, g, b = pixels[x, y][:3]
        if (r, g, b) == target_color2:
            found_positions2.append((x, y))

print(f"\nFor comparison, RGB(255,50,61) (doubleGrass): {len(found_positions2)} pixels found")
