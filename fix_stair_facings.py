"""
Fix stair facing directions in blocklist.txt to match original Millénaire.
Current (wrong): Top=north, Bottom=south, Left=west, Right=east  
Original: Top=west, Bottom=east, Left=south, Right=north

Rotation needed: 90° COUNTER-clockwise
north -> west  
east -> north
south -> east
west -> south
"""
import re

blocklist_path = r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\blocklist.txt"

with open(blocklist_path, 'r', encoding='utf-8') as f:
    content = f.read()

original_content = content

# Split by lines and process each
lines = content.split('\n')
new_lines = []

# CCW rotation mapping
rotation_map_ccw = {
    'north': 'west',
    'east': 'north', 
    'south': 'east',
    'west': 'south'
}

rotated_count = 0

for line in lines:
    # Only process stair lines
    if 'stairs' in line.lower() and 'facing=' in line:
        # Rotate the facing value CCW
        new_line = line
        for old_dir, new_dir in [('facing=north', 'facing=__WEST__'),
                                   ('facing=east', 'facing=__NORTH__'),
                                   ('facing=south', 'facing=__EAST__'),
                                   ('facing=west', 'facing=__SOUTH__')]:
            if old_dir in new_line:
                new_line = new_line.replace(old_dir, new_dir)
                rotated_count += 1
                break
        
        # Replace placeholders with final values
        new_line = new_line.replace('__WEST__', 'west')
        new_line = new_line.replace('__NORTH__', 'north')
        new_line = new_line.replace('__SOUTH__', 'south')
        new_line = new_line.replace('__EAST__', 'east')
        
        new_lines.append(new_line)
    else:
        new_lines.append(line)

content = '\n'.join(new_lines)

print(f"Rotated {rotated_count} stair facing values Counter-Clockwise")

# Show sample changes
sample_count = 0
for i, (old, new) in enumerate(zip(original_content.split('\n'), content.split('\n'))):
    if old != new:
        print(f"\nLine {i+1}:")
        print(f"  OLD: {old[:90]}...")
        print(f"  NEW: {new[:90]}...")
        sample_count += 1
        if sample_count >= 8:
            break

with open(blocklist_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"\nUpdated blocklist.txt with fixed stair facings")
