package dev.ignis.enhancedpotatocannon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = EnhancedPotatoCannon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue HEADSHOT_MULTIPLIER = BUILDER
            .comment("Headshot damage multiplier")
            .defineInRange("headshotMultiplier", 2f, 1, 64);

    private static final ForgeConfigSpec.DoubleValue TELESCOPE_FOV = BUILDER
            .comment("FOV while using telescope")
            .defineInRange("telescopeFOV", 0.5f, 0.01f, 1f);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static double headshotMultiplier;
    public static double telescopeFOV;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event){
        headshotMultiplier = HEADSHOT_MULTIPLIER.get();
        telescopeFOV = TELESCOPE_FOV.get();
    }
}
