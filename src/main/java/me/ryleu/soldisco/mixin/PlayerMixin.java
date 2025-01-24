package me.ryleu.soldisco.mixin;

import com.mojang.authlib.GameProfile;
import me.ryleu.soldisco.IPlayer;
import me.ryleu.soldisco.SOLDisco;
import me.ryleu.soldisco.component.IFoodHistory;
import net.minecraft.core.BlockPos;
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

import static me.ryleu.soldisco.component.SOLDiscoComponents.FOOD_HISTORY_COMPONENT_KEY;

@Mixin(Player.class)
public class PlayerMixin implements IPlayer {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstruct(Level level, BlockPos pos, float yRot, GameProfile gameProfile, CallbackInfo ci) {
        soldisco$updateMaxHealth();
    }

    @Inject(at = @At("HEAD"), method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;")
    private void onEat(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object)this instanceof Player player) {
            Item foodItem = itemStack.getItem();

            if (soldisco$getFoodHistory().add(foodItem)) {
                SOLDisco.LOGGER.debug("{} ate a {} (new food!)", player.getStringUUID(), foodItem);
            }
        }
    }

    @Unique
    @Override
    public void soldisco$updateMaxHealth() {
        Player target = (Player)(Object) this;
        AttributeInstance maxHealthAttribute = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            SOLDisco.LOGGER.error("max health attribute for {} was null!", target.getStringUUID());
        } else {
            maxHealthAttribute.setBaseValue(soldisco$getFoodHistory().getMaxHealth());
        }
    }

    @Unique
    public IFoodHistory soldisco$getFoodHistory() {
        return FOOD_HISTORY_COMPONENT_KEY.get(this);
    }
}
