package pro.mikey.fabric.xray.mixins;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.XRay;

// Thanks to architectury
@Mixin(BlockItem.class)
public abstract class MixinBlockItem {
    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;"))
    private void place(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide) {
            XRay.blockPlaced(context);
        }
    }
}
