package dev.beqi.yepblocked.listener

import dev.beqi.yepblocked.SpawnPredicate
import dev.beqi.yepblocked.config.ConfigManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SpawnListener {
    private val LOGGER = LoggerFactory.getLogger("YepBlocked/SpawnListener")

    fun register() {
        ServerEntityEvents.ALLOW_LOAD.register(
            object : ServerEntityEvents.AllowLoad {
                override fun onAllowLoad(
                    entity: Entity,
                    level: ServerLevel,
                    spawnReason: EntitySpawnReason?,
                    loadedFromDisk: Boolean
                ): Boolean {
                    if (loadedFromDisk || spawnReason == null) return true
                    val entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type).toString()
                    val spawnType = spawnReason.name
                    val cancel = SpawnPredicate.shouldCancel(ConfigManager.getConfig(), spawnType, entityId)
                    if (cancel) LOGGER.info("[ENTITY] $entityId ($spawnType) -> BLOCKED")
                    return !cancel
                }
            }
        )
        ServerLifecycleEvents.SERVER_STARTING.register { ConfigManager.ensureExists() }
    }
}
