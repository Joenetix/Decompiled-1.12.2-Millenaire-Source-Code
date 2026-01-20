from PIL import Image
import sys

# Load the image
img_path = r"C:\Millenaire Revived v0\src\main\resources\assets\millenaire\buildings\japanese\buildings\core\japanesepeasanth_A0.png"
img = Image.open(img_path)

# Get unique colors
colors = set()
width, height = img.size

for y in range(height):
    for x in range(width):
        r, g, b, *rest = img.getpixel((x, y))
        colors.add((r, g, b))

# Print colors sorted
print(f"Found {len(colors)} unique RGB colors in the image:")
for color in sorted(colors):
    r, g, b = color
    print(f"RGB({r},{g},{b})")
    
# Look specifically for colors that might be thatch (around 200,200,xxx)
print("\n\nColors with RGB ~200,200,xxx:")
for color in sorted(colors):
    r, g, b = color
    if 195 <= r <= 205 and 195 <= g <= 205:
        print(f" RGB({r},{g},{b}) - POTENTIAL THATCH COLOR")
