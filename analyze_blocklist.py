"""
Cross-reference blocklist.txt entries with MetadataTranslator support.
Identifies which minecraft: blocks use numeric metadata and checks if they're handled.
"""

import re
from pathlib import Path
from collections import defaultdict

# Blocks known to be handled by MetadataTranslator
HANDLED_BLOCKS = {
    # Stone variants
    "minecraft:stone",
    "minecraft:stonebrick",
    "minecraft:sandstone",
    "minecraft:red_sandstone",
    
    # Wood types
    "minecraft:planks",
    "minecraft:log",
    "minecraft:log2",
    "minecraft:sapling",
    "minecraft:leaves",
    "minecraft:leaves2",
    
    # Slabs
    "minecraft:stone_slab",
    "minecraft:wooden_slab",
    "minecraft:stone_slab2",
    "minecraft:purpur_slab",
    "minecraft:double_stone_slab",
    
    # Stairs (handled generically)
    "minecraft:oak_stairs",
    "minecraft:stone_stairs",
    "minecraft:brick_stairs",
    "minecraft:stone_brick_stairs",
    "minecraft:nether_brick_stairs",
    "minecraft:sandstone_stairs",
    "minecraft:spruce_stairs",
    "minecraft:birch_stairs",
    "minecraft:jungle_stairs",
    "minecraft:quartz_stairs",
    "minecraft:acacia_stairs",
    "minecraft:dark_oak_stairs",
    "minecraft:red_sandstone_stairs",
    "minecraft:purpur_stairs",
    
    # Doors
    "minecraft:wooden_door",
    "minecraft:iron_door",
    "minecraft:spruce_door",
    "minecraft:birch_door",
    "minecraft:jungle_door",
    "minecraft:acacia_door",
    "minecraft:dark_oak_door",
    "minecraft:oak_door",
    
    # Torches
    "minecraft:torch",
    
    # Attachments
    "minecraft:lever",
    "minecraft:ladder",
    "minecraft:vine",
    
    # Beds
    "minecraft:bed",
    
    # Colored blocks
    "minecraft:wool",
    "minecraft:stained_hardened_clay",
    "minecraft:stained_glass",
    "minecraft:stained_glass_pane",
    "minecraft:carpet",
    "minecraft:concrete",
    "minecraft:concrete_powder",
    
    # Fences
    "minecraft:fence",
    "minecraft:nether_brick_fence",
    
    # Directional
    "minecraft:pumpkin",
    "minecraft:lit_pumpkin",
    "minecraft:furnace",
    "minecraft:lit_furnace",
    "minecraft:dispenser",
    "minecraft:dropper",
    
    # Plants
    "minecraft:double_plant",
    "minecraft:red_flower",
    "minecraft:yellow_flower",
    "minecraft:tallgrass",
    
    # Redstone
    "minecraft:powered_repeater",
    "minecraft:unpowered_repeater",
    "minecraft:powered_comparator",
    "minecraft:unpowered_comparator",
    
    # Crops
    "minecraft:wheat",
    "minecraft:carrots",
    "minecraft:potatoes",
    "minecraft:beetroots",
    "minecraft:nether_wart",
    
    # Misc
    "minecraft:anvil",
    "minecraft:cauldron",
    "minecraft:farmland",
    "minecraft:cake",
    "minecraft:rail",
    "minecraft:golden_rail",
    "minecraft:detector_rail",
    "minecraft:activator_rail",
    "minecraft:hay_block",
    "minecraft:bone_block",
    "minecraft:trapdoor",
    "minecraft:standing_sign",
    "minecraft:end_rod",
    "minecraft:gravel",
    
    # Simple renames
    "minecraft:brick_block",
    "minecraft:hardened_clay",
    "minecraft:melon_block",
    "minecraft:noteblock",
    "minecraft:waterlily",
    "minecraft:fence_gate",
    "minecraft:lit_redstone_lamp",
    
    # === NEWLY ADDED ===
    # Sand and dirt variants
    "minecraft:sand",
    "minecraft:dirt",
    
    # Wood fences
    "minecraft:spruce_fence",
    "minecraft:birch_fence",
    "minecraft:jungle_fence",
    "minecraft:acacia_fence",
    "minecraft:dark_oak_fence",
    
    # Simple blocks
    "minecraft:nether_brick",
    "minecraft:purpur_block",
    "minecraft:sea_lantern",
    "minecraft:redstone_block",
    "minecraft:blue_terracotta",
    "minecraft:flower_pot",
}

