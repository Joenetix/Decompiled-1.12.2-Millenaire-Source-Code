from PIL import Image

# Load the kitchen PNG
img_path = r"c:\Millenaire Revived v0\research2\extracted\todeploy\millenaire\cultures\japanese\buildings\core\japanesekitchen_A0.png"
img = Image.open(img_path)
pixels = img.load()

# Find all occurrences of RGB(255,0,0)
red_positions = []
for x in range(img.width):
    for y in range(img.height):
        r, g, b = pixels[x, y][:3]
        if (r, g, b) == (255, 0, 0):
            red_positions.append((x, y))

print(f"Found {len(red_positions)} pixels with RGB(255,0,0)")
if red_positions:
    # Determine which floors
    floor_map = {}
    for x, y in red_positions[:20]:
        floor_map[(x, y)] = "position"
    
    for x, y in sorted(red_positions[:20]):
        print(f"  x={x}, y={y}")
