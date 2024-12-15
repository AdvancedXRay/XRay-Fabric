package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.fabric.xray.Utils;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.storage.SettingsStore;

public class GuiOverlay {
    private static final ResourceLocation circle = Utils.rlFull(XRay.PREFIX_GUI + "circle.png");

    public static void RenderGameOverlayEvent(GuiGraphics guiGraphics, DeltaTracker counter) {
        // Draw Indicator
        if (!SettingsStore.getInstance().get().isActive() || !SettingsStore.getInstance().get().showOverlay()) {
            return;
        }

        boolean renderDebug = GlDebug.isDebugEnabled();

        int x = 5, y = 5;
        if (renderDebug) {
            x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 10;
            y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 10;
        }

        RenderSystem.setShaderColor(0, 1f, 0, 1f);
//        RenderSystem.setShaderTexture(0, circle);
        guiGraphics.blit(RenderType::guiTexturedOverlay, circle, x, y, 0f, 0f, 5, 5, 5, 5);

        int width = Minecraft.getInstance().font.width(I18n.get("xray.overlay"));
        guiGraphics.drawString(Minecraft.getInstance().font, I18n.get("xray.overlay"), x + (!renderDebug ? 10 : -width - 5), y - (!renderDebug ? 1 : 2), 0xffffffff);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
