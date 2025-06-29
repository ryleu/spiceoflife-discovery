package me.ryleu.soldisco.mixin;

import me.ryleu.soldisco.SOLDiscoClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {
    protected AbstractContainerScreenMixin(Component titleIn) {
        super(titleIn);
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    public void renderSlot(GuiGraphics graphics, Slot slot, CallbackInfo info) {
        // get the stack from the slot
        ItemStack itemStack = slot.getItem();
        SOLDiscoClient.render(graphics.pose(), itemStack, slot.x, slot.y);
    }
}
