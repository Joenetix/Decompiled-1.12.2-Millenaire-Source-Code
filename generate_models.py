"""
Python script to auto-generate blockstate and model JSONs for Millénaire mod.
This creates all necessary JSON files to make textures show up in-game.
"""

import os
import json

# Base directories
PROJECT_ROOT = r"C:\Millenaire Revived v0"
BLOCKSTATES_DIR = os.path.join(PROJECT_ROOT, r"src\main\resources\assets\millenaire\blockstates")
BLOCK_MODELS_DIR = os.path.join(PROJECT_ROOT, r"src\main\resources\assets\millenaire\models\block")
ITEM_MODELS_DIR = os.path.join(PROJECT_ROOT, r"src\main\resources\assets\millenaire\models\item")

# Ensure directories exist
os.makedirs(BLOCKSTATES_DIR, exist_ok=True)
os.makedirs(BLOCK_MODELS_DIR, exist_ok=True)
os.makedirs(ITEM_MODELS_DIR, exist_ok=True)

# Registered blocks that need blockstates and models
SIMPLE_BLOCKS = [
    # Core decorative
    ("wood_deco", "timberframeplain"),
    ("stone_deco", "mudbrick"),
    ("earth_deco", "mudbrick"),
    # Norman
    ("extended_mud_brick", "mudbrick"),
    ("wall_mud_brick", "mudbrick"),
    # Indian
    ("sandstone_carved", "sandstone_carved"),
    ("sandstone_red_carved", "sandstone_red_carved"),
    ("sandstone_ochre_carved", "sandstone_ochre_carved"),
    ("wet_brick", "wetbrick0"),
    ("silk_worm", "silkwormempty"),
    ("snail_soil", "snail_soil_empty"),
    # Japanese
    ("gray_tiles", "gray_tiles_0"),
    ("green_tiles", "green_tiles_0"),
    ("red_tiles", "red_tiles_0"),
    ("wooden_bars", "wooden_bars"),
    ("wooden_bars_dark", "wooden_bars_dark"),
    # Byzantine
    ("byzantine_tiles", "byzantine_tiles_top"),
    ("wooden_bars_indian", "wooden_bars_indian"),
    ("wooden_bars_rosette", "wooden_bars_rosette_topleft"),
    # Shared
    ("stained_glass", "stained_glass_white"),
    ("rosette", "rosette_center"),
    ("alchemistexplosive", "alchemistexplosive"),
    ("sod", "sod_overlay"),
]

