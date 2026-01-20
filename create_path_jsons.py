import os
import json

# Define all path blocks
path_blocks = ['pathdirt', 'pathgravel', 'pathslabs', 'pathsandstone', 'pathochretiles', 'pathgravelslabs', 'pathsnow']
path_slabs = [f'{pb}_slab' for pb in path_blocks]
all_paths = path_blocks + path_slabs

# Create directories
bs_dir = r'src\main\resources\assets\millenaire\blockstates'
bm_dir = r'src\main\resources\assets\millenaire\models\block'
im_dir = r'src\main\resources\assets\millenaire\models\item'

os.makedirs(bs_dir, exist_ok=True)
os.makedirs(bm_dir, exist_ok=True)
os.makedirs(im_dir, exist_ok=True)

# Create blockstate JSONs
for block in all_paths:
    blockstate = {"variants": {"": {"model": f"millenaire:block/{block}"}}}
    with open(os.path.join(bs_dir, f'{block}.json'), 'w') as f:
        json.dump(blockstate, f, indent=2)

# Create block model JSONs
for block in all_paths:
    model = {"parent": "block/cube_all", "textures": {"all": f"millenaire:block/{block}"}}
    with open(os.path.join(bm_dir, f'{block}.json'), 'w') as f:
        json.dump(model, f, indent=2)

# Create item model JSONs
for block in all_paths:
    item_model = {"parent": f"millenaire:block/{block}"}
    with open(os.path.join(im_dir, f'{block}.json'), 'w') as f:
        json.dump(item_model, f, indent=2)

print(f'✅ Created {len(all_paths)*3} JSON files for {len(all_paths)} path blocks')
print(f'   - {len(all_paths)} blockstate JSONs')
print(f'   - {len(all_paths)} block model JSONs')
print(f'   - {len(all_paths)} item model JSONs')