# Blocks that exist in modern MC and don't need translation
MODERN_BLOCKS = {
    "minecraft:air",
    "minecraft:water",
    "minecraft:cobblestone",
    "minecraft:glass",
    "minecraft:glass_pane",
    "minecraft:lapis_block",
    "minecraft:mossy_cobblestone",
    "minecraft:obsidian",
    "minecraft:crafting_table",
    "minecraft:diamond_block",
    "minecraft:emerald_block",
    "minecraft:gold_block",
    "minecraft:clay",
    "minecraft:bookshelf",
    "minecraft:iron_block",
    "minecraft:iron_bars",
    "minecraft:ice",
    "minecraft:snow",
    "minecraft:flowing_lava",
    "minecraft:lava",
    "minecraft:netherrack",
    "minecraft:tnt",
    "minecraft:bedrock",
    "minecraft:gold_ore",
    "minecraft:iron_ore",
    "minecraft:coal_ore",
    "minecraft:sponge",
    "minecraft:lapis_ore",
    "minecraft:diamond_ore",
    "minecraft:redstone_ore",
    "minecraft:redstone_wire",
    "minecraft:redstone_torch",
    "minecraft:stone_pressure_plate",
    "minecraft:wooden_pressure_plate",
    "minecraft:heavy_weighted_pressure_plate",
    "minecraft:stone_button",
    "minecraft:snow_layer",
    "minecraft:jukebox",
    "minecraft:soul_sand",
    "minecraft:glowstone",
    "minecraft:brown_mushroom_block",
    "minecraft:red_mushroom_block",
    "minecraft:brown_mushroom",
    "minecraft:red_mushroom",
    "minecraft:redstone_lamp",
    "minecraft:cobblestone_wall",
    "minecraft:portal",
    "minecraft:chest",
}

def parse_blocklist(filepath):
    """Parse blocklist.txt and extract minecraft: entries with numeric metadata."""
    entries = []
    
    with open(filepath, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            
            # Skip empty lines and comments
            if not line or line.startswith('//'):
                continue
            
            parts = line.split(';')
            if len(parts) < 2:
                continue
            
            name = parts[0]
            block_id = parts[1]
            metadata = parts[2] if len(parts) > 2 else "0"
            
            # Only interested in minecraft: blocks
            if not block_id.startswith('minecraft:'):
                continue
            
            entries.append({
                'line': line_num,
                'name': name,
                'block_id': block_id,
                'metadata': metadata,
                'raw': line
            })
    
    return entries

def analyze_entries(entries):
    """Analyze which entries might need translation work."""
    
    results = {
        'handled': [],
        'modern': [],
        'property_based': [],
        'needs_check': [],
    }
    
    for entry in entries:
        block_id = entry['block_id']
        metadata = entry['metadata']
        
        # Property-based entries (like variant=oak) are handled by parsePropertiesFormat
        if '=' in metadata:
            results['property_based'].append(entry)
            continue
        
        # Check if block is handled by MetadataTranslator
        if block_id in HANDLED_BLOCKS:
            results['handled'].append(entry)
            continue
        
        # Check if block exists in modern MC
        if block_id in MODERN_BLOCKS:
            results['modern'].append(entry)
            continue
        
        # Everything else needs checking
        results['needs_check'].append(entry)
    
    return results

def main():
    blocklist_path = Path("src/main/resources/assets/millenaire/blocklist.txt")
    
    if not blocklist_path.exists():
        print(f"Error: {blocklist_path} not found")
        return
    
    print("=" * 60)
    print("Blocklist.txt Analysis for MetadataTranslator Gaps")
    print("=" * 60)
    
    entries = parse_blocklist(blocklist_path)
    print(f"\nTotal minecraft: entries found: {len(entries)}")
    
    results = analyze_entries(entries)
    
    print(f"\n[OK] Handled by MetadataTranslator: {len(results['handled'])}")
    print(f"[OK] Modern blocks (no translation needed): {len(results['modern'])}")
    print(f"[OK] Property-based (handled by parsePropertiesFormat): {len(results['property_based'])}")
    print(f"[!!] Needs review: {len(results['needs_check'])}")
    
    if results['needs_check']:
        print("\n" + "=" * 60)
        print("BLOCKS THAT MAY NEED TRANSLATION:")
        print("=" * 60)
        
        # Group by block ID
        by_block = defaultdict(list)
        for entry in results['needs_check']:
            by_block[entry['block_id']].append(entry)
        
        for block_id, block_entries in sorted(by_block.items()):
            print(f"\n{block_id}:")
            for e in block_entries[:5]:  # Show first 5 examples
                print(f"  Line {e['line']}: {e['name']} (meta: {e['metadata']})")
            if len(block_entries) > 5:
                print(f"  ... and {len(block_entries) - 5} more")
    
    # Generate summary for adding to MetadataTranslator
    print("\n" + "=" * 60)
    print("SUGGESTED ADDITIONS TO MetadataTranslator:")
    print("=" * 60)
    
    unique_blocks = set(e['block_id'] for e in results['needs_check'])
    for block_id in sorted(unique_blocks):
        print(f"  - {block_id}")

if __name__ == "__main__":
    main()
