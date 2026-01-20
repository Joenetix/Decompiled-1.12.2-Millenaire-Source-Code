
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

# ----------------------------------------------------------------------------
# DATA DEFINITIONS
# ----------------------------------------------------------------------------

STAIRS = [
    # (block_name, texture_name)
    ("stairs_green_tiles", "green_tiles_0"),
    ("stairs_red_tiles", "red_tiles_0"),
    ("stairs_byz_tiles", "byzantine_tiles_top"),
    ("stairs_mud_brick", "mudbrick"),
    ("stairs_sandstone_carved", "sandstone_carved"),
    ("stairs_ochre", "sandstone_ochre_clean"),
]

SLABS = [
    # (block_name, texture_name)
    ("byzantine_tiles_slab", "byzantine_tiles_top"),
    ("slab_mud_brick", "mudbrick"),
]

WALLS = [
    # (block_name, texture_name)
    ("snowwall", "snowbrick"),
]

PANES = [
    # (block_name, face_texture, edge_texture)
    ("paper_wall", "paper_wall", "paper_wall_side"),
    # Wooden bars/lattices are technically panes/bars
    # ("wooden_bars", "wooden_bars", "wooden_bars_top"), # Already in simple blocks? Check if simple block is enough via render type? No, usually needs pane_post.
]

SIMPLE_BLOCKS = [
    ("icebrick", "icebrick"),
    ("snowbrick", "snowbrick"),
    ("inuitcarving", "inuitcarving"), # if texture missing, fallback?
    ("fire_pit", "cobblestone"), # Placeholder
    ("hidehanging", "wool_colored_brown"), # Placeholder
]

ITEMS = [
    ("inuittrident", "inuittrident"),
    ("inuitbow", "inuitbow"),
    ("ulu", "ulu"),
    ("furhelmet", "furhelmet"),
    ("furchest", "furchest"),
    ("furlegs", "furlegs"),
    ("furboots", "furboots"),
    ("seljukscimitar", "seljukscimitar"),
    ("wallcarpetsmall", "wallcarpetsmall"),
]

# ----------------------------------------------------------------------------
# GENERATORS
# ----------------------------------------------------------------------------

def write_json(path, data):
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"Created: {os.path.basename(path)}")

