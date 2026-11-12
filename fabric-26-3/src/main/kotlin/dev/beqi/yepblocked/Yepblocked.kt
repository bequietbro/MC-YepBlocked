package dev.beqi.yepblocked

import dev.beqi.yepblocked.command.AdminCommands
import dev.beqi.yepblocked.config.ConfigManager
import dev.beqi.yepblocked.listener.SpawnListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

object Yepblocked : ModInitializer {
    const val ID = "yepblocked"

    override fun onInitialize() {
        val configPath = FabricLoader.getInstance().configDir.resolve("YepBlocked.json")
        ConfigManager.init(configPath)

        SpawnListener.register()
        AdminCommands.register()
    }
}
