package dev.beqi.yepblocked.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.beqi.yepblocked.config.ConfigManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraft.core.registries.BuiltInRegistries

object AdminCommands {
    private const val OP_LEVEL = 2

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        val dispatcher = event.dispatcher
        val command = Commands.literal("yepblocked")
            .requires { source -> source.hasPermission(OP_LEVEL) }
            .then(Commands.literal("reload").executes(::reload))
            .then(Commands.literal("add")
                .then(Commands.argument("entity", ResourceLocationArgument.id())
                    .suggests { _, builder ->
                        val remaining = builder.remaining.lowercase()
                        val registry = BuiltInRegistries.ENTITY_TYPE
                        for (type in registry) {
                            val id = registry.getKey(type).toString()
                            if (id.startsWith(remaining) || id.substringAfter(":").startsWith(remaining)) builder.suggest(id)
                        }
                        builder.buildFuture()
                    }
                    .executes(::add)
                    .then(Commands.argument("flags", StringArgumentType.greedyString())
                        .suggests { _, builder ->
                            val remaining = builder.remaining.lowercase()
                            val lastWord = remaining.trim().split(CommandLogic.WHITESPACE).lastOrNull() ?: ""
                            for (flag in CommandLogic.SUGGESTED_FLAGS) {
                                if (flag.startsWith(lastWord)) builder.suggest(flag)
                            }
                            builder.buildFuture()
                        }
                        .executes(::addWithFlags))))
            .then(Commands.literal("remove")
                .then(Commands.argument("entity", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        val remaining = builder.remaining.lowercase()
                        ConfigManager.getConfig().entityOverrides.keys.forEach { id ->
                            if (id.startsWith(remaining) || id.substringAfter(":").startsWith(remaining)) builder.suggest(id)
                        }
                        builder.buildFuture()
                    }
                    .executes(::remove)))
            .then(Commands.literal("global")
                .executes(::globalShow)
                .then(Commands.argument("flags", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        val remaining = builder.remaining.lowercase()
                        val lastWord = remaining.trim().split(CommandLogic.WHITESPACE).lastOrNull() ?: ""
                        for (flag in CommandLogic.SUGGESTED_FLAGS) {
                            if (flag.startsWith(lastWord)) builder.suggest(flag)
                        }
                        builder.buildFuture()
                    }
                    .executes(::globalSet)))
        dispatcher.register(command)
        dispatcher.register(Commands.literal("yb").redirect(command.build()))
    }

    private fun reload(ctx: CommandContext<CommandSourceStack>): Int {
        ConfigManager.reload()
        ctx.source.sendSuccess({ Component.literal(CommandLogic.buildReloadMessage()) }, false)
        return 1
    }

    private fun add(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = ResourceLocationArgument.getId(ctx, "entity")
        val validationError = validateEntity(entity)
        if (validationError != null) {
            ctx.source.sendFailure(Component.literal(validationError))
            return 0
        }
        val entityStr = entity.toString()
        val override = CommandLogic.parseAddFlags("").config!!
        val newConfig = CommandLogic.addOverride(ConfigManager.getConfig(), entityStr, override)
        ConfigManager.updateAndSave(newConfig)
        ctx.source.sendSuccess({ Component.literal(CommandLogic.buildAddResult(entityStr)) }, false)
        return 1
    }

    private fun addWithFlags(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = ResourceLocationArgument.getId(ctx, "entity")
        val validationError = validateEntity(entity)
        if (validationError != null) {
            ctx.source.sendFailure(Component.literal(validationError))
            return 0
        }
        val flags = StringArgumentType.getString(ctx, "flags")
        val result = CommandLogic.parseAddFlags(flags)
        if (result.isError) {
            result.errors.forEach { ctx.source.sendFailure(Component.literal(it)) }
            return 0
        }
        val entityStr = entity.toString()
        val newConfig = CommandLogic.addOverride(ConfigManager.getConfig(), entityStr, result.config!!)
        ConfigManager.updateAndSave(newConfig)
        ctx.source.sendSuccess({ Component.literal(CommandLogic.buildAddResult(entityStr)) }, false)
        return 1
    }

    private fun remove(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = StringArgumentType.getString(ctx, "entity").trim()
        val newConfig = CommandLogic.removeOverride(ConfigManager.getConfig(), entity)
        if (newConfig != null) {
            ConfigManager.updateAndSave(newConfig)
        }
        ctx.source.sendSuccess(
            { Component.literal(CommandLogic.buildRemoveResult(entity, newConfig != null)) },
            false
        )
        return 1
    }

    private fun globalShow(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.source.sendSuccess({ Component.literal(CommandLogic.buildGlobalStatus()) }, false)
        return 1
    }

    private fun globalSet(ctx: CommandContext<CommandSourceStack>): Int {
        val flags = StringArgumentType.getString(ctx, "flags")
        val result = CommandLogic.parseAddFlags(flags)
        if (result.isError) {
            result.errors.forEach { ctx.source.sendFailure(Component.literal(it)) }
            return 0
        }
        val newConfig = CommandLogic.applyGlobalFlags(ConfigManager.getConfig(), result.config!!)
        ConfigManager.updateAndSave(newConfig)
        ctx.source.sendSuccess({ Component.literal(CommandLogic.buildGlobalResult()) }, false)
        return 1
    }

    private fun validateEntity(entity: ResourceLocation): String? {
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entity)) return "§cUnknown entity: '$entity'"
        return null
    }
}
