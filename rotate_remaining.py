"""
Rotate facing directions for remaining blocks: beds, fences, banners, signs
that weren't caught by the previous script.
"""
import re

blocklist_path = r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\blocklist.txt"

with open(blocklist_path, 'r', encoding='utf-8') as f:
    content = f.read()

original_content = content

# Block types to check - be more inclusive this time
blocks_to_rotate = [
    'bed', 'fence', 'banner', 'sign', 'panel',  # panel is millenaire's sign
    'wall_sign', 'standing_sign', 'skull', 'head', 'piston',
    'comparator', 'repeater', 'trapdoor', 'button', 'lever',
    'tripwire', 'cocoa', 'frame', 'shulker_box'
]

lines = content.split('\n')
new_lines = []
rotated_count = 0

for line in lines:
    # Skip stairs (already rotated)
    if 'stairs' in line.lower():
        new_lines.append(line)
        continue
    
    # Check if this line should be rotated
    should_rotate = False
    line_lower = line.lower()
    
    # Check for any of our target blocks
    for block_type in blocks_to_rotate:
        if block_type in line_lower and 'facing=' in line:
            should_rotate = True
            break
    
    if should_rotate:
        # Check if already rotated (if it has the expected directional pattern)
        new_line = line
        rotated = False
        
        for old_dir, new_dir in [('facing=west', 'facing=__NORTH__'),
                                   ('facing=north', 'facing=__EAST__'),
                                   ('facing=east', 'facing=__SOUTH__'),
                                   ('facing=south', 'facing=__WEST__')]:
            if old_dir in new_line:
                new_line = new_line.replace(old_dir, new_dir)
                rotated = True
                break
        
        if rotated:
            rotated_count += 1
            # Replace placeholders with final values
            new_line = new_line.replace('__NORTH__', 'north')
            new_line = new_line.replace('__EAST__', 'east')
            new_line = new_line.replace('__SOUTH__', 'south')
            new_line = new_line.replace('__WEST__', 'west')
        
        new_lines.append(new_line)
    else:
        new_lines.append(line)

content = '\n'.join(new_lines)

print(f"Rotated {rotated_count} additional facing values")

# Show sample changes
sample_count = 0
for i, (old, new) in enumerate(zip(original_content.split('\n'), content.split('\n'))):
    if old != new:
        print(f"\nLine {i+1}:")
        print(f"  OLD: {old[:90]}...")
        print(f"  NEW: {new[:90]}...")
        sample_count += 1
        if sample_count >= 15:
            print("\n... and more")
            break

if rotated_count == 0:
    print("\nNo additional blocks found to rotate. Checking what's in blocklist...")
    # Search for bed/fence/banner/sign entries
    for i, line in enumerate(original_content.split('\n')):
        line_lower = line.lower()
        if any(b in line_lower for b in ['bed', 'fence', 'banner', 'sign', 'panel']):
            if 'facing' in line_lower or 'variant' in line_lower:
                print(f"  Line {i+1}: {line[:100]}")

with open(blocklist_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"\nDone! Updated blocklist.txt")
