"""
Rotate all stair facing directions in blocklist.txt by 90 degrees clockwise.
- west → north
- north → east  
- east → south
- south → west
"""
import re

blocklist_path = r"c:\Millenaire Revived v0\src\main\resources\assets\millenaire\blocklist.txt"

with open(blocklist_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Count replacements
original_content = content

# For stairs, rotate facing by 90 degrees clockwise
# We need to be careful not to replace already-replaced values, so use placeholder approach

# Step 1: Replace with placeholders
content = re.sub(r'(stairs[^;]*;facing=)west', r'\1__NORTH__', content)
content = re.sub(r'(stairs[^;]*;facing=)north', r'\1__EAST__', content)
content = re.sub(r'(stairs[^;]*;facing=)east', r'\1__SOUTH__', content)
content = re.sub(r'(stairs[^;]*;facing=)south', r'\1__WEST__', content)

# Step 2: Replace placeholders with final values
content = content.replace('__NORTH__', 'north')
content = content.replace('__EAST__', 'east')
content = content.replace('__SOUTH__', 'south')
content = content.replace('__WEST__', 'west')

# Count changes
changes = sum(1 for a, b in zip(original_content, content) if a != b)
stair_lines_before = len([l for l in original_content.split('\n') if 'stairs' in l.lower() and 'facing=' in l])
stair_lines_after = len([l for l in content.split('\n') if 'stairs' in l.lower() and 'facing=' in l])

print(f"Stair entries with facing: {stair_lines_before}")
print(f"Content changed: {content != original_content}")

# Show sample changes
lines_orig = original_content.split('\n')
lines_new = content.split('\n')
print("\nSample changes (first 5):")
count = 0
for i, (old, new) in enumerate(zip(lines_orig, lines_new)):
    if old != new and 'stairs' in old.lower():
        print(f"  Line {i+1}:")
        print(f"    OLD: {old[:100]}...")
        print(f"    NEW: {new[:100]}...")
        count += 1
        if count >= 5:
            break

with open(blocklist_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"\n✅ Updated blocklist.txt with rotated stair facings")
