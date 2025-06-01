package dev.ignis.enhancedpotatocannon.mixin;

import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetItemMethods;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShootableGadgetItemMethods.class)
public class PotatoCannonMixin {
    @Inject(
            method = "Lcom/simibubi/create/content/equipment/zapper/ShootableGadgetItemMethods;getGunBarrelVec(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false

    )
    private static void shoot(Player player, boolean mainHand, Vec3 rightHandForward, CallbackInfoReturnable<Vec3> cir){
        if(player.isCrouching()){
            Vec3 aimVec = player.position().add(0.0, (double)player.getEyeHeight(), 0.0);
            aimVec = aimVec.add(player.getLookAngle());
            cir.setReturnValue(aimVec);
        }
    }

}
