<p align="center"><img src="https://github.com/user-attachments/assets/9be0aa72-e2c5-456e-9acf-d303b790dc44" alt="YepBlocked Logo" height="100"></p>
<h1 align="center">YepBlocked</h1>

> [!NOTE]
> This repo is a **single multi-module Gradle project** with 7 submodules (`:common`, `:forge-1-21-1`, `:fabric-26-1`, `:fabric-26-2`, `:neoforge-1-21-1`, `:neoforge-26-1`, `:neoforge-26-2`). Open the root in IntelliJ to see all modules. Build only the platform you need with the commands below.

<h2 align="center">Description</h2>

YepBlocked is a Minecraft mod that gives you complete control over which entities can spawn and under what conditions. Configure it per-entity with fine-grained toggles for natural spawns, spawners, trial spawners, and spawn eggs - all through in-game commands.

<h2 align="center">Features</h2>

- **Global toggles** — block or allow all natural / spawner / trial spawner / egg spawns at once
- **Per-entity overrides** — fine-tune specific mobs (e.g. block phantom natural spawns but allow spawn eggs)
- **Glob & regex patterns** — use `minecraft:*` or `.*golem` to match entire groups of entities
- **Hot-reload** — apply config changes instantly with `/yb reload` (no restart required)
- **Commands** — `/yb add`, `/yb remove`, `/yb reload`, `/yb global` with tab completion
- **Persistent config** — `config/YepBlocked.json`, auto-created on first run

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

<h2 align="center">Installation</h2>

1. https://www.curseforge.com/minecraft/mc-mods/yepblocked
2. 

<h2 align="center">Build from source</h2>

The project uses a **single multi-module Gradle structure** — one `gradlew` at the root, shared code in `:common`, platform modules inline common sources via `kotlin.srcDir` for self-contained JARs.

**Prerequisites:**
- [JDK 21+](https://adoptium.net/) (Gradle auto-downloads JDK 25 for MC 26.1 platforms if missing)
- Internet connection (first build downloads Minecraft mappings and dependencies)

**Clone & navigate:**
```
git clone https://github.com/bequietbro/MC-YepBlocked.git
cd YepBlocked
```

**Build & test all modules:**
```
.\gradlew check
```

**Build & test a specific platform:**
```
.\gradlew :neoforge-1-21-1:check
```

**Run the unit tests only (shared code in `:common`):**
```
.\gradlew :common:check
```

**Platforms:**

<table>
  <tr>
    <th>Module</th>
    <th>Loader</th>
    <th>MC Version</th>
    <th>Java</th>
    <th>Run client</th>
  </tr>
  <tr>
    <td><code>:forge-1-21-1</code></td>
    <td>Forge</td>
    <td>1.21.1</td>
    <td>21</td>
    <td><code>.\gradlew :forge-1-21-1:runClient</code></td>
  </tr>
  <tr>
    <td><code>:neoforge-1-21-1</code></td>
    <td>NeoForge</td>
    <td>1.21.1</td>
    <td>21</td>
    <td><code>.\gradlew :neoforge-1-21-1:runClient</code></td>
  </tr>
  <tr>
    <td><code>:fabric-26-1</code></td>
    <td>Fabric</td>
    <td>26.1</td>
    <td>25</td>
    <td><code>.\gradlew :fabric-26-1:runClient</code></td>
  </tr>
  <tr>
    <td><code>:fabric-26-2</code></td>
    <td>Fabric</td>
    <td>26.2</td>
    <td>25</td>
    <td><code>.\gradlew :fabric-26-2:runClient</code></td>
  </tr>
  <tr>
    <td><code>:neoforge-26-1</code></td>
    <td>NeoForge</td>
    <td>26.1</td>
    <td>25</td>
    <td><code>.\gradlew :neoforge-26-1:runClient</code></td>
  </tr>
  <tr>
    <td><code>:neoforge-26-2</code></td>
    <td>NeoForge</td>
    <td>26.2</td>
    <td>25</td>
    <td><code>.\gradlew :neoforge-26-2:runClient</code></td>
  </tr>
</table>

<h2 align="center">Testing</h2>

All unit tests (65) are in `:common/src/test/kotlin/` (shared across all platforms). Run them from the project root:

```
.\gradlew :common:check
```

Or run everything (common tests + all platform builds):

```
.\gradlew check
```

Tests cover:
- Config serialization & migration
- Flag parsing (unknown flags, empty keys, tilde+`=` collisions)
- Glob-to-regex conversion and pattern matching
- Override priority (exact > regex/glob > global)

<h2 align="center">Credits</h2>

<p align="center">
Developed by <a href="https://github.com/BEQI">BEQI</a>.
</p>

<h2 align="center">License</h2>

<p align="center">
<img src="https://www.gnu.org/graphics/agplv3-155x51.png" alt="AGPLv3 License" />
</p>

<p align="center">
This project is licensed under the <strong>GNU Affero General Public License v3.0</strong>.
</p>
