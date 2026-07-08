package dev.beqi.yepblocked

import dev.beqi.yepblocked.config.EntitySpawnConfig
import dev.beqi.yepblocked.config.YepBlockedConfig
import java.util.concurrent.ConcurrentHashMap

object SpawnPredicate {

    private val compiledPatterns = ConcurrentHashMap<String, Regex>()
    private val regexSpecials = setOf('.', '+', '?', '^', '$', '(', ')', '[', ']', '{', '}', '|', '\\')

    private fun compilePattern(pattern: String): Regex {
        return if (pattern.any { it in regexSpecials }) {
            pattern.toRegex()
        } else {
            pattern.replace("*", ".*").toRegex()
        }
    }

    private enum class SpawnCategory(
        val global: (YepBlockedConfig) -> Boolean,
        val override: (EntitySpawnConfig) -> Boolean?
    ) {
        NATURAL(
            global = { it.enableNaturalSpawn },
            override = { it.naturalEnabled }
        ),
        SPAWNER(
            global = { it.enableSpawnerSpawn },
            override = { it.spawnerEnabled }
        ),
        TRIAL_SPAWNER(
            global = { it.enableTrialSpawnerSpawn },
            override = { it.trialSpawnerEnabled }
        ),
        EGG(
            global = { it.enableEggSpawn },
            override = { it.eggEnabled }
        );

        companion object {
            private val TYPES = mapOf(
                "NATURAL" to NATURAL,
                "CHUNK_GENERATION" to NATURAL,
                "PATROL" to NATURAL,
                "REINFORCEMENT" to NATURAL,
                "JOCKEY" to NATURAL,
                "EVENT" to NATURAL,
                "TRIGGERED" to NATURAL,
                "BREEDING" to NATURAL,
                "CONVERSION" to NATURAL,
                "MOB_SUMMONED" to NATURAL,
                "STRUCTURE" to NATURAL,
                "SPAWNER" to SPAWNER,
                "TRIAL_SPAWNER" to TRIAL_SPAWNER,
                "SPAWN_EGG" to EGG,
                "DISPENSER" to EGG,
                "SPAWN_ITEM_USE" to EGG,
                "DIMENSION_TRAVEL" to NATURAL
            )

            fun forType(spawnType: String): SpawnCategory? = TYPES[spawnType]
        }
    }

    fun clearCache() {
        compiledPatterns.clear()
    }

    private fun applyOverride(override: EntitySpawnConfig, spawnType: String): Boolean? {
        if (override.isBlockAll) return true
        if (!override.isUseGlobal) {
            val category = SpawnCategory.forType(spawnType)
            val enabled = category?.override?.invoke(override)
            if (enabled != null) return !enabled
        }
        return null
    }

    fun shouldCancel(config: YepBlockedConfig, spawnType: String, entityId: String): Boolean {
        if (spawnType == "COMMAND" || spawnType == "BUCKET") return false

        val exactOverride = config.entityOverrides[entityId]
        if (exactOverride != null) {
            val result = applyOverride(exactOverride, spawnType)
            if (result != null) return result
        } else {
            for ((pattern, override) in config.entityOverrides) {
                val regex = compiledPatterns.computeIfAbsent(pattern) { compilePattern(it) }
                if (regex.matches(entityId)) {
                    val result = applyOverride(override, spawnType)
                    if (result != null) return result
                }
            }
        }

        val category = SpawnCategory.forType(spawnType)
        return category != null && !category.global(config)
    }
}
