<p align="center"><img src="https://github.com/user-attachments/assets/9be0aa72-e2c5-456e-9acf-d303b790dc44" alt="YepBlocked Logo" height="100"></p>
<h1 align="center">YepBlocked</h1>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/yepblocked">
    <img src="https://img.shields.io/badge/CurseForge-F1642E?style=for-the-badge&logo=curseforge&logoColor=white" alt="CurseForge">
  </a>
  <a href="https://modrinth.com/mod/yepblocked">
    <img src="https://img.shields.io/badge/Modrinth-1BD96A?style=for-the-badge&logo=modrinth&logoColor=white" alt="Modrinth">
  </a>
  <a href="https://github.com/bequietbro/MC-YepBlocked/issues">
    <img src="https://img.shields.io/badge/Issues-181717?style=for-the-badge&logo=github&logoColor=white" alt="Issues">
  </a>
</p>

> [!IMPORTANT]
> This repository contains **source code only**. Ready-to-install JARs are published on
> [Modrinth](https://modrinth.com/mod/yepblocked) and
> [CurseForge](https://www.curseforge.com/minecraft/mc-mods/yepblocked).
> Nothing is attached to GitHub releases.

> [!NOTE]
> This repo is a **single multi-module Gradle project** with 9 submodules (`:common`, `:forge-1-21-1`, `:neoforge-1-21-1`, `:fabric-26-1`, `:fabric-26-2`, `:fabric-26-3`, `:neoforge-26-1`, `:neoforge-26-2`, `:neoforge-26-3`). Open the root in IntelliJ to see all modules. Build only the platform you need with the commands below.

<h2 align="center">Description</h2>

YepBlocked is a Minecraft mod that gives you complete control over which entities can spawn and under what conditions. Configure it per-entity with fine-grained toggles for natural spawns, spawners, trial spawners, and spawn eggs - all through in-game commands.

<h2 align="center">Supported versions</h2>

Each build targets exactly one Minecraft version. Installing a build that does not match your game version shows a clear "incompatible" message instead of crashing.

| Minecraft | Forge | NeoForge | Fabric |
|---|---|---|---|
| 1.21.1 | ✅ | ✅ | — |
| 26.1.2 | — | ✅ | ✅ |
| 26.2 | — | ✅ | ✅ |
| 26.3 | — | ✅ | ✅ |

<h2 align="center">Requirements</h2>

YepBlocked is written in Kotlin, so it needs a Kotlin runtime for your loader:

- **Forge 1.21.1** — [Kotlin for Forge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge) 5.12.0 or newer
- **NeoForge 1.21.1** — Kotlin for Forge 5.12.0 or newer
- **NeoForge 26.x** — Kotlin for Forge 6.5.0 or newer
- **Fabric** — [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) 1.14.2 or newer

The mod runs on the server side. In singleplayer the integrated server loads it, and on a LAN world only the host needs it — players who join do not have to install anything.

<h2 align="center">Features</h2>

- **Global toggles** — block or allow all natural / spawner / trial spawner / egg spawns at once
- **Per-entity overrides** — fine-tune specific mobs (e.g. block phantom natural spawns but allow spawn eggs)
- **Glob & regex patterns** — use `minecraft:*` or `.*golem` to match entire groups of entities
- **Hot-reload** — apply config changes instantly with `/yb reload` (no restart required)
- **Commands** — `/yb add`, `/yb remove`, `/yb reload`, `/yb global` with tab completion
- **Persistent config** — `config/YepBlocked.json`, auto-created on first run
- **Quiet logs** — each blocked entity and spawn reason is reported once, the rest is summarized

<h2 align="center">Commands</h2>

All commands require operator privileges (permission level 2):

| Command | Description |
|---|---|
| `/yb reload` | Reload config from disk. Only needed after manual edits — changes via `/yb add`, `/yb remove`, `/yb global` apply immediately |
| `/yb add <entity> [flags]` | Add an override. Flags: `natural`, `spawner`, `trial`, `egg` — bare flag = block, `~` = allow, `key=false` = block, `key=true` = allow |
| `/yb remove <entity>` | Remove an override |
| `/yb global [flags]` | Set global spawn defaults. Same flag syntax as `/yb add`. Without flags, shows current state. |

<h2 align="center">Config file</h2>

Config is stored in `config/YepBlocked.json` and auto-created on first run. You can edit it manually while the server is running — changes take effect after `/yb reload`.

**Example — block all natural spawns, allow everything else:**
```json
{
  "enableNaturalSpawn": false,
  "enableSpawnerSpawn": true,
  "enableTrialSpawnerSpawn": true,
  "enableEggSpawn": true,
  "entityOverrides": {}
}
```

**Example — per-entity override (zombie fully blocked, creeper only natural blocked, all golems blocked):**
```json
{
  "enableNaturalSpawn": true,
  "entityOverrides": {
    "minecraft:zombie": false,
    "minecraft:creeper": {
      "enableNaturalSpawn": false,
      "enableSpawnerSpawn": true,
      "enableEggSpawn": true
    },
    ".*golem": false
  }
}
```

Override value can be:
- `false` (boolean) — block all spawn types for that entity
- `true` (boolean) — use global defaults (equivalent to removing the override)
- An object with per-type toggles (`enableNaturalSpawn`, `enableSpawnerSpawn`, `enableTrialSpawnerSpawn`, `enableEggSpawn`) — `false` = block, `true` = allow, omitted = fall back to global

**Mod mob example (Alex's Mobs):**
```json
{
  "enableNaturalSpawn": true,
  "entityOverrides": {
    "alexsmobs:gorilla": {
      "enableNaturalSpawn": false,
      "enableSpawnerSpawn": true,
      "enableEggSpawn": true
    },
    "alexsmobs:void_worm": false
  }
}
```

Entity IDs follow the `namespace:path` format — you can find them with `/yb add <TAB>` in-game.

<h2 align="center">Logging</h2>

A broad rule such as `"minecraft:*": false` can block hundreds of spawns per second, so blocked spawns are not logged one by one:

```
[ENTITY] minecraft:chicken (SPAWN_ITEM_USE) -> BLOCKED
[SUMMARY] blocked 1000 more spawns (total 1000, 1 entity/spawn-type pairs)
```

- the first block of a given entity and spawn reason is always printed as `[ENTITY]`
- repeats of the same pair are only counted, and every 1000 of them produce one `[SUMMARY]` line
- counters reset on `/yb add`, `/yb remove`, `/yb global` and `/yb reload`, so the first block after a config change is always visible

<h2 align="center">Build from source</h2>

The project uses a **single multi-module Gradle structure** — one `gradlew` at the root.

**Prerequisites:**
- [JDK 21+](https://adoptium.net/) (Gradle auto-downloads JDK 25 for the MC 26.x platforms if missing)
- Internet connection (first build downloads Minecraft mappings and dependencies)

**Clone & navigate:**
```
git clone https://github.com/bequietbro/MC-YepBlocked.git
cd MC-YepBlocked
```

**Build & test all modules:**
```
./gradlew check
```

**Build & test a specific platform:**
```
./gradlew :neoforge-26-3:check
```

**Run the unit tests only:**
```
./gradlew :common:check
```

> On Windows use `.\gradlew` instead of `./gradlew`.

**Platforms:**

| Module | Loader | Minecraft | Java | Run client |
|---|---|---|---|---|
| `:forge-1-21-1` | Forge | 1.21.1 | 21 | `./gradlew :forge-1-21-1:runClient` |
| `:neoforge-1-21-1` | NeoForge | 1.21.1 | 21 | `./gradlew :neoforge-1-21-1:runClient` |
| `:fabric-26-1` | Fabric | 26.1.2 | 25 | `./gradlew :fabric-26-1:runClient` |
| `:fabric-26-2` | Fabric | 26.2 | 25 | `./gradlew :fabric-26-2:runClient` |
| `:fabric-26-3` | Fabric | 26.3 | 25 | `./gradlew :fabric-26-3:runClient` |
| `:neoforge-26-1` | NeoForge | 26.1.2 | 25 | `./gradlew :neoforge-26-1:runClient` |
| `:neoforge-26-2` | NeoForge | 26.2 | 25 | `./gradlew :neoforge-26-2:runClient` |
| `:neoforge-26-3` | NeoForge | 26.3 | 25 | `./gradlew :neoforge-26-3:runClient` |

<h2 align="center">Testing</h2>

All unit tests (72) are in `:common/src/test/kotlin/` (shared across all platforms). Run them from the project root:

```
./gradlew :common:check
```

Or run everything (common tests + all platform builds):

```
./gradlew check
```

Tests cover:
- Config serialization & migration
- Flag parsing (unknown flags, empty keys, tilde+`=` collisions)
- Glob-to-regex conversion and pattern matching
- Override priority (exact > regex/glob > global)
- Blocked-spawn logging (one line per entity and spawn reason, summary threshold, reset on config change, thread safety)

<h2 align="center">Reporting issues</h2>

Open an issue at [github.com/bequietbro/MC-YepBlocked/issues](https://github.com/bequietbro/MC-YepBlocked/issues) and include:

- Minecraft version and loader version (Forge / NeoForge / Fabric)
- Kotlin runtime version (Kotlin for Forge, or Fabric Language Kotlin)
- your `config/YepBlocked.json`
- the relevant part of `logs/latest.log` (lines containing `[ENTITY]` or `[SUMMARY]`)
- steps to reproduce, or the exact `/yb` commands you used

Please make sure you installed the build that matches your Minecraft version exactly.

<h2 align="center">Credits</h2>

<p align="center">
Developed by <a href="https://github.com/bequietbro">BEQUIETBRO</a>.
</p>

<h2 align="center">License</h2>

<p align="center">
<img src="https://www.gnu.org/graphics/agplv3-155x51.png" alt="AGPLv3 License" />
</p>

<p align="center">
This project is licensed under the <strong>GNU Affero General Public License v3.0</strong>.
</p>
