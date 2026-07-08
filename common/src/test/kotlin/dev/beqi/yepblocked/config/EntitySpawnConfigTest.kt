package dev.beqi.yepblocked.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EntitySpawnConfigTest {

    @Test
    fun `isBlockAll true when all fields false`() {
        val config = EntitySpawnConfig(naturalEnabled = false, spawnerEnabled = false, trialSpawnerEnabled = false, eggEnabled = false)
        assertTrue(config.isBlockAll)
    }

    @Test
    fun `isBlockAll false when any field null`() {
        assertFalse(EntitySpawnConfig(naturalEnabled = false, spawnerEnabled = false, trialSpawnerEnabled = false).isBlockAll)
        assertFalse(EntitySpawnConfig(naturalEnabled = false, spawnerEnabled = false, eggEnabled = false).isBlockAll)
        assertFalse(EntitySpawnConfig(naturalEnabled = false, trialSpawnerEnabled = false, eggEnabled = false).isBlockAll)
        assertFalse(EntitySpawnConfig(spawnerEnabled = false, trialSpawnerEnabled = false, eggEnabled = false).isBlockAll)
    }

    @Test
    fun `isBlockAll false when any field true`() {
        assertFalse(EntitySpawnConfig(naturalEnabled = true).isBlockAll)
        assertFalse(EntitySpawnConfig(spawnerEnabled = true).isBlockAll)
        assertFalse(EntitySpawnConfig(trialSpawnerEnabled = true).isBlockAll)
        assertFalse(EntitySpawnConfig(eggEnabled = true).isBlockAll)
    }

    @Test
    fun `isUseGlobal true when all fields null`() {
        assertTrue(EntitySpawnConfig().isUseGlobal)
    }

    @Test
    fun `isUseGlobal false when any field set`() {
        assertFalse(EntitySpawnConfig(naturalEnabled = true).isUseGlobal)
        assertFalse(EntitySpawnConfig(spawnerEnabled = false).isUseGlobal)
        assertFalse(EntitySpawnConfig(trialSpawnerEnabled = true).isUseGlobal)
        assertFalse(EntitySpawnConfig(eggEnabled = false).isUseGlobal)
    }

    @Test
    fun `BLOCK_ALL blocks everything`() {
        assertTrue(EntitySpawnConfig.BLOCK_ALL.isBlockAll)
        assertFalse(EntitySpawnConfig.BLOCK_ALL.isUseGlobal)
    }

    @Test
    fun `mixed null and false is not blockAll and not useGlobal`() {
        val config = EntitySpawnConfig(naturalEnabled = false)
        assertFalse(config.isBlockAll)
        assertFalse(config.isUseGlobal)
    }
}
