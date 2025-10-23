package dev.ignis.enhancedpotatocannon.mixin;

import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import dev.ignis.enhancedpotatocannon.Config;
import dev.ignis.enhancedpotatocannon.EnhancedPotatoCannon;
import dev.ignis.enhancedpotatocannon.content.potatoinfo.*;
import dev.ignis.enhancedpotatocannon.utils.CommonUtil;
import dev.ignis.enhancedpotatocannon.utils.PotatoProjectileAddonManager;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Mixin(PotatoProjectileEntity.class)
public abstract class PotatoProjectileEntityMixin extends AbstractHurtingProjectile{
    @Unique
    String _$itemId = "minecraft:air";

    @Shadow(remap = false)
    protected PotatoCannonProjectileType type;

    @Shadow(remap = false)
    protected float additionalDamageMult;

    @Shadow protected abstract void onHitEntity(EntityHitResult ray);

    @Shadow protected ItemStack stack;
    @Unique
    ExplosionStrengthInfo _$explodeStrength = new ExplosionStrengthInfo();

    @Unique
    List<PotatoEffectInfo> _$effects = new LinkedList<>();

    @Unique
    ReflectContext _$reflect = null;

    @Unique
    BallisticInfo _$ballistic = new BallisticInfo();
    @Unique
    Vec3 _$shootFromPos = Vec3.ZERO;

    protected PotatoProjectileEntityMixin(EntityType<? extends AbstractHurtingProjectile> p_36833_, Level p_36834_) {
        super(p_36833_, p_36834_);
    }

    @Inject(
            method = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;setItem(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "TAIL"),
            remap = false
    )
    private void atInit(ItemStack stack, CallbackInfo ci){
        _$itemId = _$mixinGetItemId(stack);
        _$init(_$itemId);
    }

    @Inject(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(value = "HEAD")
    )
    private void beforeHitEntity(EntityHitResult ray, CallbackInfo ci){

    }

    @Inject(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;getProjectileType()Lcom/simibubi/create/content/equipment/potatoCannon/PotatoCannonProjectileType;",ordinal = -1),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onCalcDamage(EntityHitResult ray, CallbackInfo ci, Vec3 hit, Entity target){
        Entity targetEntity = ray.getEntity();

        if((!targetEntity.level().isClientSide) && (targetEntity instanceof Monster || targetEntity instanceof ServerPlayer)){
            Vec3 startPos = this.getPosition(1);
            Vec3 vecPos = this.getDeltaMovement().normalize();
            Vec3 targetEyePos = targetEntity.getEyePosition();
            Vec3 toTarget = targetEyePos.subtract(startPos);
            double hitHeight;
            double projection = toTarget.dot(vecPos);
            if (projection < 0.0) {
                hitHeight = startPos.y;
            }else{
                Vec3 closestPoint = startPos.add(vecPos.scale(projection));
                hitHeight = closestPoint.y;
            }
            if(hitHeight>(targetEyePos.y-0.25)){
                additionalDamageMult *= (float) Config.headshotMultiplier;
                _$playSound((ServerLevel) targetEntity.level(),targetEyePos,SoundEvents.SLIME_JUMP,1f,0.5f,16);
                if(this.getOwner() instanceof ServerPlayer owner){
                    _$playSound(owner,owner.position(),SoundEvents.TRIDENT_HIT,0.5f,0.2f);
                }
            }
        }

        if(_$ballistic!=null){
            additionalDamageMult *= _$getRangeDamageRatio(this.getPosition(1));
            //EnhancedPotatoCannon.LOGGER.debug("Ratio: "+_$getRangeDamageRatio(this.getPosition(1))+" for distance: "+_$shootFromPos.distanceTo(this.getPosition(1)));
        }

        if(CommonUtil.isFriendly(getOwner(),ray.getEntity())){
            additionalDamageMult = 0;
        }

    }

    @Inject(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;pop(Lnet/minecraft/world/phys/Vec3;)V", ordinal = -1),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void addAfterHitEntityEffect(EntityHitResult ray, CallbackInfo ci, Vec3 hit){
        Entity targetEntity = ray.getEntity();

        if(!_$isSpecial(_$itemId)) return;

        if(_$effects!=null&& !_$effects.isEmpty()) _$addEffects(this.getOwner(),_$effects,1,targetEntity);

        if(!this.level().isClientSide && _$explodeStrength.affectRange>0) _$causeExplosion(ray,null);
    }

