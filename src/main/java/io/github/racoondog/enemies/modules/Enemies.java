package io.github.racoondog.enemies.modules;

import com.mojang.brigadier.suggestion.Suggestion;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Enemies extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public final Setting<List<String>> enemies = sgGeneral.add(new StringListSetting.Builder()
        .name("enemies")
        .description("The players to mark as enemies.")
        .defaultValue("MineGame159")
        .build()
    );

    public final Setting<Boolean> highlight = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-enemies")
        .description("Whether to highlight enemies in BetterTab and Nametags.")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> highlightColor = sgGeneral.add(new ColorSetting.Builder()
        .name("highlight-color")
        .description("The color to highlight the players with.")
        .visible(highlight::get)
        .defaultValue(Color.RED)
        .build()
    );

    private final Setting<Boolean> warnJoin = sgGeneral.add(new BoolSetting.Builder()
        .name("warn-on-join")
        .description("Display a warning in chat whenever an enemy joins.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> warnLeave = sgGeneral.add(new BoolSetting.Builder()
        .name("warn-on-leave")
        .description("Display a warning in chat whenever an enemy leaves.")
        .defaultValue(true)
        .onChanged(b -> clearLeaveCheck())
        .build()
    );

    private final Setting<Boolean> checkVanish = sgGeneral.add(new BoolSetting.Builder()
        .name("check-vanish")
        .description("Periodically check for vanished users and add them to the tab list.")
        .defaultValue(true)
        .onChanged(b -> clearVanishCheck())
        .build()
    );

    private final Setting<String> checkVanishCommand = sgGeneral.add(new StringSetting.Builder()
        .name("check-vanish-command")
        .description("The command to use to collect username completions for vanish checking.")
        .defaultValue("msg")
        .build()
    );

    private final Set<String> vanishedPlayers = new ObjectOpenHashSet<>(); // currently vanished players
    private final Set<String> leftPlayers = new ObjectOpenHashSet<>(); // players that left, pending vanish check
    private int timer = 0;
    private volatile boolean isChecking = false;
    private volatile int packetId = -1;

    public Enemies() {
        super(Categories.Render, "enemies", "A handy module to highlight specific players as enemies.");
    }

    private void clearVanishCheck() {
        synchronized (vanishedPlayers) {
            vanishedPlayers.clear();
        }
        isChecking = false;
        packetId = -1;
        clearLeaveCheck();
    }

    private void clearLeaveCheck() {
        synchronized (leftPlayers) {
            leftPlayers.clear();
        }
    }

    @Override
    public void onDeactivate() {
        clearVanishCheck();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (++timer >= 100) {
            timer = 0;

            if (!isChecking && checkVanish.get()) {
                isChecking = true;
                packetId = ThreadLocalRandom.current().nextInt(200);
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(packetId, checkVanishCommand.get() + " "));
            }
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet) {
            if (warnJoin.get()) {
                MinecraftClient.getInstance().execute(() -> {
                    for (PlayerListS2CPacket.Entry addEntry : packet.getPlayerAdditionEntries()) {
                        if (addEntry.profile() == null) continue;

                        String username = addEntry.profile().getName();
                        if (!enemies.get().contains(username)) continue;

                        if (checkVanish.get()) {
                            synchronized (vanishedPlayers) {
                                if (vanishedPlayers.contains(username)) continue;
                            }
                        }

                        info("(highlight)%s(default) has joined the game.", username);
                    }
                });
            }

            // remove rejoins from the pending leave queue
            if (warnLeave.get() && checkVanish.get()) {
                synchronized (leftPlayers) {
                    for (PlayerListS2CPacket.Entry addEntry : packet.getPlayerAdditionEntries()) {
                        if (addEntry.profile() == null) continue;

                        leftPlayers.remove(addEntry.profile().getName());
                    }
                }
            }
        }

        if (warnLeave.get() && event.packet instanceof PlayerRemoveS2CPacket packet) {
            // if the vanish checker is on, wait until next check to not announce a vanish as a leave
            if (checkVanish.get()) {
                MinecraftClient.getInstance().execute(() -> {
                    synchronized (leftPlayers) {
                        for (UUID uuid : packet.profileIds()) {
                            @Nullable PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(uuid);
                            if (entry != null) {
                                leftPlayers.add(entry.getProfile().getName());
                            }
                        }
                    }
                });
            } else {
                MinecraftClient.getInstance().execute(() -> {
                    for (UUID leftEntry : packet.profileIds()) {
                        @Nullable PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(leftEntry);
                        if (entry != null && enemies.get().contains(entry.getProfile().getName())) {
                            info("(highlight)%s(default) has left the game.", entry.getProfile().getName());
                        }
                    }
                });
            }
        }

        if (isChecking && event.packet instanceof CommandSuggestionsS2CPacket packet && packet.id() == packetId) {
            isChecking = false;
            packetId = -1;
            MinecraftClient.getInstance().execute(() -> {
                Set<String> newVanish = packet.getSuggestions().getList().stream()
                    .map(Suggestion::getText)
                    .filter(value -> mc.getNetworkHandler().getPlayerListEntry(value) == null)
                    .filter(value -> !(value.startsWith("@") && value.length() == 2)) // hide selectors
                    .collect(Collectors.toUnmodifiableSet());

                synchronized (vanishedPlayers) {
                    for (String name : vanishedPlayers) {
                        if (!newVanish.contains(name)) {
                            info("(highlight)%s(default) is no longer vanished.", name);
                        }
                    }
                    for (String name : newVanish) {
                        if (!vanishedPlayers.contains(name)) {
                            info("(highlight)%s(default) is now vanished.", name);
                        }
                    }

                    vanishedPlayers.clear();
                    vanishedPlayers.addAll(newVanish);

                    if (warnLeave.get()) {
                        synchronized (leftPlayers) {
                            for (String leftPlayer : leftPlayers) {
                                if (enemies.get().contains(leftPlayer) && !vanishedPlayers.contains(leftPlayer)) {
                                    info("(highlight)%s(default) has left the game.", leftPlayer);
                                }
                            }

                            leftPlayers.clear();
                        }
                    }
                }
            });
        }
    }
}
