package dev.beqi.yepblocked.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ConfigManagerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `init creates default config file when missing`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)

        assertTrue(Files.exists(path))
        val config = ConfigManager.getConfig()
        assertTrue(config.enableNaturalSpawn)
        assertTrue(config.enableSpawnerSpawn)
        assertTrue(config.enableTrialSpawnerSpawn)
        assertTrue(config.enableEggSpawn)
        assertTrue(config.entityOverrides.isEmpty())
    }

    @Test
    fun `init reads existing config file`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(path, """{"enableNaturalSpawn":false,"enableSpawnerSpawn":true,"enableEggSpawn":false,"entityOverrides":{"minecraft:zombie":false}}""")

        ConfigManager.init(path)
        val config = ConfigManager.getConfig()
        assertFalse(config.enableNaturalSpawn)
        assertTrue(config.enableSpawnerSpawn)
        assertFalse(config.enableEggSpawn)
        assertFalse(config.entityOverrides["minecraft:zombie"]!!.isUseGlobal)
        assertTrue(config.entityOverrides["minecraft:zombie"]!!.isBlockAll)
    }

    @Test
    fun `reload picks up config changes`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)
        assertTrue(ConfigManager.getConfig().enableNaturalSpawn)

        Files.writeString(path, """{"enableNaturalSpawn":false,"entityOverrides":{}}""")
        ConfigManager.reload()
        assertFalse(ConfigManager.getConfig().enableNaturalSpawn)
    }

    @Test
    fun `invalid JSON keeps previous config`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)
        assertTrue(ConfigManager.getConfig().enableNaturalSpawn)

        Files.writeString(path, "not json at all")
        ConfigManager.reload()
        assertTrue(ConfigManager.getConfig().enableNaturalSpawn)
    }

    @Test
    fun `boolean true override uses global defaults`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(path, """{"enableNaturalSpawn":true,"entityOverrides":{"minecraft:zombie":true}}""")
        ConfigManager.init(path)

        assertTrue(ConfigManager.getConfig().entityOverrides["minecraft:zombie"]!!.isUseGlobal)
    }

    @Test
    fun `boolean false override blocks all`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(path, """{"enableNaturalSpawn":true,"entityOverrides":{"minecraft:zombie":false}}""")
        ConfigManager.init(path)

        assertTrue(ConfigManager.getConfig().entityOverrides["minecraft:zombie"]!!.isBlockAll)
    }

    @Test
    fun `object override parses individual fields`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(
            path,
            """{"enableNaturalSpawn":true,"entityOverrides":{"minecraft:zombie":{"enableNaturalSpawn":false,"enableSpawnerSpawn":true}}}"""
        )
        ConfigManager.init(path)

        val override = ConfigManager.getConfig().entityOverrides["minecraft:zombie"]
        assertNotNull(override)
        assertFalse(override!!.naturalEnabled!!)
        assertTrue(override.spawnerEnabled!!)
        assertNull(override.trialSpawnerEnabled)
    }

    @Test
    fun `missing fields use defaults`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(path, """{"entityOverrides":{}}""")
        ConfigManager.init(path)

        val config = ConfigManager.getConfig()
        assertTrue(config.enableNaturalSpawn)
        assertTrue(config.enableSpawnerSpawn)
        assertTrue(config.enableTrialSpawnerSpawn)
        assertTrue(config.enableEggSpawn)
    }

    @Test
    fun `migrates old yepblocked json to YepBlocked`() {
        val oldPath = tempDir.resolve("yepblocked.json")
        val newPath = tempDir.resolve("YepBlocked.json")
        Files.writeString(oldPath, """{"enableNaturalSpawn":false,"entityOverrides":{}}""")

        ConfigManager.init(newPath)
        assertTrue(Files.exists(newPath))
        assertFalse(ConfigManager.getConfig().enableNaturalSpawn)
    }

    @Test
    fun `regex inside entityOverrides is parsed as normal override`() {
        val path = tempDir.resolve("YepBlocked.json")
        Files.writeString(
            path,
            """{"enableNaturalSpawn":true,"entityOverrides":{"minecraft:zombie_*":{"enableNaturalSpawn":false,"enableEggSpawn":true}}}"""
        )

        ConfigManager.init(path)
        val override = ConfigManager.getConfig().entityOverrides["minecraft:zombie_*"]
        assertNotNull(override)
        assertFalse(override!!.naturalEnabled!!)
        assertTrue(override.eggEnabled!!)
        assertNull(override.spawnerEnabled)
    }

    @Test
    fun `ensureExists recreates missing config`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)
        val oldCount = ConfigManager.getConfig().entityOverrides.size

        Files.deleteIfExists(path)
        assertFalse(Files.exists(path))

        ConfigManager.ensureExists()
        assertTrue(Files.exists(path))
        assertEquals(oldCount, ConfigManager.getConfig().entityOverrides.size)
    }

    @Test
    fun `updateAndSave writes new config to disk`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)
        assertTrue(ConfigManager.getConfig().enableNaturalSpawn)

        val newConfig = YepBlockedConfig(
            enableNaturalSpawn = false,
            entityOverrides = mapOf("minecraft:creeper" to EntitySpawnConfig.BLOCK_ALL)
        )
        ConfigManager.updateAndSave(newConfig)

        val content = Files.readString(path)
        assertTrue(content.contains("\"enableNaturalSpawn\": false"))
        assertTrue(content.contains("minecraft:creeper"))
        assertFalse(ConfigManager.getConfig().enableNaturalSpawn)
        assertEquals("minecraft:creeper", ConfigManager.getConfig().entityOverrides.keys.first())
    }

    @Test
    fun `updateAndSave persists across reload`() {
        val path = tempDir.resolve("YepBlocked.json")
        ConfigManager.init(path)

        val newConfig = YepBlockedConfig(
            enableNaturalSpawn = true, enableEggSpawn = false,
            entityOverrides = mapOf("minecraft:bat" to EntitySpawnConfig(naturalEnabled = false))
        )
        ConfigManager.updateAndSave(newConfig)

        ConfigManager.reload()
        assertTrue(ConfigManager.getConfig().enableNaturalSpawn)
        assertFalse(ConfigManager.getConfig().enableEggSpawn)
        assertFalse(ConfigManager.getConfig().entityOverrides["minecraft:bat"]!!.naturalEnabled!!)
    }
}
