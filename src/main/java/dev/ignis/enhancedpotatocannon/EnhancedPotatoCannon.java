package dev.ignis.enhancedpotatocannon;

import com.mojang.logging.LogUtils;
import dev.ignis.enhancedpotatocannon.utils.PotatoProjectileAddonManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EnhancedPotatoCannon.MODID)
public class EnhancedPotatoCannon {

    public static final String MODID = "enhancedpotatocannon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnhancedPotatoCannon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册生命周期事件到 Mod 事件总线
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // 注册游戏事件到 Forge 事件总线
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // 使用 @SubscribeEvent 注解
    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("EnhancedPotatoCannon Installed!");
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new SpyglassListener());
        });
    }

    @SubscribeEvent
    public void addReloadListeners(final AddReloadListenerEvent event) {
        EnhancedPotatoCannon.LOGGER.debug("Start data pack reload");
        event.addListener(PotatoProjectileAddonManager.ReloadListener.INSTANCE);
    }
}