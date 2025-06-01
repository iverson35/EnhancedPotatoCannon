package dev.ignis.enhancedpotatocannon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItemRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PotatoCannonItemRenderer.class)
public class PotatoCannonRenderMixin {
    @Inject(
            method = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoCannonItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lcom/simibubi/create/foundation/item/render/CustomRenderedItemModel;Lcom/simibubi/create/foundation/item/render/PartialItemModelRenderer;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    protected void renderHead(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci){
        float xOffset = 0f;
        float yOffset = 0f;
        float zOffset = 0f;
        try{
            if (Minecraft.getInstance().player.isCrouching()) {
                switch (transformType) {
                    case GUI:
                        break;
                    case FIRST_PERSON_RIGHT_HAND:
                        xOffset = -0.2f;
                        zOffset = 0.1f;
                        break;
                    case FIRST_PERSON_LEFT_HAND:
                        xOffset = 0.2f;
                        zOffset = 0.1f;
                        break;
                }
            }
        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Dist is not client: "+e.getMessage());
        }
        ms.pushPose();
        ms.translate(xOffset, yOffset, zOffset);
    }

    @Inject(
            method = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoCannonItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lcom/simibubi/create/foundation/item/render/CustomRenderedItemModel;Lcom/simibubi/create/foundation/item/render/PartialItemModelRenderer;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "TAIL"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    protected void renderTail(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci){
        ms.popPose();
    }
}
