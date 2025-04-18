package io.github.racoondog.enemies.mixin;

import io.github.racoondog.enemies.modules.Enemies;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BetterTab.class, remap = false)
public class BetterTabMixin {
    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void addEnemyColor(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        Enemies enemies = Modules.get().get(Enemies.class);

        if (enemies.isActive() && enemies.enemies.get().contains(entry.getProfile().getName())) {
            // inherit name & style if possible
            Text name = entry.getDisplayName();
            String nameString = name == null ? entry.getProfile().getName() : name.getString();
            Style style = name == null ? Style.EMPTY : name.getStyle();

            for (Formatting format : Formatting.values()) {
                if (format.isColor()) nameString = nameString.replace(format.toString(), "");
            }

            cir.setReturnValue(Text.literal(nameString).setStyle(style.withColor(TextColor.fromRgb(enemies.highlightColor.get().getPacked()))));
        }
    }
}
