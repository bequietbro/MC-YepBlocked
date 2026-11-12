package dev.beqi.yepblocked

import dev.beqi.yepblocked.command.AdminCommands
import dev.beqi.yepblocked.config.ConfigManager
import dev.beqi.yepblocked.listener.SpawnListener
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent
import java.util.function.Consumer

@Mod(Yepblocked.ID)
object Yepblocked {
    const val ID = "yepblocked"

    init {
        val configPath = FMLPaths.CONFIGDIR.get().resolve("YepBlocked.json")
        ConfigManager.init(configPath)
        NeoForge.EVENT_BUS.register(SpawnListener)
        NeoForge.EVENT_BUS.addListener(Consumer<ServerAboutToStartEvent> { ConfigManager.ensureExists() })
        NeoForge.EVENT_BUS.register(AdminCommands)
    }
}
