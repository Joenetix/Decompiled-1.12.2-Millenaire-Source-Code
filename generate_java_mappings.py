"""
Generate Java color mappings for MillenaireBuildingParser.java
Based on the extracted color sheet labels.
"""
import json

with open(r'C:\Users\Joey\.gemini\antigravity\brain\c3cbe1bf-7148-4296-a87c-a6b08dd35e77\color_sheet_with_labels.json') as f:
    colors = json.load(f)

# Create mapping rules based on label patterns
# Each rule: (label_pattern, minecraft_block)
mapping_rules = [
    # Basic blocks
    ('empty', 'Blocks.AIR'),
    ('preserveground', None),  # Special marker, not a block
    ('allbutrees', None),
    ('grass', 'Blocks.GRASS_BLOCK'),
    
    # Thatch
    ('thatch', 'MillBlocks.WOOD_DECORATION.get()'),
    
    # Stone variants
    ('stonebrick', 'Blocks.STONE_BRICKS'),
    ('crackedstonebrick', 'Blocks.CRACKED_STONE_BRICKS'),
    ('mossystonebrick', 'Blocks.MOSSY_STONE_BRICKS'),
    ('cobblestone', 'Blocks.COBBLESTONE'),
    ('stone', 'Blocks.STONE'),
    
    # Wood
    ('planks oak', 'Blocks.OAK_PLANKS'),
    ('planks pine', 'Blocks.SPRUCE_PLANKS'),
    ('planks spruce', 'Blocks.SPRUCE_PLANKS'),
    ('planks birch', 'Blocks.BIRCH_PLANKS'),
    ('planks jungle', 'Blocks.JUNGLE_PLANKS'),
    ('planks acacia', 'Blocks.ACACIA_PLANKS'),
    ('planks dark', 'Blocks.DARK_OAK_PLANKS'),
    
    # Logs
    ('log oak', 'Blocks.OAK_LOG'),
    ('log pine', 'Blocks.SPRUCE_LOG'),
    ('log spruce', 'Blocks.SPRUCE_LOG'),
    ('log birch', 'Blocks.BIRCH_LOG'),
    ('log jungle', 'Blocks.JUNGLE_LOG'),
    
    # Torches
    ('torch', 'Blocks.TORCH'),
    
    # Chests
    ('chest', 'Blocks.CHEST'),
    ('lockedchest', 'Blocks.CHEST'),  # No locked chest in modern MC
    ('mainchest', 'Blocks.CHEST'),
    
    # Doors (simplified)
    ('door', 'Blocks.OAK_DOOR'),
    
    # Fences
    ('fence gate', 'Blocks.OAK_FENCE_GATE'),
    ('fence', 'Blocks.OAK_FENCE'),
    
    # Wool
    ('white wool', 'Blocks.WHITE_WOOL'),
    ('wool', 'Blocks.WHITE_WOOL'),
    
    # Glass
    ('glass', 'Blocks.GLASS'),
    
    # Beds
    ('bedtop', 'Blocks.RED_BED'),
    ('bedbottom', 'Blocks.RED_BED'),
    
    # Carpets
    ('carpet white', 'Blocks.WHITE_CARPET'),
    ('carpet', 'Blocks.WHITE_CARPET'),
    
    # Banners
    ('banner', 'Blocks.WHITE_BANNER'),
    
    # Path blocks (Millenaire custom)
    ('pathslabs', 'MillBlocks.PATHSLABS_SLAB.get()'),
    ('pathgravel', 'MillBlocks.PATHGRAVEL.get()'),
    ('pathdirt', 'MillBlocks.PATHDIRT.get()'),
    ('pathsandstone', 'MillBlocks.PATHSANDSTONE.get()'),
    ('pathsnow', 'MillBlocks.PATHSNOW.get()'),
    
    # Terracotta
    ('terracotta', 'Blocks.TERRACOTTA'),
    
    # Sandstone
    ('sandstone', 'Blocks.SANDSTONE'),
    
    # Gravel
    ('gravel', 'Blocks.GRAVEL'),
    
    # Dirt
    ('dirt', 'Blocks.DIRT'),
    
    # Bedrock
    ('bedrock', 'Blocks.BEDROCK'),
    
    # Signs (position markers, not actual signs)
    ('signpos', None),
    ('signg', None),
    
    # Special positions (not blocks)
    ('pos', None),
    ('soil', None),
    ('source', None),
    ('spawn', None),
    ('spot', None),
]

print("=== GENERATING JAVA MAPPINGS ===")
print()

# Find colors that need specific mappings
generated = []
for c in colors:
    rgb = tuple(c['rgb'])
    label = c['label'].lower().replace(' ', '')
    
    # Skip header
    if 'generatedcoloursheet' in label:
        continue
    
    # Try to match a rule
    for pattern, block in mapping_rules:
        pattern_clean = pattern.replace(' ', '')
        if pattern_clean in label:
            if block:
                java_line = f"colorMap.put(rgb({rgb[0]}, {rgb[1]}, {rgb[2]}), {block}.defaultBlockState()); // {c['label']}"
                generated.append(java_line)
            break

print(f"Generated {len(generated)} mappings\n")
print("// Sample mappings (first 30):")
for line in generated[:30]:
    print(line)

# Save to file
with open('generated_color_mappings.txt', 'w') as f:
    f.write("// Auto-generated color mappings from color sheet\n")
    f.write("// Add these to MillenaireBuildingParser.initializeColorMappings()\n\n")
    for line in generated:
        f.write(line + "\n")

print(f"\nSaved all {len(generated)} mappings to generated_color_mappings.txt")
