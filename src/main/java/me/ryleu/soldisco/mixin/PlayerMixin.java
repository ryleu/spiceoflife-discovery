package me.ryleu.soldisco.mixin;

import com.mojang.authlib.GameProfile;
import me.ryleu.soldisco.FoodHistory;
import me.ryleu.soldisco.IPlayer;
import me.ryleu.soldisco.SOLDisco;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin implements IPlayer {
//    private static final EntityDataAccessor<CompoundTag> DATA_FOOD_HISTORY = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
//
//    @Inject(method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", at = @At("RETURN"))
//    private void registerSyncedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
//
//    }

    @Unique
    protected FoodHistory foodHistory = new FoodHistory((Player)(Object) this);

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstruct(Level level, BlockPos pos, float yRot, GameProfile gameProfile, CallbackInfo ci) {
        spiceoflife_discovery$updateMaxHealth();
    }

    @Inject(at = @At("HEAD"), method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;")
    private void onEat(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object)this instanceof Player player) {
            Item foodItem = itemStack.getItem();

            if (foodHistory.add(foodItem)) {
                SOLDisco.LOGGER.debug("{} ate a {} (new food!)", player.getStringUUID(), foodItem);
            }

            spiceoflife_discovery$updateMaxHealth();
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onDeserialize(CompoundTag compoundTag, CallbackInfo ci) {
        foodHistory = FoodHistory.read((Player)(Object) this, compoundTag);
        spiceoflife_discovery$updateMaxHealth();
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onSerialize(CompoundTag compoundTag, CallbackInfo ci) {
        foodHistory.write(compoundTag);
    }

    @Override
    public FoodHistory spiceoflife_discovery$getFoodHistory() {
        return foodHistory;
    }

    @Unique
    @Override
    public void spiceoflife_discovery$updateMaxHealth() {
        Player target = (Player)(Object) this;
        AttributeInstance maxHealthAttribute = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            SOLDisco.LOGGER.error("max health attribute for {} was null!", target.getStringUUID());
        } else {
            maxHealthAttribute.setBaseValue(foodHistory.getMaxHealth());
        }
    }

    @Override
    public void spiceoflife_discovery$setFoodHistory(FoodHistory newFoodHistory) {
        foodHistory = newFoodHistory;
    }
}
