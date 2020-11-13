package pro.mikey.fabric.xray.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class MainScreen extends Screen {
    public MainScreen() {
        super(LiteralText.EMPTY);

    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
