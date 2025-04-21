package io.github.racoondog.enemies.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.racoondog.enemies.modules.Enemies;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BetterTab.class, remap = false)
public class BetterTabMixin {
    @Inject(method = "getPlayerName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getDisplayName()Lnet/minecraft/text/Text;", remap = true, ordinal = 0))
    private void addEnemyColor(PlayerListEntry entry, CallbackInfoReturnable<Text> cir, @Local LocalRef<Color> colorRef) {
        Enemies enemies = Modules.get().get(Enemies.class);

        if (enemies.isActive() && enemies.highlight.get() && enemies.enemies.get().contains(entry.getProfile().getName())) {
            colorRef.set(enemies.highlightColor.get());
        }
    }
}
