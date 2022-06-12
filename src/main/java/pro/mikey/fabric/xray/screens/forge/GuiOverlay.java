package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.storage.SettingsStore;

public class GuiOverlay {
    private static final Identifier circle = new Identifier(XRay.PREFIX_GUI + "circle.png");

    public static void RenderGameOverlayEvent(MatrixStack matrixStack, float delta) {
        // Draw Indicator
        if (!SettingsStore.getInstance().get().isActive() || !SettingsStore.getInstance().get().showOverlay()) {
            return;
        }

        RenderSystem.setShaderColor(0, 1f, 0, 1f);
        RenderSystem.setShaderTexture(0, circle);
        Screen.drawTexture(matrixStack, 5, 5, 0f, 0f, 5, 5, 5, 5);

        MinecraftClient.getInstance()
            .textRenderer
            .drawWithShadow(matrixStack, I18n.translate("xray.overlay"), 15, 4, 0xffffffff);
    }
}
