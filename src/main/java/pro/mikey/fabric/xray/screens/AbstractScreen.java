package pro.mikey.fabric.xray.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class AbstractScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier("textures/gui/recipe_book.png");

    AbstractScreen(Text title) {
        super(title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        //    RenderSystem.pushMatrix();
        //    RenderSystem.translatef(0.0F, 0.0F, 100.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - 147) / 2;
        int j = (this.height - 166) / 2;

        this.drawTexture(matrices, i, j, 1, 1, 147, 166);
        //    RenderSystem.popMatrix();
    }
}
