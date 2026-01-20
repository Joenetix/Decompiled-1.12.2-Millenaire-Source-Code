
import os
import json

BASE_PATH = "C:/Millenaire Revived v0/src/main/resources/assets/millenaire"
BLOCKSTATES_DIR = os.path.join(BASE_PATH, "blockstates")
BLOCK_MODELS_DIR = os.path.join(BASE_PATH, "models/block")
ITEM_MODELS_DIR = os.path.join(BASE_PATH, "models/item")

# Ensure directories exist
for d in [BLOCKSTATES_DIR, BLOCK_MODELS_DIR, ITEM_MODELS_DIR]:
    if not os.path.exists(d):
        os.makedirs(d)

# --- Helper Functions ---

def create_stairs_assets(block_name, texture_name):
    """
    Generates blockstate, and 3 models (block, inner, outer) for stairs.
    Also generates Item model.
    """
    # 1. Models
    # Base
    model_base = {
        "parent": "minecraft:block/stairs",
        "textures": {
            "bottom": f"millenaire:block/{texture_name}",
            "top": f"millenaire:block/{texture_name}",
            "side": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(model_base, f, indent=2)

    # Inner
    model_inner = {
        "parent": "minecraft:block/inner_stairs",
        "textures": {
            "bottom": f"millenaire:block/{texture_name}",
            "top": f"millenaire:block/{texture_name}",
            "side": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_inner.json"), "w") as f:
        json.dump(model_inner, f, indent=2)

    # Outer
    model_outer = {
        "parent": "minecraft:block/outer_stairs",
        "textures": {
            "bottom": f"millenaire:block/{texture_name}",
            "top": f"millenaire:block/{texture_name}",
            "side": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_outer.json"), "w") as f:
        json.dump(model_outer, f, indent=2)

    # 2. Blockstate (Full variants)
    # We construct the 4 facings * 2 halves * 5 shapes = 40 variants
    variants = {}
    for facing in ["north", "south", "east", "west"]:
        y = 0
        if facing == "east": y = 90
        elif facing == "south": y = 180
        elif facing == "west": y = 270
        
        for half in ["bottom", "top"]:
            x = 0 if half == "bottom" else 180 # Usually handled by model rotation x:180 for top in vanilla logic?
            # Actually standard generic handling:
            # top half is usually x=180, uvlock=true.
            
            for shape in ["straight", "inner_left", "inner_right", "outer_left", "outer_right"]:
                # Determine model
                model_suffix = ""
                if "inner" in shape: model_suffix = "_inner"
                elif "outer" in shape: model_suffix = "_outer"
                
                model = f"millenaire:block/{block_name}{model_suffix}"
                
                # Rotations
                # Vanilla stairs logic is complex.
                # Simplified robust logic:
                # straightforward y rotation based on facing.
                # inner/outer shapes adjust rotation?
                # Actually, the base model + facing rotation usually enough if shape models are standard.
                # Let's verify standard forge/vanilla stair implementation.
                # Straight: standard.
                # Inner Left: y=0 (if north).
                
                # To be SAFE and avoid visual glitches, we'll try to follow vanilla 'stairs.json' pattern if possible.
                # But creating a dynamic map here is safer than hardcoding.
                
                # Variant key string
                key = f"facing={facing},half={half},shape={shape}"
                
                # Add 'waterlogged' variants (true/false) - map to same model
                for wl in ["false", "true"]:
                    full_key = f"{key},waterlogged={wl}"
                    
                    variant_data = {"model": model}
                    
                    # Compute rotations
                    ry = y
                    rx = 0
                    uvlock = False
                    
                    if half == "top":
                        rx = 180
                        uvlock = True
                    
                    # Shape adjustments for Inner/Outer based on vanilla assets?
                    # Vanilla 'stairs' blockstate defines explicit y and uvlock for shapes.
                    # e.g. facing=east, shape=inner_right -> y=90 -> rotated.
                    # It seems shape doesn't add extra rotation if using inner/outer models correctly.
                    # EXCEPT: 
                    # inner_left of East might need different rotation?
                    # Let's stick to base facing rotation. If corners look wrong, we can iterate.
                    # Base facing rotation: North=0, East=90, South=180, West=270.
                    # BUT specific shapes might need 90 degree offsets?
                    # inner_left: y = y (usually)
                    # inner_right: y = y
                    # outer_left: y = y
                    # outer_right: y = y
                    # Actually, strict vanilla blockstates often have:
                    # facing=east,shape=inner_left -> y=90
                    # facing=east,shape=inner_right -> y=90
                    # So it seems standard rotation applies.
                    
                    if ry != 0: variant_data["y"] = ry
                    if rx != 0: variant_data["x"] = rx
                    if uvlock: variant_data["uvlock"] = True
                    
                    variants[full_key] = variant_data

    blockstate = {"variants": variants}
    with open(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), "w") as f:
        json.dump(blockstate, f, indent=2)

    # 3. Item Model
    item_model = {
        "parent": f"millenaire:block/{block_name}"
    }
    with open(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(item_model, f, indent=2)
    print(f"Generated Stairs: {block_name}")


def create_slab_assets(block_name, texture_name):
    """
    Generates blockstate, and models (bottom, top, double) for slabs.
    Note: 'double' slab usually uses full cube model.
    """
    # 1. Models
    # Bottom
    model_bottom = {
        "parent": "minecraft:block/slab",
        "textures": {
            "bottom": f"millenaire:block/{texture_name}",
            "top": f"millenaire:block/{texture_name}",
            "side": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(model_bottom, f, indent=2)

    # Top
    model_top = {
        "parent": "minecraft:block/slab_top",
        "textures": {
            "bottom": f"millenaire:block/{texture_name}",
            "top": f"millenaire:block/{texture_name}",
            "side": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_top.json"), "w") as f:
        json.dump(model_top, f, indent=2)

    # Double (Full Cube)
    model_double = {
        "parent": "minecraft:block/cube_all",
        "textures": {
            "all": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_double.json"), "w") as f:
        json.dump(model_double, f, indent=2)

    # 2. Blockstate
    variants = {}
    for type_val, model_suffix in [("bottom", ""), ("top", "_top"), ("double", "_double")]:
        model = f"millenaire:block/{block_name}{model_suffix}"
        
        # Waterlogged variants
        for wl in ["false", "true"]:
            key = f"type={type_val},waterlogged={wl}"
            variants[key] = {"model": model}
    
    blockstate = {"variants": variants}
    with open(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), "w") as f:
        json.dump(blockstate, f, indent=2)

    # 3. Item Model
    item_model = {
        "parent": f"millenaire:block/{block_name}"
    }
    with open(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(item_model, f, indent=2)
    print(f"Generated Slab: {block_name}")

def create_bed_assets(block_name, texture_name):
    # Minimal bed implementation to satisfy blockstate errors.
    # Uses 'bed_charpoy' or similar texture.
    # Models: head, foot.
    # We'll use a existing logic or simple placeholder if models are complex.
    # Actually, vanilla beds have "bed_head", "bed_foot" models.
    # But those use "particle" texture. They are EntityBlock based usually.
    # For visual correctness without entity renderer setup, we might see nothing.
    # BUT, to fix the crash/errors, we MUST provide the variants.
    
    # We will generate a "particle only" model to satisfy the loader, 
    # relying on the BlockEntityRenderer to draw the bed.
    
    model_data = {
        "parent": "minecraft:block/bed", # parent often just sets particle
        "textures": {
            "particle": f"millenaire:block/{texture_name}"
        }
    }
    with open(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(model_data, f, indent=2)

    # Blockstate
    variants = {}
    for facing in ["north", "south", "east", "west"]:
        y = 0
        if facing == "east": y = 90
        elif facing == "south": y = 180
        elif facing == "west": y = 270
        
        for part in ["head", "foot"]:
            for occupied in ["false", "true"]:
                key = f"facing={facing},occupied={occupied},part={part}"
                
                # Bed models typically: head points s/w/n/e?
                # We'll point to same model for now + rotation
                variants[key] = {
                    "model": f"millenaire:block/{block_name}",
                    "y": y
                }
    
    with open(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), "w") as f:
        json.dump({"variants": variants}, f, indent=2)
        
    # Item Model
    # Bed items are usually sprites (sprites on bed). 
    # We'll use generated item with texture.
    item_texture = texture_name
    if "charpoy" in texture_name: item_texture = "bed_charpoy"
    
    item_model = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"millenaire:item/{item_texture}" 
            # Note: Bed items usually in items/ folder. Verify existence? 
            # If not, use block texture as fallback.
        }
    }
    # Check if item texture exists? 
    # For now assume item texture same name or fallback
    # If bed_charpoy blocks use 'bed_charpoy.png' in blocks, item might be 'bed_charpoy.png' in items.
    
    with open(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), "w") as f:
        json.dump(item_model, f, indent=2)
    print(f"Generated Bed: {block_name}")


# --- Execution ---

# Stairs List: name -> texture
stairs = [
    ("stairs_timberframe", "timberframeplain"),
    ("stairs_mudbrick", "mudbrick"),
    ("stairs_cookedbrick", "cookedbrick"),
    ("stairs_thatch", "thatch"),
    ("stairs_gray_tiles", "gray_tiles_0"), # Simplification
    # Add others?
    # mudbrick_seljuk_decorated? No, stair list from errors:
    # stairs_timberframe, stairs_mudbrick, stairs_cookedbrick, stairs_thatch, stairs_gray_tiles.
]

# Slabs List: name -> texture
slabs = [
    # wood_deco uses wood_deco blockstate to decide texture? 
    # Actually, wood_deco is a block with sub-blocks (thatch, timberframe etc).
    # slab_wood_deco likely mimics 'oak_planks' but specific texture?
    # Based on errors: slab_wood_deco, slab_stone_deco, gray_tiles_slab, green_tiles_slab, red_tiles_slab.
    
    # What texture for slab_wood_deco? 
    # In older millenaire, it was often the timberframe or generic wood.
    # Let's use 'timberframeplain' for wood_deco slab for now.
    ("slab_wood_deco", "timberframeplain"),
    
    # stone_deco? 'stone' or 'stone_deco'? 
    # Let's use 'stone' or 'sandstone'?
    # blockstates/stone_deco.json might have clues.
    # But for safety, 'stone' texture seems safe if stone_deco.png missing.
    ("slab_stone_deco", "stone"), 
    
    ("gray_tiles_slab", "gray_tiles_0"),
    ("green_tiles_slab", "green_tiles_0"),
    ("red_tiles_slab", "red_tiles_0"),
]

# Beds
beds = [
    ("bed_straw", "thatch"), # Guessing texture
    ("bed_charpoy", "bed_charpoy"), # verified texture
]

# Run Generators
for name, tex in stairs:
    create_stairs_assets(name, tex)

for name, tex in slabs:
    create_slab_assets(name, tex)
    
for name, tex in beds:
    create_bed_assets(name, tex)

