package dev.beqi.yepblocked.command

import dev.beqi.yepblocked.config.EntitySpawnConfig
import dev.beqi.yepblocked.config.YepBlockedConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandLogicTest {

    private fun parseOk(flags: String): EntitySpawnConfig {
        val result = CommandLogic.parseAddFlags(flags)
        assertTrue(result.isSuccess) { "Expected success for '$flags' but got errors: ${result.errors}" }
        return result.config!!
    }

    @Test
    fun `parse empty flags returns BLOCK_ALL`() {
        val result = parseOk("")
        assertTrue(result.isBlockAll)
    }

    @Test
    fun `parse bare flag sets field to false`() {
        val result = parseOk("natural")
        assertEquals(false, result.naturalEnabled)
        assertNull(result.spawnerEnabled)
        assertNull(result.trialSpawnerEnabled)
        assertNull(result.eggEnabled)
    }

    @Test
    fun `parse tildy flag sets field to true`() {
        val result = parseOk("~natural")
        assertEquals(true, result.naturalEnabled)
        assertNull(result.spawnerEnabled)
        assertNull(result.trialSpawnerEnabled)
        assertNull(result.eggEnabled)
    }

    @Test
    fun `parse explicit false equals bare`() {
        assertEquals(
            parseOk("natural"),
            parseOk("natural=false")
        )
    }

    @Test
    fun `parse explicit true equals tildy`() {
        assertEquals(
            parseOk("~natural"),
            parseOk("natural=true")
        )
    }

    @Test
    fun `parse multiple flags`() {
        val result = parseOk("natural=false spawner=true ~trial egg")
        assertEquals(false, result.naturalEnabled)
        assertEquals(true, result.spawnerEnabled)
        assertEquals(true, result.trialSpawnerEnabled)
        assertEquals(false, result.eggEnabled)
    }

    @Test
    fun `parse trial alias maps to trialSpawnerEnabled`() {
        val result = parseOk("trial")
        assertEquals(false, result.trialSpawnerEnabled)
    }

    @Test
    fun `parse trialspawner without underscore maps to trialSpawnerEnabled`() {
        val result = parseOk("trialspawner")
        assertEquals(false, result.trialSpawnerEnabled)
    }

    @Test
    fun `parse whitespace padding`() {
        val result = parseOk("  natural  spawner  ")
        assertEquals(false, result.naturalEnabled)
        assertEquals(false, result.spawnerEnabled)
    }

    @Test
    fun `parse invalid flag value returns error`() {
        val result = CommandLogic.parseAddFlags("natural=yeah")
        assertTrue(result.isError)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("yeah"))
        assertNull(result.config)
    }

    @Test
    fun `parse multiple invalid flags returns all errors`() {
        val result = CommandLogic.parseAddFlags("natural=foo spawner=bar")
        assertTrue(result.isError)
        assertEquals(2, result.errors.size)
        assertNull(result.config)
    }

    @Test
    fun `parse valid and invalid flags mixed returns errors`() {
        val result = CommandLogic.parseAddFlags("natural=true egg=baz")
        assertTrue(result.isError)
        assertNull(result.config)
    }

    @Test
    fun `parse tilde with equals returns error`() {
        val result = CommandLogic.parseAddFlags("~natural=false")
        assertTrue(result.isError)
        assertTrue(result.errors[0].contains("~"))
        assertNull(result.config)
    }

    @Test
    fun `parse empty flag name returns error`() {
        val result = CommandLogic.parseAddFlags("=true")
        assertTrue(result.isError)
        assertTrue(result.errors[0].contains("Empty"))
        assertNull(result.config)
    }

    @Test
    fun `parse unknown flag returns error`() {
        val result = CommandLogic.parseAddFlags("unknownflag=true")
        assertTrue(result.isError)
        assertTrue(result.errors[0].contains("unknownflag"))
        assertNull(result.config)
    }

    @Test
    fun `parse unknown flag without value returns error`() {
        val result = CommandLogic.parseAddFlags("unknownflag")
        assertTrue(result.isError)
        assertTrue(result.errors[0].contains("unknownflag"))
        assertNull(result.config)
    }

    @Test
    fun `parse mixed known unknown flags returns all errors`() {
        val result = CommandLogic.parseAddFlags("natural=true unknownflag=true")
        assertTrue(result.isError)
        assertEquals(1, result.errors.size)
        assertNull(result.config)
    }

    @Test
    fun `buildReloadMessage returns colored text`() {
        val msg = CommandLogic.buildReloadMessage()
        assertTrue(msg.startsWith("§a"))
        assertTrue(msg.contains("reloaded", ignoreCase = true))
    }

    @Test
    fun `buildAddResult contains entity name`() {
        val msg = CommandLogic.buildAddResult("minecraft:creeper")
        assertTrue(msg.contains("creeper"))
    }

    @Test
    fun `buildRemoveResult success contains entity`() {
        val msg = CommandLogic.buildRemoveResult("minecraft:creeper", true)
        assertTrue(msg.contains("creeper"))
        assertFalse(msg.contains("not found"))
    }

    @Test
    fun `buildRemoveResult failure contains not found`() {
        val msg = CommandLogic.buildRemoveResult("minecraft:creeper", false)
        assertTrue(msg.contains("creeper"))
        assertTrue(msg.contains("not found"))
    }

    @Test
    fun `addOverride adds entity to empty config`() {
        val config = YepBlockedConfig()
        val override = EntitySpawnConfig.BLOCK_ALL
        val result = CommandLogic.addOverride(config, "minecraft:creeper", override)
        assertEquals(1, result.entityOverrides.size)
        assertSame(override, result.entityOverrides["minecraft:creeper"])
    }

    @Test
    fun `addOverride replaces existing entity`() {
        val config = YepBlockedConfig(entityOverrides = mapOf(
            "minecraft:creeper" to EntitySpawnConfig.BLOCK_ALL
        ))
        val newOverride = EntitySpawnConfig(naturalEnabled = false)
        val result = CommandLogic.addOverride(config, "minecraft:creeper", newOverride)
        assertEquals(1, result.entityOverrides.size)
        assertSame(newOverride, result.entityOverrides["minecraft:creeper"])
    }

    @Test
    fun `addOverride preserves unrelated overrides`() {
        val config = YepBlockedConfig(entityOverrides = mapOf(
            "minecraft:zombie" to EntitySpawnConfig.BLOCK_ALL
        ))
        val result = CommandLogic.addOverride(config, "minecraft:creeper", EntitySpawnConfig.BLOCK_ALL)
        assertEquals(2, result.entityOverrides.size)
        assertTrue(result.entityOverrides.containsKey("minecraft:zombie"))
        assertTrue(result.entityOverrides.containsKey("minecraft:creeper"))
    }

    @Test
    fun `removeOverride returns null for unknown entity`() {
        val config = YepBlockedConfig(entityOverrides = mapOf(
            "minecraft:creeper" to EntitySpawnConfig.BLOCK_ALL
        ))
        assertNull(CommandLogic.removeOverride(config, "minecraft:zombie"))
    }

    @Test
    fun `removeOverride removes existing entity`() {
        val config = YepBlockedConfig(entityOverrides = mapOf(
            "minecraft:creeper" to EntitySpawnConfig.BLOCK_ALL,
            "minecraft:zombie" to EntitySpawnConfig.BLOCK_ALL
        ))
        val result = CommandLogic.removeOverride(config, "minecraft:creeper")
        assertNotNull(result)
        assertEquals(1, result!!.entityOverrides.size)
        assertFalse(result.entityOverrides.containsKey("minecraft:creeper"))
        assertTrue(result.entityOverrides.containsKey("minecraft:zombie"))
    }
}
