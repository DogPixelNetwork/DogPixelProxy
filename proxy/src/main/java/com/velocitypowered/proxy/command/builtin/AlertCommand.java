/*
 * Copyright (C) 2020-2023 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class AlertCommand {

    private final ProxyServer server;

    public AlertCommand(ProxyServer server) {
        this.server = server;
    }

    public void register() {
        LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
                .literalArgumentBuilder("alert")
                .requires(source -> source.getPermissionValue("velocity.command.alert") == Tristate.TRUE)
                .executes(context -> {
                    context.getSource().sendMessage(
                            Component.text("用法: /alert <消息>", NamedTextColor.RED)
                    );
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("message",
                                StringArgumentType.greedyString())
                        .executes(this::execute));

        server.getCommandManager().register(
                server.getCommandManager().metaBuilder(new BrigadierCommand(rootNode))
                        .plugin(VelocityVirtualPlugin.INSTANCE)
                        .build(),
                new BrigadierCommand(rootNode)
        );
    }

    private int execute(CommandContext<CommandSource> context) {
        String message = StringArgumentType.getString(context, "message");


        Component alertMessage = Component.text()
                .append(Component.text("全服通知 ", NamedTextColor.GOLD))
                .append(Component.text(">> ", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(message, NamedTextColor.AQUA))
                .build();


        for (Player player : server.getAllPlayers()) {
            player.sendMessage(alertMessage);
            player.showTitle(Title.title(
                    Component.text("全服通知", NamedTextColor.RED),
                    Component.text(message, NamedTextColor.AQUA),
                    Title.Times.of(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000))
            ));
        }


        server.getConsoleCommandSource().sendMessage(alertMessage);


        return Command.SINGLE_SUCCESS;
    }
}