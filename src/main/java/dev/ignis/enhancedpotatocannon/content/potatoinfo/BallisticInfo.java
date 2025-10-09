package dev.ignis.enhancedpotatocannon.content.potatoinfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import dev.ignis.enhancedpotatocannon.utils.PotatoProjectileAddonManager;

public class BallisticInfo {
    public float effectiveRange = 256;
    public float decreaseRateOutsideRange = 0;
    public boolean hitSelf = false;

    public BallisticInfo() {
    }

    public BallisticInfo(float effectiveRange, float decreaseRateOverRange, boolean hitSelf) {
        this.effectiveRange = effectiveRange;
        this.decreaseRateOutsideRange = decreaseRateOverRange;
        this.hitSelf = hitSelf;
    }

    public boolean isDefault(){
        return effectiveRange == 256 && decreaseRateOutsideRange == 0 && !hitSelf;
    }

    public float getDamageRatio(float distance){
        if(distance<=effectiveRange){
            return 1;
        }else{
            return Math.max(0,1-(distance-effectiveRange)*decreaseRateOutsideRange);
        }
    }

    public void readFromJson(JsonObject jsonObject){
        try{
            var map = jsonObject.asMap();
            effectiveRange = map.containsKey("effective_range")?map.get("effective_range").getAsFloat():256;
            decreaseRateOutsideRange = map.containsKey("damage_decrease_range_outside_range")?map.get("damage_decrease_range_outside_range").getAsFloat():0;
            hitSelf = map.containsKey("hit_self")?map.get("hit_self").getAsBoolean():false;
        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Cannot load effect data: "+e.getMessage());
            throw new JsonParseException("Cannot load effect data");
        }
    }
}