def create_stairs_assets(block_name, texture):
    # Blockstate
    bs = {
        "variants": {
            "facing=east,half=bottom,shape=straight": { "model": f"millenaire:block/{block_name}", "y": 90 },
            "facing=west,half=bottom,shape=straight": { "model": f"millenaire:block/{block_name}", "y": 270 },
            "facing=south,half=bottom,shape=straight": { "model": f"millenaire:block/{block_name}", "y": 180 },
            "facing=north,half=bottom,shape=straight": { "model": f"millenaire:block/{block_name}" },
            "facing=east,half=bottom,shape=outer_right": { "model": f"millenaire:block/{block_name}_outer", "y": 90 },
            "facing=west,half=bottom,shape=outer_right": { "model": f"millenaire:block/{block_name}_outer", "y": 270 },
            "facing=south,half=bottom,shape=outer_right": { "model": f"millenaire:block/{block_name}_outer", "y": 180 },
            "facing=north,half=bottom,shape=outer_right": { "model": f"millenaire:block/{block_name}_outer" },
            "facing=east,half=bottom,shape=outer_left": { "model": f"millenaire:block/{block_name}_outer", "y": 90, "y": 90 }, # Oops logic
            # Simplified for brevity - usually you map all combos. 
            # For Millenaire, we'll map simplified ones or use a loop.
        }
    }
    
    # Correct mapping logic:
    variants = {}
    for facing, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
        variants[f"facing={facing},half=bottom,shape=straight"] = { "model": f"millenaire:block/{block_name}", "y": y }
        variants[f"facing={facing},half=bottom,shape=inner_left"] = { "model": f"millenaire:block/{block_name}_inner", "y": y, "uvlock": True }
        variants[f"facing={facing},half=bottom,shape=inner_right"] = { "model": f"millenaire:block/{block_name}_inner", "y": y, "uvlock": True }
        variants[f"facing={facing},half=bottom,shape=outer_left"] = { "model": f"millenaire:block/{block_name}_outer", "y": y, "uvlock": True }
        variants[f"facing={facing},half=bottom,shape=outer_right"] = { "model": f"millenaire:block/{block_name}_outer", "y": y, "uvlock": True }
        
        # Top half
        variants[f"facing={facing},half=top,shape=straight"] = { "model": f"millenaire:block/{block_name}", "y": y, "x": 180, "uvlock": True }
        variants[f"facing={facing},half=top,shape=inner_left"] = { "model": f"millenaire:block/{block_name}_inner", "y": y, "x": 180, "uvlock": True }
        variants[f"facing={facing},half=top,shape=inner_right"] = { "model": f"millenaire:block/{block_name}_inner", "y": y, "x": 180, "uvlock": True }
        variants[f"facing={facing},half=top,shape=outer_left"] = { "model": f"millenaire:block/{block_name}_outer", "y": y, "x": 180, "uvlock": True }
        variants[f"facing={facing},half=top,shape=outer_right"] = { "model": f"millenaire:block/{block_name}_outer", "y": y, "x": 180, "uvlock": True }

    write_json(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), {"variants": variants})

    # Models
    tex_path = f"millenaire:block/{texture}"
    
    model_std = {
        "parent": "minecraft:block/stairs",
        "textures": { "bottom": tex_path, "top": tex_path, "side": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), model_std)
    
    model_inner = {
        "parent": "minecraft:block/inner_stairs",
        "textures": { "bottom": tex_path, "top": tex_path, "side": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_inner.json"), model_inner)
    
    model_outer = {
        "parent": "minecraft:block/outer_stairs",
        "textures": { "bottom": tex_path, "top": tex_path, "side": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_outer.json"), model_outer)

    # Item Model
    item_model = { "parent": f"millenaire:block/{block_name}" }
    write_json(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), item_model)


def create_slab_assets(block_name, texture):
    # Blockstate
    variants = {
        "type=bottom": { "model": f"millenaire:block/{block_name}" },
        "type=top": { "model": f"millenaire:block/{block_name}_top" },
        "type=double": { "model": f"millenaire:block/{block_name}_double" }
    }
    write_json(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), {"variants": variants})

    tex_path = f"millenaire:block/{texture}"
    
    # Models
    model_bottom = {
        "parent": "minecraft:block/slab",
        "textures": { "bottom": tex_path, "top": tex_path, "side": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), model_bottom)
    
    model_top = {
        "parent": "minecraft:block/slab_top",
        "textures": { "bottom": tex_path, "top": tex_path, "side": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_top.json"), model_top)
    
    model_double = {
        "parent": "minecraft:block/cube_all",
        "textures": { "all": tex_path }
    }
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_double.json"), model_double)

    # Item Model
    item_model = { "parent": f"millenaire:block/{block_name}" }
    write_json(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), item_model)

def create_wall_assets(block_name, texture):
    # Blockstate (simplified - usually walls have complex multipart)
    # For now, just a post/side model for all states to avoid complexity
    # Actually, walls default usually to multipart.
    
    variants = {
        "variants": {
            "facing=north": { "model": f"millenaire:block/{block_name}_side", "uvlock": True },
            "facing=east": { "model": f"millenaire:block/{block_name}_side", "y": 90, "uvlock": True },
            "facing=south": { "model": f"millenaire:block/{block_name}_side", "y": 180, "uvlock": True },
            "facing=west": { "model": f"millenaire:block/{block_name}_side", "y": 270, "uvlock": True },
            "up=true": { "model": f"millenaire:block/{block_name}_post" },
            "up=false": { "model": f"millenaire:block/{block_name}_post" } # Fallback
        }
    }
    
    # Use Multipart for real walls
    multipart = [
        { "apply": { "model": f"millenaire:block/{block_name}_post" }, "when": { "up": "true" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side", "uvlock": True }, "when": { "north": "low" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side", "y": 90, "uvlock": True }, "when": { "east": "low" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side", "y": 180, "uvlock": True }, "when": { "south": "low" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side", "y": 270, "uvlock": True }, "when": { "west": "low" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_tall", "uvlock": True }, "when": { "north": "tall" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_tall", "y": 90, "uvlock": True }, "when": { "east": "tall" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_tall", "y": 180, "uvlock": True }, "when": { "south": "tall" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_tall", "y": 270, "uvlock": True }, "when": { "west": "tall" } }
    ]
    write_json(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), {"multipart": multipart})

    tex_path = f"millenaire:block/{texture}"
    
    # Models
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_post.json"), {
        "parent": "minecraft:block/template_wall_post", "textures": { "wall": tex_path }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_side.json"), {
        "parent": "minecraft:block/template_wall_side", "textures": { "wall": tex_path }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_side_tall.json"), {
        "parent": "minecraft:block/template_wall_side_tall", "textures": { "wall": tex_path }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_inventory.json"), {
        "parent": "minecraft:block/wall_inventory", "textures": { "wall": tex_path }
    })
    
    # Item Model
    write_json(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), {
        "parent": f"millenaire:block/{block_name}_inventory"
    })

