package dev.ignis.enhancedpotatocannon;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpyglassListener {
    private boolean isUsingTelescopeClient = false;
    private boolean isUsingCustomTelescope = false;

    public SpyglassListener(){
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        boolean lastTickStatus = getIsUsing(event.player);
        if (event.phase == TickEvent.Phase.START) {
            boolean isCrouching = event.player.isCrouching();
            boolean hasTelescope = event.player.getMainHandItem().getItem() == Items.SPYGLASS || event.player.getOffhandItem().getItem() == Items.SPYGLASS;
            setIsUsing(event.player,isCrouching && hasTelescope);
            //if(event.player.level().isClientSide()) EnhancedPotatoCannon.LOGGER.debug("Client status iC:"+isCrouching+"- hT:"+hasTelescope+"- lT:"+lastTickStatus+"- tT:"+getIsUsing(event.player));
            if(hasTelescope){
                if(!lastTickStatus && getIsUsing(event.player)){
                    startUsingTelescope(event.player);
                }else if(lastTickStatus && !getIsUsing(event.player)){
                    stopUsingTelescope(event.player);
                }
            }else{
                isUsingCustomTelescope = false;
            }
        }
    }

    public boolean getIsUsing(Player player){
        return player.level().isClientSide()?isUsingTelescopeClient:isUsingTelescopeServer;
    }

    @SubscribeEvent
    public void onFOVUpdate(ViewportEvent.ComputeFov event) {
        if (isUsingCustomTelescope) {
            // 直接修改 FOV，不依赖原版望远镜机制
            event.setFOV(event.getFOV() * 0.1F);
        }
    }

    public void setIsUsing(Player player,boolean value){
        if(player.level().isClientSide){
            isUsingTelescopeClient = value;
        }else{
            isUsingTelescopeServer = value;
        }
    }

    public void startUsingTelescope(Player player) {
        EnhancedPotatoCannon.LOGGER.debug("Start using at "+(player.level().isClientSide()?"client":"server"));
        /*Level level = player.level();
        InteractionHand hand = getTelescopeHand(player);

        if (hand != null && !player.isUsingItem()) {
            // 播放使用音效
            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            // 增加统计
            player.awardStat(Stats.ITEM_USED.get(Items.SPYGLASS));
            // 开始使用物品
            player.startUsingItem(hand);
        }*/
        isUsingCustomTelescope = true;
    }

    public void stopUsingTelescope(Player player) {
        EnhancedPotatoCannon.LOGGER.debug("Stop using at "+(player.level().isClientSide()?"client":"server"));
        /*if (isUsingTelescope(player)) {
            // 播放停止音效
            player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            // 停止使用物品
            player.stopUsingItem();
        }*/
        isUsingCustomTelescope = false;
    }

    public static boolean isUsingTelescope(Player player) {
        return player.isUsingItem() &&
                player.getUseItem().getItem() instanceof SpyglassItem;
    }

    private static InteractionHand getTelescopeHand(Player player) {
        if (player.getMainHandItem().getItem() instanceof SpyglassItem) {
            return InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().getItem() instanceof SpyglassItem) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
