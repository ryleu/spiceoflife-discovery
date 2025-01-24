package me.ryleu.soldisco.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
//    @Unique
//    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("RETURN"))
//    public void onPlayerCopied(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
//        ((IPlayer) this).spiceoflife_discovery$setFoodHistory(((IPlayer) that).spiceoflife_discovery$getFoodHistory());
//    }
}
