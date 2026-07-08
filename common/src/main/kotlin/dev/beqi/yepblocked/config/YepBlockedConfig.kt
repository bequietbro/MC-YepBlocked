package dev.beqi.yepblocked.config

import com.google.gson.annotations.SerializedName

data class YepBlockedConfig(
    @SerializedName("enableNaturalSpawn") val enableNaturalSpawn: Boolean = true,
    @SerializedName("enableSpawnerSpawn") val enableSpawnerSpawn: Boolean = true,
    @SerializedName("enableTrialSpawnerSpawn") val enableTrialSpawnerSpawn: Boolean = true,
    @SerializedName("enableEggSpawn") val enableEggSpawn: Boolean = true,
    @SerializedName("entityOverrides") val entityOverrides: Map<String, EntitySpawnConfig> = emptyMap()
)
