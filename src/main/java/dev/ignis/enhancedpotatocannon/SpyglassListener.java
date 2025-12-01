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
        boolean lastTickStatus = isUsingTelescopeClient;
        if (event.phase == TickEvent.Phase.START) {
            if(!event.player.isLocalPlayer()) return;
            boolean isCrouching = event.player.isCrouching();
            boolean hasTelescope = event.player.getMainHandItem().getItem() == Items.SPYGLASS || event.player.getOffhandItem().getItem() == Items.SPYGLASS;
            isUsingTelescopeClient = isCrouching && hasTelescope;
            //if(event.player.level().isClientSide()) EnhancedPotatoCannon.LOGGER.debug("Client status iC:"+isCrouching+"- hT:"+hasTelescope+"- lT:"+lastTickStatus+"- tT:"+getIsUsing(event.player));
            if(hasTelescope){
                if(!lastTickStatus && isUsingTelescopeClient){
                    startUsingTelescope(event.player);
                }else if(lastTickStatus && !isUsingTelescopeClient){
                    stopUsingTelescope(event.player);
                }
            }else{
                isUsingCustomTelescope = false;
            }
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(ViewportEvent.ComputeFov event) {
        if (isUsingCustomTelescope) {
            // 直接修改 FOV，不依赖原版望远镜机制
            event.setFOV(event.getFOV() * Config.telescopeFOV);
        }
    }

    public void startUsingTelescope(Player player) {
        isUsingCustomTelescope = true;
    }

    public void stopUsingTelescope(Player player) {
        isUsingCustomTelescope = false;
    }
}