def create_pane_assets(block_name, face_tex, edge_tex):
    # Multipart for panes
    tex_face = f"millenaire:block/{face_tex}"
    tex_edge = f"millenaire:block/{edge_tex}"
    
    multipart = [
        { "apply": { "model": f"millenaire:block/{block_name}_post" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side" }, "when": { "north": "true" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side", "y": 90 }, "when": { "east": "true" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_alt" }, "when": { "south": "true" } },
        { "apply": { "model": f"millenaire:block/{block_name}_side_alt", "y": 90 }, "when": { "west": "true" } },
        { "apply": { "model": f"millenaire:block/{block_name}_noside" }, "when": { "north": "false" } },
        { "apply": { "model": f"millenaire:block/{block_name}_noside_alt" }, "when": { "east": "false" } },
        { "apply": { "model": f"millenaire:block/{block_name}_noside_alt", "y": 90 }, "when": { "south": "false" } },
        { "apply": { "model": f"millenaire:block/{block_name}_noside", "y": 270 }, "when": { "west": "false" } },
    ]
    write_json(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), {"multipart": multipart})
    
    # Models (using example from iron_bars or glass_pane)
    # Simplification: just use 'pane_post', 'pane_side', 'pane_noside' templates.
    # Minecraft uses: template_glass_pane_post, template_glass_pane_side, etc.
    
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_post.json"), {
        "parent": "minecraft:block/template_glass_pane_post", "textures": { "pane": tex_face, "edge": tex_edge }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_side.json"), {
        "parent": "minecraft:block/template_glass_pane_side", "textures": { "pane": tex_face, "edge": tex_edge }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_side_alt.json"), {
        "parent": "minecraft:block/template_glass_pane_side_alt", "textures": { "pane": tex_face, "edge": tex_edge }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_noside.json"), {
        "parent": "minecraft:block/template_glass_pane_noside", "textures": { "pane": tex_face, "edge": tex_edge }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}_noside_alt.json"), {
        "parent": "minecraft:block/template_glass_pane_noside_alt", "textures": { "pane": tex_face, "edge": tex_edge }
    })
    
    # Item
    write_json(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), {
        "parent": "minecraft:item/generated", "textures": { "layer0": tex_face }
    })

def create_simple_assets(block_name, texture):
    write_json(os.path.join(BLOCKSTATES_DIR, f"{block_name}.json"), {
         "variants": { "": { "model": f"millenaire:block/{block_name}" } }
    })
    write_json(os.path.join(BLOCK_MODELS_DIR, f"{block_name}.json"), {
        "parent": "minecraft:block/cube_all", "textures": { "all": f"millenaire:block/{texture}" }
    })
    write_json(os.path.join(ITEM_MODELS_DIR, f"{block_name}.json"), {
        "parent": f"millenaire:block/{block_name}"
    })

def create_item_assets(item_name, texture):
    write_json(os.path.join(ITEM_MODELS_DIR, f"{item_name}.json"), {
        "parent": "minecraft:item/generated", "textures": { "layer0": f"millenaire:item/{texture}" }
    })

# ----------------------------------------------------------------------------
# MAIN EXECUTION
# ----------------------------------------------------------------------------

for name, tex in STAIRS:
    create_stairs_assets(name, tex)

for name, tex in SLABS:
    create_slab_assets(name, tex)

for name, tex in WALLS:
    create_wall_assets(name, tex)

for name, tex_face, tex_edge in PANES:
    create_pane_assets(name, tex_face, tex_edge)

for name, tex in SIMPLE_BLOCKS:
    create_simple_assets(name, tex)

for name, tex in ITEMS:
    create_item_assets(name, tex)

print("Done generating assets.")
