package dev.ignis.enhancedpotatocannon.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Unique;

import static net.minecraft.world.effect.MobEffectCategory.BENEFICIAL;

public class CommonUtil {
    public static boolean arePlayersInSameTeam(Player player1, Player player2) {
        Scoreboard scoreboard = player1.getCommandSenderWorld().getScoreboard();
        PlayerTeam team1 = scoreboard.getPlayersTeam(player1.getScoreboardName());
        PlayerTeam team2 = scoreboard.getPlayersTeam(player2.getScoreboardName());

        return team1 != null && team1 == team2;
    }

    public static boolean isTamedByOwner(Entity owner, Entity target) {
        if (target instanceof TamableAnimal tamable) {
            return tamable.isTame() && tamable.getOwner() == owner;
        }
        return false;
    }

    public static boolean isBeneficialEffect(MobEffect effect) {
        MobEffectCategory category = effect.getCategory();

        return BENEFICIAL.equals(category);
    }

    public static boolean isFriendly(Entity ownerEntity,Entity target){
        if(!(ownerEntity instanceof Player owner)) return false;
        if(target == null) return false;
        if(owner.level().isClientSide || target.level().isClientSide) return false;

        if (owner == target) {
            return true;
        }

        if (owner instanceof ServerPlayer ownerPlayer && target instanceof ServerPlayer targetPlayer) {
            if (arePlayersInSameTeam(ownerPlayer, targetPlayer)) {
                return true;
            }
        }

        // 3. 检查驯服关系（如狼、猫等）
        if (isTamedByOwner(owner, target)) {
            return true;
        }

        return false;
    }
}
