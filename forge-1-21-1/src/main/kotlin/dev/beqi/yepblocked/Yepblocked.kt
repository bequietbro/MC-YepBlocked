package dev.beqi.yepblocked

import dev.beqi.yepblocked.command.AdminCommands
import dev.beqi.yepblocked.config.ConfigManager
import dev.beqi.yepblocked.listener.SpawnListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLPaths
import java.util.function.Consumer

@Mod(Yepblocked.ID)
object Yepblocked {
    const val ID = "yepblocked"

    init {
        val configPath = FMLPaths.CONFIGDIR.get().resolve("YepBlocked.json")
        ConfigManager.init(configPath)
        MinecraftForge.EVENT_BUS.register(SpawnListener)
        MinecraftForge.EVENT_BUS.addListener(Consumer<ServerAboutToStartEvent> { ConfigManager.ensureExists() })
        MinecraftForge.EVENT_BUS.register(AdminCommands)
    }
}
