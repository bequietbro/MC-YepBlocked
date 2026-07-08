package dev.beqi.yepblocked.command

import dev.beqi.yepblocked.config.EntitySpawnConfig
import dev.beqi.yepblocked.config.YepBlockedConfig
import dev.beqi.yepblocked.config.ConfigManager

private data class ParsedFlag(val key: String, val value: Boolean, val error: String?)

data class ParseFlagsResult(val config: EntitySpawnConfig?, val errors: List<String>) {
    val isSuccess: Boolean get() = errors.isEmpty()
    val isError: Boolean get() = errors.isNotEmpty()
}

object CommandLogic {

    val SUGGESTED_FLAGS = listOf("natural", "spawner", "trial", "egg")
    val WHITESPACE = Regex("\\s+")

    fun buildReloadMessage(): String = "§aYepBlocked config reloaded"

    fun buildAddResult(entity: String): String = "§aAdded §e$entity §ato overrides"

    fun buildRemoveResult(entity: String, success: Boolean): String =
        if (success) "§aRemoved §e$entity §afrom overrides"
        else "§c$entity §cnot found in overrides"

    fun parseAddFlags(flags: String): ParseFlagsResult {
        if (flags.isBlank()) return ParseFlagsResult(EntitySpawnConfig.BLOCK_ALL, emptyList())

        var naturalEnabled: Boolean? = null
        var spawnerEnabled: Boolean? = null
        var trialSpawnerEnabled: Boolean? = null
        var eggEnabled: Boolean? = null
        val errors = mutableListOf<String>()

        for (part in flags.trim().split(WHITESPACE)) {
            if (part.isBlank()) continue
            val parsed = when {
                part.startsWith("~") -> {
                    val rest = part.removePrefix("~")
                    if ("=" in rest) ParsedFlag(rest, true, "Cannot use ~ prefix with =value syntax for flag '$rest'.")
                    else ParsedFlag(rest, true, null)
                }
                "=" in part -> {
                    val idx = part.indexOf("=")
                    val k = part.substring(0, idx)
                    if (k.isBlank()) ParsedFlag(k, false, "Empty flag name in '$part'.")
                    else {
                        val v = part.substring(idx + 1).lowercase().toBooleanStrictOrNull()
                        if (v == null) ParsedFlag(k, false, "Invalid value '${part.substring(idx + 1)}' for flag '$k'. Use true or false.")
                        else ParsedFlag(k, v, null)
                    }
                }
                else -> ParsedFlag(part, false, null)
            }
            if (parsed.error != null) {
                errors.add("§c${parsed.error}")
                continue
            }
            when (parsed.key.lowercase()) {
                "natural" -> naturalEnabled = parsed.value
                "spawner" -> spawnerEnabled = parsed.value
                "trial", "trialspawner" -> trialSpawnerEnabled = parsed.value
                "egg" -> eggEnabled = parsed.value
                else -> errors.add("§cUnknown flag '${parsed.key}'.")
            }
        }

        if (errors.isNotEmpty()) return ParseFlagsResult(null, errors)
        return ParseFlagsResult(EntitySpawnConfig(naturalEnabled, spawnerEnabled, trialSpawnerEnabled, eggEnabled), emptyList())
    }

    fun addOverride(
        config: YepBlockedConfig,
        entity: String,
        override: EntitySpawnConfig
    ): YepBlockedConfig {
        val newOverrides = config.entityOverrides.toMutableMap()
        newOverrides[entity] = override
        return config.copy(entityOverrides = newOverrides)
    }

    fun removeOverride(
        config: YepBlockedConfig,
        entity: String
    ): YepBlockedConfig? {
        if (entity !in config.entityOverrides) return null
        val newOverrides = config.entityOverrides.toMutableMap()
        newOverrides.remove(entity)
        return config.copy(entityOverrides = newOverrides)
    }

    fun applyGlobalFlags(config: YepBlockedConfig, parsed: EntitySpawnConfig): YepBlockedConfig {
        return config.copy(
            enableNaturalSpawn = parsed.naturalEnabled ?: config.enableNaturalSpawn,
            enableSpawnerSpawn = parsed.spawnerEnabled ?: config.enableSpawnerSpawn,
            enableTrialSpawnerSpawn = parsed.trialSpawnerEnabled ?: config.enableTrialSpawnerSpawn,
            enableEggSpawn = parsed.eggEnabled ?: config.enableEggSpawn
        )
    }

    fun buildGlobalStatus(): String {
        val cfg = ConfigManager.getConfig()
        val parts = mutableListOf<String>()
        if (!cfg.enableNaturalSpawn) parts.add("§cnatural")
        else parts.add("§anatural")
        if (!cfg.enableSpawnerSpawn) parts.add("§cspawner")
        else parts.add("§aspawner")
        if (!cfg.enableTrialSpawnerSpawn) parts.add("§ctrial")
        else parts.add("§atrial")
        if (!cfg.enableEggSpawn) parts.add("§cegg")
        else parts.add("§aegg")
        return "§7Global spawn settings: ${parts.joinToString(" §8|§7 ")}"
    }

    fun buildGlobalResult(): String = "§aGlobal spawn settings updated"
}
