package me.ryleu.soldisco;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static me.ryleu.soldisco.SOLDisco.MOD_ID;

public class SOLDiscoClient implements ClientModInitializer {
    public static final KeyMapping KEYBINDING_MAIN_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyMapping(MOD_ID + ".key.food_history", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "key.categories." + MOD_ID));

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register( client -> {
            while (KEYBINDING_MAIN_SCREEN.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new FoodHistoryScreen());
                }
            }
        });
    }
}
