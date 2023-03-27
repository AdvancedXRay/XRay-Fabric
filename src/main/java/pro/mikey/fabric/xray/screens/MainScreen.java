package pro.mikey.fabric.xray.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.BlockStore;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends AbstractScreen {
    private final List<BlockGroup> blocks = new ArrayList<>();

    public MainScreen() {
        super(Component.empty());

        List<BlockGroup> read = BlockStore.getInstance().read();
        if (read != null) {
            this.blocks.addAll(read);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int y = 50;
        for (BlockGroup group : this.blocks) {
            drawString(matrices, this.font, "Hello", this.width / 2 - 40, y, 0xFFFFFF);

            y += this.font.lineHeight + 10;
            for (BlockEntry entry : group.entries()) {
                drawString(matrices, this.font, entry.getName(), this.width / 2 - 40, y, 0xFFFFFF);

                matrices.pushPose();
                matrices.translate(this.width / 2f - 60, y - 5, 0);
                matrices.scale(.8f, .8f, .8f);
                this.itemRenderer.renderAndDecorateFakeItem(matrices, new ItemStack(Items.GOLD_BLOCK), 0, 0);
                matrices.popPose();

                y += this.font.lineHeight + 10;
            }

            y += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.blocks.clear();
        super.onClose();
    }
}
