package dev.beqi.yepblocked.listener

import dev.beqi.yepblocked.SpawnPredicate
import dev.beqi.yepblocked.config.ConfigManager
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SpawnListener {
    private val LOGGER = LoggerFactory.getLogger("YepBlocked/SpawnListener")

    @SubscribeEvent
    fun onFinalizeSpawn(event: FinalizeSpawnEvent) {
        val entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.entity.type).toString()
        val spawnType = event.spawnType.name
        val cancel = SpawnPredicate.shouldCancel(ConfigManager.getConfig(), spawnType, entityId)
        if (cancel) {
            LOGGER.info("[ENTITY] $entityId ($spawnType) -> BLOCKED")
            event.isSpawnCancelled = true
        }
    }
}
