package dev.beqi.yepblocked.config

import com.google.gson.*
import dev.beqi.yepblocked.SpawnPredicate
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Path
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object ConfigManager {
    private val LOGGER: Logger = LoggerFactory.getLogger("YepBlocked")
    @Volatile
    private var config: YepBlockedConfig = YepBlockedConfig()
    @Volatile
    private var configPath: Path? = null
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(EntitySpawnConfig::class.java, EntitySpawnConfigAdapter())
        .create()

    private const val OLD_CONFIG_NAME = "yepblocked.json"

    fun init(path: Path) {
        configPath = path
        migrateOldConfig(path)
        val isNew = !path.exists()
        if (isNew) {
            generateDefaultConfig(path)
            LOGGER.info("Generated default config at $path")
        }
        loadConfig()
    }

    private fun migrateOldConfig(path: Path) {
        val oldPath = path.resolveSibling(OLD_CONFIG_NAME)
        if (oldPath.exists() && !path.exists()) {
            try {
                path.parent.createDirectories()
                java.nio.file.Files.copy(oldPath, path, StandardCopyOption.REPLACE_EXISTING)
                java.nio.file.Files.deleteIfExists(oldPath)
                LOGGER.info("Migrated old config $OLD_CONFIG_NAME to ${path.fileName}")
            } catch (e: IOException) {
                LOGGER.warn("Failed to migrate old config: ${e.message}")
            }
        }
    }

    private fun generateDefaultConfig(path: Path) {
        path.parent.createDirectories()
        val defaultConfig = YepBlockedConfig()
        path.writeText(gson.toJson(defaultConfig))
    }

    private fun loadConfig() {
        val path = configPath ?: return
        try {
            val text = path.readText()
            if (text.isBlank()) throw JsonSyntaxException("Empty config file")
            val parsed = gson.fromJson(text, YepBlockedConfig::class.java)
            if (parsed != null) config = parsed
            LOGGER.info("Loaded config (${config.entityOverrides.size} overrides)")
        } catch (e: JsonSyntaxException) {
            LOGGER.error("Invalid JSON syntax in config: ${e.message}")
            LOGGER.error("Keeping previous config")
        } catch (e: IOException) {
            LOGGER.error("Failed to read config file: ${e.message}")
            LOGGER.error("Keeping previous config")
        } catch (e: Exception) {
            LOGGER.error("Unexpected error loading config", e)
            LOGGER.error("Keeping previous config")
        }
    }

    private fun saveToDisk(path: Path, cfg: YepBlockedConfig) {
        try {
            path.parent.createDirectories()
            path.writeText(gson.toJson(cfg))
        } catch (e: IOException) {
            LOGGER.warn("Failed to save config: ${e.message}")
        }
    }

    fun updateAndSave(newConfig: YepBlockedConfig) {
        config = newConfig
        SpawnPredicate.clearCache()
        SpawnPredicate.clearCache()
        configPath?.let { saveToDisk(it, config) }
    }

    fun ensureExists() {
        val path = configPath ?: return
        if (!path.exists()) {
            generateDefaultConfig(path)
            LOGGER.info("Re-generated missing config at $path")
            loadConfig()
            SpawnPredicate.clearCache()
        }
    }

    fun reload() {
        SpawnPredicate.clearCache()
        loadConfig()
    }

    fun getConfig(): YepBlockedConfig = config

    private class EntitySpawnConfigAdapter : JsonDeserializer<EntitySpawnConfig>, JsonSerializer<EntitySpawnConfig> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EntitySpawnConfig {
            return when {
                json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> {
                    if (json.asBoolean) EntitySpawnConfig()
                    else EntitySpawnConfig.BLOCK_ALL
                }
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    EntitySpawnConfig(
                        naturalEnabled = obj.get("enableNaturalSpawn")?.asBoolean,
                        spawnerEnabled = obj.get("enableSpawnerSpawn")?.asBoolean,
                        trialSpawnerEnabled = obj.get("enableTrialSpawnerSpawn")?.asBoolean,
                        eggEnabled = obj.get("enableEggSpawn")?.asBoolean
                    )
                }
                else -> throw JsonParseException("Invalid entity override entry")
            }
        }

        override fun serialize(src: EntitySpawnConfig, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()
            src.naturalEnabled?.let { obj.addProperty("enableNaturalSpawn", it) }
            src.spawnerEnabled?.let { obj.addProperty("enableSpawnerSpawn", it) }
            src.trialSpawnerEnabled?.let { obj.addProperty("enableTrialSpawnerSpawn", it) }
            src.eggEnabled?.let { obj.addProperty("enableEggSpawn", it) }
            return obj
        }
    }
}
