package dev.beqi.yepblocked.config

import com.google.gson.annotations.SerializedName

data class EntitySpawnConfig(
    @SerializedName("enableNaturalSpawn") val naturalEnabled: Boolean? = null,
    @SerializedName("enableSpawnerSpawn") val spawnerEnabled: Boolean? = null,
    @SerializedName("enableTrialSpawnerSpawn") val trialSpawnerEnabled: Boolean? = null,
    @SerializedName("enableEggSpawn") val eggEnabled: Boolean? = null
) {
    companion object {
        val BLOCK_ALL = EntitySpawnConfig(
            naturalEnabled = false, spawnerEnabled = false, trialSpawnerEnabled = false, eggEnabled = false
        )
    }

    val isBlockAll: Boolean get() =
        naturalEnabled == false && spawnerEnabled == false && trialSpawnerEnabled == false && eggEnabled == false

    val isUseGlobal: Boolean get() =
        naturalEnabled == null && spawnerEnabled == null && trialSpawnerEnabled == null && eggEnabled == null
}
