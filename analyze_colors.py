from PIL import Image
import collections

# Analyze Japanese Inn
print("=" * 60)
print("JAPANESE INN ANALYSIS")
print("=" * 60)

img = Image.open(r'src\main\resources\assets\millenaire\buildings\japanese\buildings\core\japaneseinn_A0.png')
pixels = img.load()

print(f'Image size: {img.size} (179x13 = 17W x 13D, with 10 floors + separators)')

# Get all unique colors
colors = collections.Counter()
for y in range(img.height):
    for x in range(img.width):
        rgb = pixels[x,y][:3]
        colors[rgb] += 1

print(f'\nAll unique colors:')
for rgb, count in colors.most_common():
    print(f'  RGB{rgb}: {count} pixels')

# Analyze the unknown color from logs
target = (205, 205, 205)
positions = [(x,y) for y in range(img.height) for x in range(img.width) if pixels[x,y][:3] == target]
print(f'\n\nUNKNOWN COLOR: RGB{target}')
print(f'  Count: {len(positions)} pixels')
print(f'  Positions: {positions}')
print(f'  Analysis: Located at floor 0, columns 0-2, rows 7-9')
print(f'  Context: Bottom-left corner of first floor')

# Analyze fort
print("\n\n" + "=" * 60)
print("JAPANESE FORT ANALYSIS")  
print("=" * 60)

img2 = Image.open(r'src\main\resources\assets\millenaire\buildings\japanese\buildings\townhalls\japanesefort_A0.png')
pixels2 = img2.load()

print(f'Image size: {img2.size}')

# Get all unique colors
colors2 = collections.Counter()
for y in range(img2.height):
    for x in range(img2.width):
        rgb = pixels2[x,y][:3]
        colors2[rgb] += 1

print(f'\nAll unique colors (top 40):')
for rgb, count in colors2.most_common(40):
    print(f'  RGB{rgb}: {count} pixels')

# Analyze the unknown color from logs
positions2 = [(x,y) for y in range(img2.height) for x in range(img2.width) if pixels2[x,y][:3] == target]
print(f'\n\nUNKNOWN COLOR: RGB{target}')
print(f'  Count: {len(positions2)} pixels')
if positions2:
    print(f'  Sample positions: {positions2[:10]}')