# Standalone items (not BlockItems)
STANDALONE_ITEMS = [
    # Currency
    ("denier", "denier"),
    ("denierargent", "denierargent"),
    ("denieror", "denieror"),
    ("purse", "purse"),
    # Special
    ("summoningwand", "summoningwand"),
    ("negationwand", "negationwand"),
    ("vishnu_amulet", "vishnu_amulet0"),
    ("alchemist_amulet", "amulet_alchemist"),
    ("yggdrasil_amulet", "yggdrasil_amulet0"),
    ("skoll_hati_amulet", "skoll_hati_amulet"),
    # Seeds
    ("rice", "rice"),
    ("turmeric", "turmeric"),
    ("maize", "maize"),
    ("grapes", "grapes"),
    ("cotton", "cotton"),
    # Norman
    ("normanpickaxe", "normanpickaxe"),
    ("normanaxe", "normanaxe"),
    ("normanshovel", "normanshovel"),
    ("normanhoe", "normanhoe"),
    ("normanbroadsword", "normansword"),
    ("normanhelmet", "normanhelmet"),
    ("normanplate", "normanplate"),
    ("normanlegs", "normanlegs"),
    ("normanboots", "normanboots"),
    ("ciderapple", "ciderapple"),
    ("cider", "cider"),
    ("calva", "calva"),
    ("boudin", "boudin"),
    ("tripes", "tripes"),
    ("tapestry", "tapestry"),
    # Indian
    ("vegcurry", "vegcurry"),
    ("chickencurry", "chickencurry"),
    ("rasgulla", "rasgulla"),
    ("brickmould", "brickmould"),
    ("indianstatue", "indianstatue"),
    # Japanese
    ("tachisword", "tachisword"),
    ("yumibow", "yumibow_standby"),
    ("japaneseredhelmet", "japaneseredhelmet"),
    ("japaneseredplate", "japaneseredplate"),
    ("japaneseredlegs", "japaneseredlegs"),
    ("japaneseredboots", "japaneseredboots"),
    ("japanesebluehelmet", "japanesebluehelmet"),
    ("japaneseblueplate", "japaneseblueplate"),
    ("japanesebluelegs", "japanesebluelegs"),
    ("japaneseblueboots", "japaneseblueboots"),
    ("japaneseguardhelmet", "japaneseguardhelmet"),
    ("japaneseguardplate", "japaneseguardplate"),
    ("japaneseguardlegs", "japaneseguardlegs"),
    ("japaneseguardboots", "japaneseguardboots"),
    ("udon", "udon"),
    ("sake", "sake"),
    ("ikayaki", "ikayaki"),
    # Mayan
    ("mayanpickaxe", "mayanpickaxe"),
    ("mayanaxe", "mayanaxe"),
    ("mayanshovel", "mayanshovel"),
    ("mayanhoe", "mayanhoe"),
    ("mayanmace", "mayanmace"),
    ("wah", "wah"),
    ("balche", "balche"),
    ("sikilpah", "sikilpah"),
    ("masa", "masa"),
    ("cacauhaa", "cacauhaa"),
    ("obsidianflake", "obsidianflake"),
    ("mayanquestcrown", "mayanquestcrown"),
    ("mayanstatue", "mayanstatue"),
    # Byzantine
    ("byzantinepickaxe", "byzantinepickaxe"),
    ("byzantineaxe", "byzantineaxe"),
    ("byzantineshovel", "byzantineshovel"),
    ("byzantinehoe", "byzantinehoe"),
    ("byzantinemace", "byzantinemace"),
    ("byzantinehelmet", "byzantinehelmet"),
    ("byzantineplate", "byzantineplate"),
    ("byzantinelegs", "byzantinelegs"),
    ("byzantineboots", "byzantineboots"),
    ("olives", "olives"),
    ("oliveoil", "oliveoil"),
    ("winebasic", "winebasic"),
    ("winefancy", "winefancy"),
    ("feta", "feta"),
    ("souvlaki", "souvlaki"),
    ("silk", "silk"),
    ("clothes_byz_wool", "clothes_byz_wool"),
    ("clothes_byz_silk", "clothes_byz_silk"),
    ("byzantineiconsmall", "byzantineicon_small"),
    ("byzantineiconmedium", "byzantineicon_medium"),
    ("byzantineiconlarge", "byzantineicon_large"),
    # Parchments
    ("parchment_normanvillagers", "parchmentvillagers"),
    ("parchment_normanbuildings", "parchmentbuildings"),
    ("parchment_normanitems", "parchmentitems"),
    ("parchment_normanfull", "parchmentall"),
    ("parchment_indianvillagers", "parchmentvillagers"),
    ("parchment_indianbuildings", "parchmentbuildings"),
    ("parchment_indianitems", "parchmentitems"),
    ("parchment_indianfull", "parchmentall"),
    ("parchment_japanesevillagers", "parchmentvillagers"),
    ("parchment_japanesebuildings", "parchmentbuildings"),
    ("parchment_japaneseitems", "parchmentitems"),
    ("parchment_japanesefull", "parchmentall"),
    ("parchment_mayanvillagers", "parchmentvillagers"),
    ("parchment_mayanbuildings", "parchmentbuildings"),
    ("parchment_mayanitems", "parchmentitems"),
    ("parchment_mayanfull", "parchmentall"),
    ("parchment_villagescroll", "parchmentvillage"),
    ("parchment_sadhu", "parchmentblank"),
]

def create_simple_blockstate(block_id):
    """Create a simple blockstate JSON for cube_all blocks"""
    return {
        "variants": {
            "": {"model": f"millenaire:block/{block_id}"}
        }
    }

def create_block_model(block_id, texture_name):
    """Create a block model JSON"""
    return {
        "parent": "minecraft:block/cube_all",
        "textures": {
            "all": f"millenaire:block/{texture_name}"
        }
    }

def create_item_model_from_block(block_id):
    """Create an item model that references a block model"""
    return {
        "parent": f"millenaire:block/{block_id}"
    }

def create_item_model(item_id, texture_name):
    """Create a standalone item model"""
    return {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"millenaire:item/{texture_name}"
        }
    }

def write_json(filepath, data):
    """Write JSON data to file"""
    with open(filepath, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"Created: {os.path.basename(filepath)}")

# Generate blockstates and models for simple blocks
print("Creating blockstates and block models...")
for block_id, texture_name in SIMPLE_BLOCKS:
    # Blockstate
    blockstate_path = os.path.join(BLOCKSTATES_DIR, f"{block_id}.json")
    write_json(blockstate_path, create_simple_blockstate(block_id))
    
    # Block model
    block_model_path = os.path.join(BLOCK_MODELS_DIR, f"{block_id}.json")
    write_json(block_model_path, create_block_model(block_id, texture_name))
    
    # BlockItem model
    item_model_path = os.path.join(ITEM_MODELS_DIR, f"{block_id}.json")
    write_json(item_model_path, create_item_model_from_block(block_id))

# Generate item models for standalone items
print("\nCreating item models...")
for item_id, texture_name in STANDALONE_ITEMS:
    item_model_path = os.path.join(ITEM_MODELS_DIR, f"{item_id}.json")
    write_json(item_model_path, create_item_model(item_id, texture_name))

print(f"\n✅ Done! Created {len(SIMPLE_BLOCKS) * 3} block-related files and {len(STANDALONE_ITEMS)} item files.")
print(f"Total: {len(SIMPLE_BLOCKS) * 3 + len(STANDALONE_ITEMS)} JSON files")
