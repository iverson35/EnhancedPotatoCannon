package dev.ignis.enhancedpotatocannon.content.potatoinfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;

public class ReflectContext {
    public boolean doReflect = false;
    public int maxReflect = 10;
    public float speedDecay = 0.5f;

    public ReflectContext(){

    }

    public ReflectContext(ReflectContext base){
        doReflect = base.doReflect;
        maxReflect = base.maxReflect;
        speedDecay = base.speedDecay;
    }

    public void readFromJson(JsonObject jsonObject){
        try{
            var map = jsonObject.asMap();
            if(map.containsKey("speed_decay")||map.containsKey("max_reflect")){
                doReflect = true;
                maxReflect = map.containsKey("max_reflect")?map.get("max_reflect").getAsInt():10;
                speedDecay = map.containsKey("speed_decay")?map.get("speed_decay").getAsFloat():0.5f;
            }else{
                doReflect = false;
            }
        }catch (Exception e){
            EnhancedPotatoCannon.LOGGER.error("Cannot load reflect data: "+e.getMessage());
            throw new JsonParseException("Cannot load reflect data");
        }
    }

    @Override
    public String toString() {
        return "ReflectContext{" +
                "doReflect=" + doReflect +
                ", maxReflect=" + maxReflect +
                ", speedDecay=" + speedDecay +
                '}';
    }
}
