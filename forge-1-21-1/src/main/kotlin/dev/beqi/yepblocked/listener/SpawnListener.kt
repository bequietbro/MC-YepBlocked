package dev.beqi.yepblocked.listener

import dev.beqi.yepblocked.SpawnPredicate
import dev.beqi.yepblocked.config.ConfigManager
import net.minecraftforge.event.entity.living.MobSpawnEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraft.core.registries.BuiltInRegistries
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SpawnListener {
    private val LOGGER = LoggerFactory.getLogger("YepBlocked/SpawnListener")

    @SubscribeEvent
    fun onFinalizeSpawn(event: MobSpawnEvent.FinalizeSpawn) {
        val config = ConfigManager.getConfig()

        val entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.entity.type).toString()
        val spawnType = event.spawnType.name
        val cancel = SpawnPredicate.shouldCancel(config, spawnType, entityId)
        if (cancel) {
            LOGGER.info("[ENTITY] $entityId ($spawnType) -> BLOCKED")
            event.isSpawnCancelled = true
        }
    }
}
