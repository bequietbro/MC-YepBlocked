package dev.beqi.yepblocked

import dev.beqi.yepblocked.config.EntitySpawnConfig
import dev.beqi.yepblocked.config.YepBlockedConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpawnPredicateTest {

    @BeforeEach
    fun setUp() {
        SpawnPredicate.clearCache()
    }

    @Test
    fun `COMMAND spawn is always allowed`() {
        val config = YepBlockedConfig(enableNaturalSpawn = false)
        assertFalse(SpawnPredicate.shouldCancel(config, "COMMAND", "minecraft:zombie"))
    }

    @Test
    fun `BUCKET spawn is always allowed`() {
        val config = YepBlockedConfig(enableNaturalSpawn = false)
        assertFalse(SpawnPredicate.shouldCancel(config, "BUCKET", "minecraft:zombie"))
    }

    @Test
    fun `NATURAL cancelled when global toggle false`() {
        val config = YepBlockedConfig(enableNaturalSpawn = false)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
    }

    @Test
    fun `NATURAL allowed when global toggle true`() {
        val config = YepBlockedConfig(enableNaturalSpawn = true)
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
    }

    @Test
    fun `BLOCK_ALL override blocks entity everywhere`() {
        val overrides = mapOf("minecraft:zombie" to EntitySpawnConfig.BLOCK_ALL)
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWNER", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWN_EGG", "minecraft:zombie"))
    }

    @Test
    fun `specific type override blocks only that type`() {
        val overrides = mapOf("minecraft:zombie" to EntitySpawnConfig(naturalEnabled = false))
        val config = YepBlockedConfig(
            enableNaturalSpawn = true, enableSpawnerSpawn = true, entityOverrides = overrides
        )
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertFalse(SpawnPredicate.shouldCancel(config, "SPAWNER", "minecraft:zombie"))
    }

    @Test
    fun `override with useGlobal falls back to global toggle`() {
        val overrides = mapOf("minecraft:zombie" to EntitySpawnConfig())
        val config = YepBlockedConfig(enableNaturalSpawn = false, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
    }

    @Test
    fun `override with useGlobal and enabled global - entity allowed`() {
        val overrides = mapOf("minecraft:zombie" to EntitySpawnConfig())
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
    }

    @Test
    fun `regex in entityOverrides matches entity ID`() {
        val overrides = mapOf(
            ".*zombie.*" to EntitySpawnConfig.BLOCK_ALL
        )
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie_villager"))
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:skeleton"))
    }

    @Test
    fun `regex with specific override blocks only those types`() {
        val overrides = mapOf(
            ".*zombie.*" to EntitySpawnConfig(naturalEnabled = false, spawnerEnabled = true)
        )
        val config = YepBlockedConfig(
            enableNaturalSpawn = true, enableSpawnerSpawn = true, entityOverrides = overrides
        )
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertFalse(SpawnPredicate.shouldCancel(config, "SPAWNER", "minecraft:zombie"))
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:skeleton"))
    }

    @Test
    fun `glob star matches any characters`() {
        val overrides = mapOf(
            "minecraft:zombie*" to EntitySpawnConfig.BLOCK_ALL
        )
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie_villager"))
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:skeleton"))
    }

    @Test
    fun `glob star at end matches suffix`() {
        val overrides = mapOf(
            "*spider" to EntitySpawnConfig.BLOCK_ALL
        )
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:spider"))
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:cave_spider"))
        assertFalse(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
    }

    @Test
    fun `clearCache does not break regex matching`() {
        val overrides = mapOf(
            "minecraft:creeper" to EntitySpawnConfig.BLOCK_ALL
        )
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:creeper"))
        SpawnPredicate.clearCache()
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:creeper"))
    }

    @Test
    fun `explicit override takes priority over regex`() {
        val overrides = mapOf(
            "minecraft:zombie" to EntitySpawnConfig(naturalEnabled = false),
            "minecraft:.*" to EntitySpawnConfig.BLOCK_ALL
        )
        val config = YepBlockedConfig(enableNaturalSpawn = true, entityOverrides = overrides)
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertFalse(SpawnPredicate.shouldCancel(config, "SPAWNER", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:skeleton"))
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie_villager"))
    }

    @Test
    fun `unknown spawn type is allowed`() {
        val config = YepBlockedConfig(enableNaturalSpawn = false)
        assertFalse(SpawnPredicate.shouldCancel(config, "UNKNOWN", "minecraft:zombie"))
    }

    @Test
    fun `global toggles affect respective spawn types`() {
        val config = YepBlockedConfig(
            enableNaturalSpawn = false, enableSpawnerSpawn = false,
            enableTrialSpawnerSpawn = false, enableEggSpawn = false
        )
        assertTrue(SpawnPredicate.shouldCancel(config, "NATURAL", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWNER", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "TRIAL_SPAWNER", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWN_EGG", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWN_ITEM_USE", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "DIMENSION_TRAVEL", "minecraft:zombie"))
    }

    @Test
    fun `natural spawn type aliases map to NATURAL`() {
        val config = YepBlockedConfig(enableNaturalSpawn = false)
        val naturalAliases = listOf(
            "NATURAL", "CHUNK_GENERATION", "PATROL", "REINFORCEMENT", "JOCKEY",
            "EVENT", "TRIGGERED", "BREEDING", "CONVERSION", "MOB_SUMMONED", "STRUCTURE",
            "DIMENSION_TRAVEL"
        )
        for (type in naturalAliases) {
            assertTrue(SpawnPredicate.shouldCancel(config, type, "minecraft:zombie"))
        }
    }

    @Test
    fun `egg aliases map to EGG toggle`() {
        val config = YepBlockedConfig(enableEggSpawn = false)
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWN_EGG", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "DISPENSER", "minecraft:zombie"))
        assertTrue(SpawnPredicate.shouldCancel(config, "SPAWN_ITEM_USE", "minecraft:zombie"))
    }
}
