package dev.ignis.enhancedpotatocannon.mixin;

import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpyglassItem.class)
public class SpyglassItemMixin extends Item {
    public SpyglassItemMixin(Properties p_41383_) {
        super(p_41383_);
    }

    @Inject(
            method = "Lnet/minecraft/world/item/SpyglassItem;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At(value = "HEAD"),
            cancellable = true

    )
    private void onUse(Level p_151218_, Player p_151219_, InteractionHand p_151220_, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if(p_151219_.getOffhandItem().getItem() instanceof ProjectileWeaponItem || p_151219_.getMainHandItem().getItem() instanceof ProjectileWeaponItem){
            cir.setReturnValue(InteractionResultHolder.pass(p_151219_.getItemInHand(p_151220_)));
        }
    }
}
