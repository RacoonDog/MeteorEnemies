package io.github.racoondog.enemies;

import io.github.racoondog.enemies.commands.EnemyCommand;
import io.github.racoondog.enemies.modules.Enemies;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class EnemiesAddon extends MeteorAddon {
    @Override
    public void onInitialize() {
        Modules.get().add(new Enemies());
        Commands.add(new EnemyCommand());
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("RacoonDog", "MeteorEnemies");
    }

    @Override
    public String getPackage() {
        return "io.github.racoondog.enemies";
    }
}
