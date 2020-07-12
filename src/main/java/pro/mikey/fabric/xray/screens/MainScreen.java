package pro.mikey.fabric.xray.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MainScreen extends Screen {
    protected MainScreen(Text title) {
        super(title);

    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
