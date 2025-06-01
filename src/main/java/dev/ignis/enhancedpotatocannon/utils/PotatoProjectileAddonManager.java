package dev.ignis.enhancedpotatocannon.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import dev.ignis.enhancedpotatocannon.content.potatoinfo.PotatoEffectInfo;
import dev.ignis.enhancedpotatocannon.content.potatoinfo.ExplosionStrengthInfo;
import dev.ignis.enhancedpotatocannon.content.potatoinfo.ReflectContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class PotatoProjectileAddonManager {
    public static MobEffect getEffectById(String id){
        var effect = ForgeRegistries.MOB_EFFECTS.getDelegate(ResourceLocation.tryParse(id));

        try{
            return effect.get().get();
        }catch (Exception ignored){
            return null;
        }
    }
    private static Set<String> specialEffectPotatoes = new HashSet<>();
    private static Hashtable<String, ExplosionStrengthInfo> strengthInfoHashtable = new Hashtable<>();
    private static Hashtable<String, List<PotatoEffectInfo>> effectInfoHashtable = new Hashtable<>();
    private static Hashtable<String, ReflectContext> reflectHashtable = new Hashtable<>();

    private static void clear(){
        specialEffectPotatoes = new HashSet<>();
        strengthInfoHashtable = new Hashtable<>();
        effectInfoHashtable = new Hashtable<>();
        reflectHashtable = new Hashtable<>();
    }

    private static void insertDataFromJson(JsonObject jsonObject){
        try{
            ExplosionStrengthInfo strengthInfo = new ExplosionStrengthInfo();
            List<PotatoEffectInfo> effectInfos = new LinkedList<>();
            ReflectContext reflectContext = new ReflectContext();
            List<JsonElement> itemIdsJson = jsonObject.get("items").getAsJsonArray().asList();
            List<String> itemIds = new LinkedList<>();
            for(var id:itemIdsJson){
                itemIds.add(id.getAsString());
            }

            strengthInfo.readFromJson(jsonObject);

            reflectContext.readFromJson(jsonObject);

            if(jsonObject.asMap().containsKey("effects")){
                List<JsonElement> effectsJson = jsonObject.getAsJsonArray("effects").asList();
                for(var element:effectsJson){
                    PotatoEffectInfo effectInfo = new PotatoEffectInfo();
                    effectInfo.readFromJson((JsonObject) element);
                    if(!effectInfo.isDefault()) effectInfos.add(effectInfo);
                }
            }

            if(!strengthInfo.isDefault()||!effectInfos.isEmpty()||reflectContext.doReflect){
                for(var id:itemIds){
                    specialEffectPotatoes.add(id);
                    strengthInfoHashtable.put(id,strengthInfo);
                    effectInfoHashtable.put(id,effectInfos);
                    reflectHashtable.put(id,reflectContext);
                    EnhancedPotatoCannon.LOGGER.debug("Loaded data: "+id);
                }
            }

        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Cannot load data: "+e.getMessage());
        }

    }

    public static boolean isSpecialPotato(String id){
        return specialEffectPotatoes.contains(id);
    }

    public static ExplosionStrengthInfo getExplosionStrengthInfo(String id){
        return strengthInfoHashtable.get(id);
    }

    public static List<PotatoEffectInfo> getPotatoEffectInfos(String id){
        return effectInfoHashtable.get(id);
    }

    public static ReflectContext getReflectContext(String id){
        if(reflectHashtable.containsKey(id)){
            return new ReflectContext(reflectHashtable.get(id));
        }else return new ReflectContext();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {

        private static final Gson GSON = new Gson();

        public static final PotatoProjectileAddonManager.ReloadListener INSTANCE = new PotatoProjectileAddonManager.ReloadListener();

        protected ReloadListener() {
            super(GSON, "potato_cannon_projectile_types");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
            clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                JsonElement element = entry.getValue();
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    insertDataFromJson(object);
                }
            }
        }
    }
}
