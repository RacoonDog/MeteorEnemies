package io.github.racoondog.enemies.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.racoondog.enemies.modules.Enemies;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

public class EnemyCommand extends Command {
    public EnemyCommand() {
        super("enemy", "Handy command to manage enemies.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(ctx -> {
            Enemies enemies = Modules.get().get(Enemies.class);

            if (enemies.enemies.get().isEmpty()) {
                info("You have (highlight)no players(default) marked as enemies.");
            } else {
                info("Enemies (%s):", enemies.enemies.get().size());
                for (String player : enemies.enemies.get()) {
                    info("- (highlight)%s", player);
                }
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(argument("player", StringArgumentType.word())
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "player");

                Enemies enemies = Modules.get().get(Enemies.class);

                if (name.equals(mc.getNetworkHandler().getProfile().getName())) {
                    error("Cannot mark yourself as an enemy.");
                } else if (enemies.enemies.get().contains(name)) {
                    error("%s was already marked as an enemy.", name);
                } else {
                    enemies.enemies.get().add(name);
                    info("(highlight)%s(default) is now marked as an enemy.", name);
                }

                return SINGLE_SUCCESS;
            })
            .suggests((ctx, suggestionsBuilder) -> {
                return CommandSource.suggestMatching(mc.getNetworkHandler().getPlayerList().stream()
                    .map(PlayerListEntry::getProfile)
                    .filter(profile -> !profile.getId().equals(mc.getNetworkHandler().getProfile().getId()))
                    .map(GameProfile::getName), suggestionsBuilder);
            })
        ));

        builder.then(literal("remove").then(argument("player", StringArgumentType.word())
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "player");

                Enemies enemies = Modules.get().get(Enemies.class);

                if (enemies.enemies.get().remove(name)) {
                    info("(highlight)%s(default) is no longer marked as an enemy.", name);
                } else {
                    error("%s was not marked as an enemy.", name);
                }

                return SINGLE_SUCCESS;
            })
            .suggests((ctx, suggestionsBuilder) -> {
                return CommandSource.suggestMatching(Modules.get().get(Enemies.class).enemies.get(), suggestionsBuilder);
            })
        ));
    }
}
