# Millenaire Revived - Standalone Mod

Autonomous cultural villages built from scratch for Minecraft 1.20.1 Forge.

## What We Built

**No dependencies!** Everything is custom-built:
- ✅ Custom Village AI system
- ✅ Custom Citizen entities with jobs
- ✅ Multi-level building system
- ✅ Resource management
- ✅ Autonomous village development

## Architecture

### Core Systems (`org.millenaire.core`)
- **Village.java** - Main village logic, manages citizens/buildings/resources
- **VillageRegistry.java** - Tracks all villages, persists with world save

### Entities (`org.millenaire.entities`)
- **Citizen.java** - Autonomous NPC with job system
- **Job.java** - Base class for professions (Farmer, Builder, Miner, etc.)

### Buildings (`org.millenaire.buildings`)
- **Building.java** - Multi-level buildings with upgrade paths

### Resources (`org.mill enaire.resources`)
- **ResourceInventory.java** - Item storage and management

### World Generation (`org.millenaire.worldgen`)
- **VillageSpawner.java** - Creates autonomous villages when structures generate

### Cultures (`org.millenaire.cultures`)
- **CultureRegistry.java** - Manages Norman, Mayan, Japanese, Hindi, Byzantine cultures
- **NormanCulture.java** - First culture implementation

## Current Status

**Phase 1 - Foundation: COMPLETE** ✅
- Core Village system
- Citizen entities
- Building framework
- Resource management
- Village persistence

**Next: Phase 2 - Jobs & AI**
- Implement Builder job
- Implement Farmer job
- Add task system
- Create AI goals

## Building

Requires Java 17 and Gradle.

```bash
./gradlew build
```

## Running

```bash
./gradlew runClient
```

## Design Philosophy

Inspired by:
- Original Millenaire's cultural diversity
- MineColonies' proven AI patterns
- Vanilla Minecraft's Jigsaw worldgen

But **100% custom code** - no dependencies, full control!

## License

MIT - See LICENSE file

Original Millenaire by Kinniken  
Revival by the Millenaire Team
