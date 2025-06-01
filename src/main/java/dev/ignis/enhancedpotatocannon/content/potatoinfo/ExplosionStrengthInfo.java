package dev.ignis.enhancedpotatocannon.content.potatoinfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;

public class ExplosionStrengthInfo {
    public float affectRange = 0;
    public float knockBackMultiplier = 0;
    public float knockBackBaseValue = 0;
    public float maxDamage = 0;
    public float penetrateRatio = 0;

    public ExplosionStrengthInfo(){

    }

    public ExplosionStrengthInfo(float affectRange, float knockBackMultiplier, float knockBackBaseValue, float maxDamage, float penetrateRatio) {
        this.affectRange = affectRange;
        this.knockBackMultiplier = knockBackMultiplier;
        this.knockBackBaseValue = knockBackBaseValue;
        this.maxDamage = maxDamage;
        this.penetrateRatio = penetrateRatio;
    }

    public boolean isDefault(){
        return affectRange==0&&knockBackMultiplier==0&&knockBackBaseValue==0&&maxDamage==0&&penetrateRatio==0;
    }

    public void readFromJson(JsonObject jsonObject){
        try{
            var map = jsonObject.asMap();
            affectRange = map.containsKey("affect_radius")?map.get("affect_radius").getAsFloat():0;
            knockBackMultiplier = map.containsKey("explosion_knockback")?map.get("explosion_knockback").getAsFloat():0;
            knockBackBaseValue = knockBackMultiplier/2;
            maxDamage = map.containsKey("explosion_damage")?map.get("explosion_damage").getAsFloat():0;
            penetrateRatio = map.containsKey("penetrate_ratio")?map.get("penetrate_ratio").getAsFloat():0;
        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Cannot load explosion data: "+e.getMessage());
            throw new JsonParseException("Cannot load explosion data");
        }
    }
}
