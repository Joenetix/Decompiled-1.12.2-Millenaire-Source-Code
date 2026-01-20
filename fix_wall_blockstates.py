"""
Fix all wall blockstate files for 1.20.1 compatibility.
- Changes true/false to none/low/tall format for wall connections
- Adds block/ prefix to model paths
- Creates missing _side_tall models
"""

import os
import json
import re

BLOCKSTATES_DIR = r"src\main\resources\assets\millenaire\blockstates"
MODELS_DIR = r"src\main\resources\assets\millenaire\models\block"

def get_wall_name_from_filename(filename):
    """Extract wall name like 'wall_mud_brick' from filename"""
    return filename.replace('.json', '')

def create_blockstate_1_20(wall_name):
    """Create a proper 1.20.1 wall blockstate with none/low/tall support"""
    return {
        "multipart": [
            {
                "when": { "up": "true" },
                "apply": { "model": f"millenaire:block/{wall_name}_post" }
            },
            {
                "when": { "north": "low" },
                "apply": { "model": f"millenaire:block/{wall_name}_side", "uvlock": True }
            },
            {
                "when": { "north": "tall" },
                "apply": { "model": f"millenaire:block/{wall_name}_side_tall", "uvlock": True }
            },
            {
                "when": { "east": "low" },
                "apply": { "model": f"millenaire:block/{wall_name}_side", "y": 90, "uvlock": True }
            },
            {
                "when": { "east": "tall" },
                "apply": { "model": f"millenaire:block/{wall_name}_side_tall", "y": 90, "uvlock": True }
            },
            {
                "when": { "south": "low" },
                "apply": { "model": f"millenaire:block/{wall_name}_side", "y": 180, "uvlock": True }
            },
            {
                "when": { "south": "tall" },
                "apply": { "model": f"millenaire:block/{wall_name}_side_tall", "y": 180, "uvlock": True }
            },
            {
                "when": { "west": "low" },
                "apply": { "model": f"millenaire:block/{wall_name}_side", "y": 270, "uvlock": True }
            },
            {
                "when": { "west": "tall" },
                "apply": { "model": f"millenaire:block/{wall_name}_side_tall", "y": 270, "uvlock": True }
            }
        ]
    }

# Map wall names to textures
TEXTURE_MAP = {
    'wall_mud_brick': 'millenaire:blocks/mudbrick',
    'wall_painted_brick_black': 'millenaire:blocks/painted_brick_black',
    'wall_painted_brick_blue': 'millenaire:blocks/painted_brick_blue',
    'wall_painted_brick_brown': 'millenaire:blocks/painted_brick_brown',
    'wall_painted_brick_cyan': 'millenaire:blocks/painted_brick_cyan',
    'wall_painted_brick_gray': 'millenaire:blocks/painted_brick_gray',
    'wall_painted_brick_green': 'millenaire:blocks/painted_brick_green',
    'wall_painted_brick_light_blue': 'millenaire:blocks/painted_brick_light_blue',
    'wall_painted_brick_lime': 'millenaire:blocks/painted_brick_lime',
    'wall_painted_brick_magenta': 'millenaire:blocks/painted_brick_magenta',
    'wall_painted_brick_orange': 'millenaire:blocks/painted_brick_orange',
    'wall_painted_brick_pink': 'millenaire:blocks/painted_brick_pink',
    'wall_painted_brick_purple': 'millenaire:blocks/painted_brick_purple',
    'wall_painted_brick_red': 'millenaire:blocks/painted_brick_red',
    'wall_painted_brick_silver': 'millenaire:blocks/painted_brick_silver',
    'wall_painted_brick_white': 'millenaire:blocks/painted_brick_white',
    'wall_painted_brick_yellow': 'millenaire:blocks/painted_brick_yellow',
    'wall_sandstone_carved': 'millenaire:blocks/sandstone_carved',
    'wall_sandstone_ochre_carved': 'millenaire:blocks/sandstone_ochre_carved',
    'wall_sandstone_red_carved': 'millenaire:blocks/sandstone_red_carved',
    'snowwall': 'millenaire:blocks/snowbrick',  # snowwall uses snowbrick texture
}

def get_texture_from_wall_name(wall_name):
    """Map wall name to texture path"""
    if wall_name in TEXTURE_MAP:
        return TEXTURE_MAP[wall_name]
    # Fallback: try to infer
    base = wall_name.replace('wall_', '')
    return f'millenaire:blocks/{base}'

def create_side_tall_model(wall_name, texture):
    """Create a wall_side_tall model"""
    return {
        "parent": "minecraft:block/template_wall_side_tall",
        "textures": {
            "wall": texture
        }
    }

def create_side_model(wall_name, texture):
    """Create a wall_side model"""
    return {
        "parent": "minecraft:block/template_wall_side",
        "textures": {
            "wall": texture
        }
    }

def create_post_model(wall_name, texture):
    """Create a wall_post model"""
    return {
        "parent": "minecraft:block/template_wall_post",
        "textures": {
            "wall": texture
        }
    }

def is_wall_blockstate(filepath):
    """Check if this blockstate uses old wall format with true/false"""
    try:
        with open(filepath, 'r') as f:
            content = f.read()
            data = json.loads(content)
            if 'multipart' in data:
                for part in data['multipart']:
                    when = part.get('when', {})
                    # Old format uses "true" for north/south/east/west
                    for direction in ['north', 'south', 'east', 'west']:
                        if when.get(direction) == 'true':
                            return True
    except:
        pass
    return False

def main():
    # Get all potential wall blockstate files
    all_files = os.listdir(BLOCKSTATES_DIR)
    
    wall_files = []
    for f in all_files:
        if not f.endswith('.json'):
            continue
        filepath = os.path.join(BLOCKSTATES_DIR, f)
        # Check if it's a wall_ file or has old wall format
        if f.startswith('wall_') or is_wall_blockstate(filepath):
            wall_files.append(f)
    
    print(f"Found {len(wall_files)} wall-type blockstate files")
    
    for wall_file in wall_files:
        wall_name = get_wall_name_from_filename(wall_file)
        texture = get_texture_from_wall_name(wall_name)
        
        print(f"Processing {wall_name}...")
        
        # Create/update blockstate
        blockstate_path = os.path.join(BLOCKSTATES_DIR, wall_file)
        blockstate = create_blockstate_1_20(wall_name)
        with open(blockstate_path, 'w') as f:
            json.dump(blockstate, f, indent=4)
        print(f"  Updated blockstate: {blockstate_path}")
        
        # Create/update post model
        post_path = os.path.join(MODELS_DIR, f"{wall_name}_post.json")
        post_model = create_post_model(wall_name, texture)
        with open(post_path, 'w') as f:
            json.dump(post_model, f, indent=4)
        print(f"  Created/updated post model")
        
        # Create/update side model
        side_path = os.path.join(MODELS_DIR, f"{wall_name}_side.json")
        side_model = create_side_model(wall_name, texture)
        with open(side_path, 'w') as f:
            json.dump(side_model, f, indent=4)
        print(f"  Created/updated side model")
        
        # Create side_tall model
        side_tall_path = os.path.join(MODELS_DIR, f"{wall_name}_side_tall.json")
        side_tall_model = create_side_tall_model(wall_name, texture)
        with open(side_tall_path, 'w') as f:
            json.dump(side_tall_model, f, indent=4)
        print(f"  Created side_tall model")
    
    print(f"\nDone! Fixed {len(wall_files)} wall blockstates and created/updated models.")

if __name__ == "__main__":
    main()
