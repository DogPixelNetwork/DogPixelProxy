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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.VelocityServer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public final class ShutdownCommand {

    private static ScheduledTask countdownTask;
    private static BossBar globalBossBar;
    private static AtomicInteger remainingSeconds;

    private ShutdownCommand() {
    }

    public static BrigadierCommand command(final VelocityServer server) {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("shutdown")
                        .requires(source -> source.hasPermission("velocity.command.shutdown"))
                        .executes(context -> {
                            server.shutdown(true);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("reason",
                                        StringArgumentType.greedyString())
                                .executes(context -> {
                                    String reason = context.getArgument("reason", String.class);
                                    server.shutdown(true, parseReason(reason));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("minutes",
                                                IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            String reason = context.getArgument("reason", String.class);
                                            int minutes = context.getArgument("minutes", Integer.class);
                                            startCountdown(server, parseReason(reason), minutes);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }

    private static Component parseReason(String reason) {
        return Component.text(reason, NamedTextColor.RED);
    }

    private static void startCountdown(VelocityServer server, Component reason, int minutes) {
        cancelCountdown();

        remainingSeconds = new AtomicInteger(minutes * 60);
        globalBossBar = BossBar.bossBar(
                Component.text("服务器维护倒计时", NamedTextColor.YELLOW),
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );

        broadcastShutdownNotice(server, reason, minutes * 60);

        countdownTask = server.getScheduler()
                .buildTask(server, () -> {
                    int seconds = remainingSeconds.decrementAndGet();
                    if (seconds <= 0) {
                        server.shutdown(true, reason);
                        return;
                    }

                    float progress = (float) seconds / (minutes * 60);
                    globalBossBar.progress(progress);
                    globalBossBar.name(Component.text(String.format(
                            "服务器将在 %d分%d秒后维护",
                            seconds / 60,
                            seconds % 60
                    ), NamedTextColor.YELLOW));

                    if (seconds <= 10) {
                        globalBossBar.color(BossBar.Color.RED);
                        server.getAllPlayers().forEach(player -> {
                            player.showTitle(Title.title(
                                    Component.text("维护通知", NamedTextColor.YELLOW, TextDecoration.BOLD),
                                    Component.text(String.format(
                                            "服务器将在 %d 秒后维护！", seconds
                                    ), NamedTextColor.RED)
                            ));
                            player.sendMessage(Component.text(
                                    String.format("§c§l%d 秒重启", seconds),
                                    NamedTextColor.RED, TextDecoration.BOLD
                            ));
                        });
                    } else {
                        if (seconds % 600 == 0 || (seconds <= 600 && seconds % 60 == 0)) {
                            broadcastShutdownNotice(server, reason, seconds);
                        }
                    }
                })
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
    }

    private static void broadcastShutdownNotice(ProxyServer server, Component reason, int seconds) {
        int minutes = seconds / 60;
        Component message = Component.text()
                .append(Component.text("[维护通知] ", NamedTextColor.GOLD))
                .append(Component.text("服务器将在 ", NamedTextColor.YELLOW))
                .append(Component.text(minutes + " 分钟", NamedTextColor.RED))
                .append(Component.text("后开始维护。原因: ", NamedTextColor.YELLOW))
                .append(reason)
                .build();

        server.getAllPlayers().forEach(player -> {
            player.sendMessage(message);
            player.showTitle(Title.title(
                    Component.text("维护通知", NamedTextColor.YELLOW, TextDecoration.BOLD),
                    Component.text(String.format(
                            "服务器将在 %d 分钟后维护！", minutes
                    ), NamedTextColor.RED)
            ));

            if (globalBossBar != null) {
                player.showBossBar(globalBossBar);
            }
        });

        server.getConsoleCommandSource().sendMessage(message);
    }

    private static void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (globalBossBar != null) {
            globalBossBar = null;
        }
    }
}