    @Inject(
            method = "onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void beforeHitBlock(BlockHitResult ray, CallbackInfo ci){
        if(_$reflect == null){
            _$reflect = PotatoProjectileAddonManager.getReflectContext(_$itemId);
            //EnhancedPotatoCannon.LOGGER.error("Reflect is null");
        }
        //EnhancedPotatoCannon.LOGGER.debug("RC: "+(this.level().isClientSide?"Client-":"Server-")+_$reflect.toString());


        if(this.level().isClientSide() && _$reflect.doReflect){
            //EnhancedPotatoCannon.LOGGER.debug("Canceling client kill");
            //EnhancedPotatoCannon.LOGGER.debug(_$reflect==null?"RIN":_$reflect.toString());
            ci.cancel();
        }

        if(_$reflect.doReflect&&_$reflect.maxReflect!=0&&this.getDeltaMovement().length()>=0.25){

            //EnhancedPotatoCannon.LOGGER.debug("Reflected");
            if(_$reflect.maxReflect>0) --_$reflect.maxReflect;
            Vec3 velocity = this.getDeltaMovement();
            Vec3i normalInt = ray.getDirection().getNormal();
            Vec3 reflect = _$reflect(velocity,new Vec3(normalInt.getX(),normalInt.getY(),normalInt.getZ()));
            if(Double.isNaN(reflect.x)||Double.isNaN(reflect.y)||Double.isNaN(reflect.z)){
                EnhancedPotatoCannon.LOGGER.error("Cannot reflect potato");
            }
            else{
                this.setDeltaMovement(reflect.multiply(_$reflect.speedDecay,_$reflect.speedDecay,_$reflect.speedDecay));
                if(this.level() instanceof ServerLevel serverLevel){
                    for(var player:serverLevel.players()){
                        if(player.distanceTo(this)>48) continue;
                        try{
                            var blockState = level().getBlockState(ray.getBlockPos());
                            var soundPos = ray.getLocation();
                            var soundEvent = blockState.getBlock().getSoundType(blockState,serverLevel,ray.getBlockPos(),null).getBreakSound();
                            var holder = ForgeRegistries.SOUND_EVENTS.getHolder(soundEvent).get();
                            ClientboundSoundPacket soundPacket = new ClientboundSoundPacket(holder, SoundSource.PLAYERS, soundPos.x, soundPos.y, soundPos.z, 0.5f, 1f, 0);
                            player.connection.send(soundPacket);
                            serverLevel.sendParticles(ParticleTypes.CLOUD, soundPos.x, soundPos.y, soundPos.z, 5, 0.1, 0.1, 0.1, 0.1);
                        }catch (Exception e){
                            EnhancedPotatoCannon.LOGGER.error("Cannot send potato particle to player");
                        }
                    }

                }
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void onReadAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        //EnhancedPotatoCannon.LOGGER.debug("GET: "+nbt.toString());
        if(nbt.contains("DoReflect")) {
            if(_$reflect == null) _$reflect = PotatoProjectileAddonManager.getReflectContext(_$itemId);
            _$reflect.doReflect = nbt.getBoolean("DoReflect");
            if(nbt.contains("MaxReflect")) _$reflect.maxReflect=nbt.getInt("MaxReflect");
            if(nbt.contains("SpeedDecay")) _$reflect.speedDecay=nbt.getFloat("SpeedDecay");
        }
        if(nbt.contains("EffectiveRange")){
            if(_$ballistic == null) _$ballistic = new BallisticInfo();
            _$ballistic.effectiveRange = nbt.getFloat("EffectiveRange");
            _$ballistic.decreaseRateOutsideRange = nbt.getFloat("DecreaseRateOutsideRange");
            _$ballistic.hitSelf = nbt.getBoolean("DoReflect");
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void onAddAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        String id = _$mixinGetItemId(this.stack);
        var reflectCopy = PotatoProjectileAddonManager.getReflectContext(id);
        if(reflectCopy.doReflect){
            nbt.putInt("MaxReflect", reflectCopy.maxReflect);
            nbt.putFloat("SpeedDecay", reflectCopy.speedDecay);
            nbt.putBoolean("DoReflect", reflectCopy.doReflect);
        }
        var ballisticCopy = PotatoProjectileAddonManager.getBallisticInfo(id);
        if(ballisticCopy!=null){
            nbt.putFloat("EffectiveRange",ballisticCopy.effectiveRange);
            nbt.putFloat("DecreaseRateOutsideRange",ballisticCopy.decreaseRateOutsideRange);
            nbt.putBoolean("HitSelf",ballisticCopy.hitSelf);
        }
        //EnhancedPotatoCannon.LOGGER.debug("SEND: "+nbt.toString());
    }

    @Inject(
            method = "onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;pop(Lnet/minecraft/world/phys/Vec3;)V", ordinal = -1),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void addAfterHitBlockEffect(BlockHitResult ray, CallbackInfo ci, Vec3 hit){

        if(!_$isSpecial(_$itemId)) return;

        if(!this.level().isClientSide && _$explodeStrength.affectRange>0) _$causeExplosion(null,ray);
    }

    @Inject(
            method = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;tick()V",
            at = @At(value = "HEAD"),
            remap = true
    )
    private void onTickStarts(CallbackInfo ci){
        if(_$shootFromPos.equals(Vec3.ZERO)){
            _$shootFromPos = this.getPosition(1);
        }
    }

    @Inject(
            method = "Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;tick()V",
            at = @At(value = "TAIL"),
            remap = true
    )
    private void onTickEnds(CallbackInfo ci){
        if(_$ballistic!=null){
            if(_$ballistic.hitSelf&&this.getOwner()!=null){
                this.onHitEntity(new EntityHitResult(this.getOwner(),this.getOwner().getPosition(1).add(0,0.1f,0)));
            }
            if(_$getRangeDamageRatio(this.getPosition(1))<=0){
                if(!this.level().isClientSide && _$explodeStrength.affectRange>0) _$createExplosion(this.getOwner(),new ExplosionHitInfo(this.level(),this.getPosition(1),null,this),_$explodeStrength,_$effects);
                this.kill();
            }
        }
    }

    @Unique
    private void _$init(String id){
        if(PotatoProjectileAddonManager.isSpecialPotato(id)){
            _$explodeStrength = PotatoProjectileAddonManager.getExplosionStrengthInfo(id);
            _$effects = PotatoProjectileAddonManager.getPotatoEffectInfos(id);
            _$ballistic = PotatoProjectileAddonManager.getBallisticInfo(id);
            if(_$reflect == null) _$reflect = PotatoProjectileAddonManager.getReflectContext(_$itemId);
        }
    }

    @Unique
    private boolean _$isSpecial(String id){
        return PotatoProjectileAddonManager.isSpecialPotato(id);
    }

    @Unique
    private String _$mixinGetItemId(ItemStack stack){
        var item = stack.getItem();
        var key = ForgeRegistries.ITEMS.getKey(item);
        if (key != null)
            return key.toString();
        else {
            EnhancedPotatoCannon.LOGGER.error("Cannot find item " + item.toString());
            return "minecraft:air";
        }
    }

    @Unique
    private void _$causeExplosion(@Nullable EntityHitResult rayEntity,@Nullable BlockHitResult rayBlock){
        Vec3 location = null;
        Entity directHit = null;
        if(rayEntity != null){
            location = rayEntity.getLocation();
            directHit = rayEntity.getEntity();
        }else if(rayBlock != null){
            location = rayBlock.getLocation();
        }else return;
        _$createExplosion(this.getOwner(),new ExplosionHitInfo(this.level(),location,directHit,this),_$explodeStrength,_$effects);
    }

    @Unique
    private float _$getRangeDamageRatio(Vec3 nowPos){
        if(_$ballistic!=null){
            return _$ballistic.getDamageRatio((float) nowPos.distanceTo(_$shootFromPos));
        }else return 1;
    }

    @Unique
    private static void _$createExplosion(Entity owner,ExplosionHitInfo hitInfo, ExplosionStrengthInfo strengthInfo,List<PotatoEffectInfo> effectInfos){
        //var explosion = new Explosion(level,potato,explodePoint.x,explodePoint.y,explodePoint.z,strength,false,Explosion.BlockInteraction.KEEP);

        Level level = hitInfo.level;
        Vec3 explodePoint = hitInfo.explodePoint;
        Entity directHitEntity = hitInfo.directHitEntity;
        AbstractHurtingProjectile potato = hitInfo.potato;

        float affectRange = strengthInfo.affectRange;
        float strength = strengthInfo.affectRange;
        float knockBackMultiplier = strengthInfo.knockBackMultiplier;
        float knockBackBaseValue = strengthInfo.knockBackBaseValue;
        float maxDamage = strengthInfo.maxDamage;
        float penetrateRatio = strengthInfo.penetrateRatio;


        if(level instanceof ServerLevel serverLevel){
            //EnhancedPotatoCannon.LOGGER.debug("ServerSideExplode");

            //Entities
            final AABB area = new AABB(explodePoint.x - affectRange, explodePoint.y - affectRange, explodePoint.z - affectRange,explodePoint.x + affectRange, explodePoint.y + affectRange, explodePoint.z + affectRange);
            final List<Entity> affectedEntityList = level.getEntities(potato,area);
            if(directHitEntity!=null&&!affectedEntityList.contains(directHitEntity)){
                affectedEntityList.add(directHitEntity);
            }

            Hashtable<ServerPlayer,Vec3> playerKnockBackTable = new Hashtable<>();

            for(Entity entity:affectedEntityList){
                Vec3 entityPos = entity.getPosition(1).add(entity.getEyePosition()).multiply(0.5,0.5,0.5);
                double distance = entityPos.distanceTo(explodePoint);

                if(distance>affectRange) continue;

                double distanceRatio = directHitEntity==entity?1:Math.max(0,1-distance/affectRange);

                if(entity instanceof LivingEntity livingEntity){


                    //hurt
                    float damage = maxDamage*(float)Math.pow(distanceRatio,2);

                    //knock back
                    Vec3 knockBackVec = directHitEntity==entity?new Vec3(0,1,0):entityPos.add(explodePoint.reverse()).normalize();
                    double knockBackForce = knockBackBaseValue+distanceRatio*knockBackMultiplier;
                    if(knockBackMultiplier<0){
                        knockBackForce = -knockBackBaseValue+(1f-distanceRatio)*-knockBackMultiplier;
                        knockBackVec = knockBackVec.reverse();
                        //_$sendMessageToOwner(potato,"KnockBack:"+knockBackForce+" - "+knockBackVec);
                    }


                    //detect wall
                    BlockHitResult resultEye = level.clip(new ClipContext(explodePoint,entity.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,null));
                    BlockHitResult resultFoot = level.clip(new ClipContext(explodePoint,entity.getPosition(1), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,null));
                    BlockHitResult resultCenter = level.clip(new ClipContext(explodePoint,entityPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,null));
                    float coverRatio = 0;
                    if(resultEye.getType().equals(HitResult.Type.BLOCK)) coverRatio += 0.5f;
                    if(resultCenter.getType().equals(HitResult.Type.BLOCK)) coverRatio += 0.25f;
                    if(resultFoot.getType().equals(HitResult.Type.BLOCK)) coverRatio += 0.25f;
                    if(directHitEntity==entity) coverRatio = 0;

                    //apply

                    //_$sendMessageToOwner(potato,"Cover="+coverRatio+",Parts="+coverParts);

                    float affectRatio = 1;
                    if(penetrateRatio<=1){
                        if(penetrateRatio==0&&coverRatio==1){
                            //_$sendMessageToOwner(potato,"FULL Cover");
                            continue;
                        }
                        affectRatio = (1-(coverRatio*(1-penetrateRatio)));
                        damage *= affectRatio;
                        knockBackForce *= affectRatio;
                        //_$sendMessageToOwner(potato,"Affect="+affectRatio);
                    }

                    if(maxDamage!=0) livingEntity.hurt(CreateDamageSources.potatoCannon(level, potato, potato.getOwner()), damage);

                    if(knockBackBaseValue==0&&knockBackMultiplier==0){}
                    else if(entity instanceof ServerPlayer serverPlayer){
                        Vec3 scaledFlatKnockback = (new Vec3(knockBackVec.x, 0.0, knockBackVec.z)).normalize().scale(knockBackForce);

                        Vec3 knockBack = new Vec3(scaledFlatKnockback.x,serverPlayer.onGround()?Math.min(0.4,knockBackForce):0,scaledFlatKnockback.z);
                        playerKnockBackTable.put(serverPlayer,knockBack.multiply(1,1,1));
                    }else{
                        livingEntity.knockback(knockBackForce,-knockBackVec.x,-knockBackVec.z);
                    }

                    if(effectInfos!=null&&!effectInfos.isEmpty()&&entity!=directHitEntity){
                        _$addEffects(owner,effectInfos,affectRatio,entity);
                    }

                    //inform

                    //_$sendMessageToOwner(potato,"Entity="+livingEntity.getMobType()+",Damage="+damage);
                    //_$sendMessageToOwner(potato,"Force="+knockBackForce+",DistanceRatio="+distanceRatio);
                }
            }

            //client packet
            for (ServerPlayer player : serverLevel.players()) {
                var knockBack = playerKnockBackTable.getOrDefault(player,Vec3.ZERO);
                int realStrength = (int)strength;
                if(effectInfos!=null&&!effectInfos.isEmpty()){
                    try{
                        var holder = ForgeRegistries.SOUND_EVENTS.getHolder(SoundEvents.GLASS_BREAK).get();
                        ClientboundSoundPacket soundPacket = new ClientboundSoundPacket(holder, SoundSource.PLAYERS, explodePoint.x, explodePoint.y, explodePoint.z, 1f, 1f, 0);
                        player.connection.send(soundPacket);
                        serverLevel.sendParticles(ParticleTypes.EFFECT,explodePoint.x,explodePoint.y,explodePoint.z,(int)affectRange*5,(double)affectRange/4,(double)affectRange/4,(double)affectRange/4,affectRange/10);

                    }catch (Exception e){
                        //EnhancedPotatoCannon.LOGGER.debug("Cannot play sound: "+e.getMessage());
                    }
                }
                var packet = new ClientboundExplodePacket(explodePoint.x,explodePoint.y,explodePoint.z,realStrength,new LinkedList<>(),knockBack);
                if(maxDamage!=0||Math.abs(knockBackMultiplier)>0.01)player.connection.send(packet);
            }
        }
        //else{
            //EnhancedPotatoCannon.LOGGER.debug("ClientSideExplode");
        //}
    }

    @Unique
    private static void _$addEffects(Entity owner,List<PotatoEffectInfo> effects, float durationRatio, Entity entity){
        if(!(entity instanceof LivingEntity)) return;
        for(var effect : effects){
            int duration = (int)(durationRatio*effect.duration);
            if((effect.effect==null&&!effect.isFire)||effect.amplifier<0||effect.amplifier>255||duration<=0) continue;
            if(!effect.isFire){
                if(!CommonUtil.isFriendly(owner,entity)||CommonUtil.isBeneficialEffect(effect.effect)){
                    MobEffectInstance effectInstance = new MobEffectInstance(effect.effect, duration, effect.amplifier);
                    ((LivingEntity) entity).addEffect(effectInstance);
                }
            }else{
                if(!CommonUtil.isFriendly(owner,entity)) entity.setSecondsOnFire((int)Math.ceil((float)duration/20f));
            }
            //EnhancedPotatoCannon.LOGGER.debug("Apply "+effect.effect+" for "+duration);
        }
    }

    @Unique
    private static void _$sendMessageToOwner(AbstractHurtingProjectile potato,String string){
        try{
            Objects.requireNonNull(potato.getOwner()).sendSystemMessage(MutableComponent.create(new LiteralContents(string)));
        }catch (Exception e){
            //EnhancedPotatoCannon.LOGGER.debug("Cannot find potato owner");
        }
    }

    @Unique
    private static Vec3 _$reflect(Vec3 velocity, Vec3 normal) {
        double normalDotNormal = normal.dot(normal);
        double projectionFactor = velocity.dot(normal) / normalDotNormal;
        double reflectedX = velocity.x - 2 * projectionFactor * normal.x;
        double reflectedY = velocity.y - 2 * projectionFactor * normal.y;
        double reflectedZ = velocity.z - 2 * projectionFactor * normal.z;

        return new Vec3(reflectedX, reflectedY, reflectedZ);
    }

    @Unique
    private static void _$playSound(ServerLevel level, Vec3 pos, SoundEvent soundEvent,float volume,float pitch,double radius){
        var holder = ForgeRegistries.SOUND_EVENTS.getHolder(soundEvent).get();
        for (ServerPlayer player : level.players()) {
            if(player.position().distanceTo(pos)<radius){
                ClientboundSoundPacket soundPacket = new ClientboundSoundPacket(holder, SoundSource.PLAYERS, pos.x, pos.y, pos.z, volume, pitch, 0);
                player.connection.send(soundPacket);
            }
        }
    }

    @Unique
    private static void _$playSound(ServerPlayer player,Vec3 pos, SoundEvent soundEvent,float volume,float pitch){
        ServerLevel level = player.serverLevel();
        var holder = ForgeRegistries.SOUND_EVENTS.getHolder(soundEvent).get();
        ClientboundSoundPacket soundPacket = new ClientboundSoundPacket(holder, SoundSource.PLAYERS, pos.x, pos.y, pos.z, volume, pitch, 0);
        player.connection.send(soundPacket);
    }
}

