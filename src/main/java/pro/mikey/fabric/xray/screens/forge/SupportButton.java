package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

class SupportButton extends Button {
    private final List<Component> support = new ArrayList<>();

    SupportButton(
            int widthIn,
            int heightIn,
            int width,
            int height,
            Component text,
            MutableComponent support,
            OnPress onPress,
            CreateNarration createNarration
            ) {
        super(widthIn, heightIn, width, height, text, onPress, createNarration);

        for (String line : support.getString().split("\n")) {
            this.support.add(Component.translatable(line));
        }
    }

    List<Component> getSupport() {
        return this.support;
    }
}
