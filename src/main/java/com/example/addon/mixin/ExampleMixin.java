package com.example.addon.mixin;

import com.example.addon.AddonTemplate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example Mixin class.
 * For more resources, visit:
 * - <a href="https://fabricmc.net/wiki/tutorial:mixin_introduction">The FabricMC wiki</a>
 * - <a href="https://github.com/SpongePowered/Mixin/wiki">The Mixin wiki</a>
 * - <a href="https://github.com/LlamaLad7/MixinExtras/wiki">The MixinExtras wiki</a>
 * - <a href="https://jenkins.liteloader.com/view/Other/job/Mixin/javadoc/allclasses-noframe.html">The Mixin javadoc</a>
 * - <a href="https://github.com/2xsaiko/mixin-cheatsheet">The Mixin cheatsheet</a>
 */
@Mixin(MinecraftClient.class)
public abstract class ExampleMixin {
    /**
     * Example Mixin injection targeting the {@code <init>} method (the constructor) at {@code TAIL} (end of method).
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onGameLoaded(RunArgs args, CallbackInfo ci) {
        AddonTemplate.LOG.info("Hello from ExampleMixin !!");
    }
}
