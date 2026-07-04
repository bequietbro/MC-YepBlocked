<p align="center"><img src="https://github.com/user-attachments/assets/9be0aa72-e2c5-456e-9acf-d303b790dc44" alt="YepBlocked Logo" height="100"></p>
<h1 align="center">YepBlocked</h1>

> [!NOTE]
> This repo contains **4 independent platform versions** - you only need the one matching your Minecraft loader. See [Build from source](#build-from-source) for details.

<h2 align="center">Description</h2>

YepBlocked is a Minecraft mod that gives you complete control over which entities can spawn and under what conditions. Configure it per-entity with fine-grained toggles for natural spawns, spawners, trial spawners, and spawn eggs - all through in-game commands.

<h2 align="center">Features</h2>

- **Global toggles** — block or allow all natural / spawner / trial spawner / egg spawns at once
- **Per-entity overrides** — fine-tune specific mobs (e.g. block phantom natural spawns but allow spawn eggs)
- **Glob & regex patterns** — use `minecraft:*` or `.*golem` to match entire groups of entities
- **Hot-reload** — apply config changes instantly with `/yb reload` (no restart required)
- **Commands** — `/yb add`, `/yb remove`, `/yb reload` with tab completion
- **Persistent config** — `config/YepBlocked.json`, auto-created on first run

<h2 align="center">Commands</h2>

All commands require operator privileges (permission level 2):

| Command | Description |
|---|---|
| `/yb reload` | Reload config from disk |
| `/yb add <entity> [flags]` | Add an override. Flags: `natural`, `spawner`, `trial`, `egg` — bare flag = block, `~` = allow, `key=false` = block, `key=true` = allow |
| `/yb remove <entity>` | Remove an override |

<h2 align="center">Installation</h2>

1. Download the mod JAR for your platform from the [Releases](../../releases) page.
2. Place it in your `mods` folder.
3. Install the required **Kotlin mod** for your loader:
   - **NeoForge**: Install [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge).
   - **Forge**: Install [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge).
   - **Fabric**: Install [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin).
5. Start the game — `config/YepBlocked.json` is created automatically.

<h2 align="center">Build from source</h2>

Each platform is a **fully self-contained Gradle project** with its own `gradlew`, `settings.gradle`, and a local copy of shared sources in `common/`.

**Prerequisites:**
- [JDK 21+](https://adoptium.net/) (JDK 17 for Forge 1.20.1; Gradle auto-downloads if missing)
- Internet connection (first build downloads Minecraft mappings and dependencies)

**Clone & navigate:**
```
git clone https://github.com/bequietbro/MC-YepBlocked.git
cd YepBlocked
```

**Build a specific platform:**
```
cd neoforge-1-21-1
.\gradlew build
```

Or test with unit tests:
```
.\gradlew check
```

**Platforms:**

| Directory | Loader | MC Version | Run client |
|---|---|---|---|
| `neoforge-1-21-1/` | NeoForge | 1.21.1 | `cd neoforge-1-21-1 && .\gradlew runClient` |
| `neoforge-26-1/` | NeoForge | 26.1 | `cd neoforge-26-1 && .\gradlew runClient` |
| `forge-1-20-1/` | Forge | 1.20.1 | `cd forge-1-20-1 && .\gradlew runClient` |
| `fabric-26-1/` | Fabric | 26.1 | `cd fabric-26-1 && .\gradlew runClient` |

<h2 align="center">Testing</h2>

Run all 65+ unit tests (config loading, command parsing, spawn predicates) for a single platform:

```
cd neoforge-1-21-1
.\gradlew check
```

Tests are located in `common/src/test/kotlin/` (shared) and cover:
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
