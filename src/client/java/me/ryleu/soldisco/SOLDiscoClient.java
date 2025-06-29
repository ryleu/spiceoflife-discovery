package me.ryleu.soldisco;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.ryleu.soldisco.component.IFoodHistory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static me.ryleu.soldisco.SOLDisco.MOD_ID;

public class SOLDiscoClient implements ClientModInitializer {
    public static final ResourceLocation NEW_FOOD_ICON = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/star.png");
    public static final KeyMapping KEYBINDING_MAIN_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyMapping(MOD_ID + ".key.food_history", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "key.categories." + MOD_ID));

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KEYBINDING_MAIN_SCREEN.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new FoodHistoryScreen());
                }
            }
        });
    }

    public static void render(PoseStack poseStack, ItemStack item, int x, int y) {
        LocalPlayer player = Minecraft.getInstance().player;

        // check if we even can render
        if (item.isEmpty() || player == null)
            return;

        IFoodHistory foodHistory = ((IPlayer) player).soldisco$getFoodHistory();

        // check if we should render
        if (!item.getComponents().has(DataComponents.FOOD) || foodHistory.contains(item.getItem()))
            return;

        // actually render
        RenderSystem.disableDepthTest(); // will render under the item without this
        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, NEW_FOOD_ICON);
        drawIcon(poseStack, x + 8, y);
        poseStack.popPose();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void drawIcon(PoseStack poseStack, int x, int y) {
        // Hard-coded sprite bounds
        final int width = 8;
        final int height = 8;
        final int texX = 0;
        final int texY = 0;
        final int texWidth = 8;
        final int texHeight = 8;
        final int fullWidth = 16;
        final int fullHeight = 16;
        final int z = 0;

        // screen coordinates
        int x0 = x;
        int x1 = x + width;
        int y0 = y;
        int y1 = y + height;

        // normalized UV coords
        float u0 = texX / (float) fullWidth;
        float u1 = (texX + texWidth) / (float) fullWidth;
        float v0 = texY / (float) fullHeight;
        float v1 = (texY + texHeight) / (float) fullHeight;

        // draw
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS,
                        DefaultVertexFormat.POSITION_TEX);

        buf.addVertex(matrix, x0, y0, z).setUv(u0, v0);
        buf.addVertex(matrix, x0, y1, z).setUv(u0, v1);
        buf.addVertex(matrix, x1, y1, z).setUv(u1, v1);
        buf.addVertex(matrix, x1, y0, z).setUv(u1, v0);

        BufferUploader.drawWithShader(Objects.requireNonNull(buf.build()));
    }
}
