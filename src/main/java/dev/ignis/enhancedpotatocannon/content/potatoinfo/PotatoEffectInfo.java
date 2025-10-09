package dev.ignis.enhancedpotatocannon.content.potatoinfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import dev.ignis.enhancedpotatocannon.utils.PotatoProjectileAddonManager;
import net.minecraft.world.effect.MobEffect;

public class PotatoEffectInfo {
    public MobEffect effect = null;
    public boolean isFire = false;
    public int duration = 0;
    public int amplifier = 0;

    public PotatoEffectInfo(){

    }

    public PotatoEffectInfo(MobEffect effect, int duration, int amplifier) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public boolean isDefault(){
        return effect==null&&!isFire&&duration==0&&amplifier==0;
    }

    public void readFromJson(JsonObject jsonObject){
        try{
            var map = jsonObject.asMap();
            String effectId = map.containsKey("effect")?map.get("effect").getAsString():"NO_EFFECT_ID";
            if(!effectId.equals("minecraft:fire")){
                effect = PotatoProjectileAddonManager.getEffectById(effectId);
                if (effect == null) throw new NullPointerException("Cannot get effect " + effectId);
            }else{
                isFire = true;
            }
            duration = map.containsKey("duration")?map.get("duration").getAsInt()*20:0;
            amplifier = map.containsKey("amplifier")?map.get("amplifier").getAsInt():0;
        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Cannot load effect data: "+e.getMessage());
            throw new JsonParseException("Cannot load effect data");
        }
    }
}
