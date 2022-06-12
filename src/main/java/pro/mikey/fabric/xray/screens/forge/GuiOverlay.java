package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.storage.SettingsStore;

public class GuiOverlay {
    private static final ResourceLocation circle = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    public static void RenderGameOverlayEvent(PoseStack matrixStack, float delta) {
        // Draw Indicator
        if (!SettingsStore.getInstance().get().isActive() || !SettingsStore.getInstance().get().showOverlay()) {
            return;
        }

        RenderSystem.setShaderColor(0, 1f, 0, 1f);
        RenderSystem.setShaderTexture(0, circle);
        Screen.blit(matrixStack, 5, 5, 0f, 0f, 5, 5, 5, 5);

        Minecraft.getInstance()
            .font
            .drawShadow(matrixStack, I18n.get("xray.overlay"), 15, 4, 0xffffffff);
    }
}
