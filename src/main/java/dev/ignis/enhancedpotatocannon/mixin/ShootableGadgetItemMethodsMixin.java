package dev.ignis.enhancedpotatocannon.mixin;

import com.simibubi.create.content.equipment.zapper.ShootableGadgetItemMethods;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(ShootableGadgetItemMethods.class)
public class ShootableGadgetItemMethodsMixin {
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


    @Inject(
            method = "Lcom/simibubi/create/content/equipment/zapper/ShootableGadgetItemMethods;applyCooldown(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Ljava/util/function/Predicate;I)V",
            at = @At(value = "HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false,
            cancellable = true
    )
    private static void beforeApplyCooldown(Player player, ItemStack item, InteractionHand hand, Predicate<ItemStack> predicate, int cooldown, CallbackInfo ci){
        int originalCooldown = cooldown;
        if(player!=null&&player.hasEffect(MobEffects.DIG_SPEED)){
            int multiplier = 1+Objects.requireNonNull(player.getEffect(MobEffects.DIG_SPEED)).getAmplifier();
            cooldown = Math.max(1,(int)Math.ceil((float)cooldown/(0.5+multiplier)));
        }
        if(player!=null&&player.hasEffect(MobEffects.DIG_SLOWDOWN)){
            int multiplier = 1+Objects.requireNonNull(player.getEffect(MobEffects.DIG_SLOWDOWN)).getAmplifier();
            cooldown = Math.max(1,(int)Math.ceil((float)cooldown*(0.5+multiplier)));
        }
        if (cooldown > 0) {
            boolean gunInOtherHand = predicate.test(player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
            player.getCooldowns().addCooldown(item.getItem(), gunInOtherHand ? cooldown * 2 / 3 : cooldown);
            //EnhancedPotatoCannon.LOGGER.debug("Cooldown: "+cooldown+" -> "+originalCooldown);
        }
        ci.cancel();
    };
}
