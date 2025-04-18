package io.github.racoondog.enemies.mixin;

import io.github.racoondog.enemies.modules.Enemies;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerUtils.class, remap = false)
public class PlayerUtilsMixin {
    @Shadow @Final private static Color color;

    @Inject(method = "getPlayerColor", at = @At("HEAD"), cancellable = true)
    private static void inject(PlayerEntity entity, Color defaultColor, CallbackInfoReturnable<Color> cir) {
        Enemies enemies = Modules.get().get(Enemies.class);

        if (enemies.isActive() && enemies.enemies.get().contains(entity.getGameProfile().getName())) {
            cir.setReturnValue(color.set(enemies.highlightColor.get()).a(defaultColor.a));
        }
    }
}
