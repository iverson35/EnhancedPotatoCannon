package dev.ignis.enhancedpotatocannon.content.potatoinfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExplosionHitInfo {
    public Level level = null;
    public Vec3 explodePoint = null;
    public Entity directHitEntity = null;
    public AbstractHurtingProjectile potato = null;

    public ExplosionHitInfo(Level level, Vec3 explodePoint, Entity directHitEntity, AbstractHurtingProjectile potato) {
        this.level = level;
        this.explodePoint = explodePoint;
        this.directHitEntity = directHitEntity;
        this.potato = potato;
    }
}
