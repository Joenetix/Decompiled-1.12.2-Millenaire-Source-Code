"""
Rotate facing directions for ALL directional blocks in blocklist.txt by 90 degrees clockwise.
This includes: beds, chests, furnaces, torches, ladders, doors, fence_gates, signs, anvils, etc.

Rotation: west -> north -> east -> south -> west
"""
import re

blocklist_path = r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\blocklist.txt"

with open(blocklist_path, 'r', encoding='utf-8') as f:
    content = f.read()

original_content = content

# Block types that need facing rotation (NOT stairs since already done)
# Match any line with facing= that is NOT a stair
# We'll use a more targeted approach - rotate ALL facing= values that aren't already rotated

# Count lines with facing before
lines_with_facing_before = len([l for l in content.split('\n') if 'facing=' in l])

# Step 1: Replace facing values with placeholders (avoiding stairs which were already done)
# We need to match facing= followed by direction, but NOT in stair lines

# Actually, let's just rotate everything that has facing= and isn't a stair
# Split by lines and process each
lines = content.split('\n')
new_lines = []

rotation_map = {
    'west': 'north',
    'north': 'east', 
    'east': 'south',
    'south': 'west'
}

blocks_to_rotate = [
    'chest', 'furnace', 'torch', 'ladder', 'door', 'fence_gate', 
    'sign', 'anvil', 'bed', 'glazed_terracotta', 'end_rod',
    'pumpkin', 'jack_o_lantern', 'observer', 'dispenser', 'dropper',
    'hopper', 'brewing_stand', 'lectern', 'grindstone', 'stonecutter',
    'loom', 'barrel', 'smoker', 'blast_furnace', 'campfire',
    'beehive', 'bee_nest', 'respawn_anchor', 'bell'
]

rotated_count = 0

for line in lines:
    # Skip stairs (already rotated)
    if 'stairs' in line.lower():
        new_lines.append(line)
        continue
    
    # Check if this line should be rotated
    should_rotate = False
    for block_type in blocks_to_rotate:
        if block_type in line.lower() and 'facing=' in line:
            should_rotate = True
            break
    
    if should_rotate:
        # Rotate the facing value
        new_line = line
        for old_dir, new_dir in [('facing=west', 'facing=__NORTH__'),
                                   ('facing=north', 'facing=__EAST__'),
                                   ('facing=east', 'facing=__SOUTH__'),
                                   ('facing=south', 'facing=__WEST__')]:
            if old_dir in new_line:
                new_line = new_line.replace(old_dir, new_dir)
                rotated_count += 1
                break
        
        # Replace placeholders with final values
        new_line = new_line.replace('__NORTH__', 'north')
        new_line = new_line.replace('__EAST__', 'east')
        new_line = new_line.replace('__SOUTH__', 'south')
        new_line = new_line.replace('__WEST__', 'west')
        
        new_lines.append(new_line)
    else:
        new_lines.append(line)

content = '\n'.join(new_lines)

# Show stats
print(f"Lines with 'facing=' before: {lines_with_facing_before}")
print(f"Rotated {rotated_count} facing values (excluding stairs)")

# Show sample changes
sample_count = 0
for i, (old, new) in enumerate(zip(original_content.split('\n'), content.split('\n'))):
    if old != new:
        print(f"\nLine {i+1}:")
        print(f"  OLD: {old[:80]}...")
        print(f"  NEW: {new[:80]}...")
        sample_count += 1
        if sample_count >= 10:
            break

with open(blocklist_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"\nUpdated blocklist.txt with rotated facings for non-stair blocks")
