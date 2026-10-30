package com.example.voenkomat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/** Пуля: быстрая, почти без падения, не бьёт «своих» солдат. */
public class EntityBullet extends EntityThrowable {

    private float damage = 5.0F;

    public EntityBullet(World world) {
        super(world);
        this.setSize(0.1F, 0.1F);
    }

    public EntityBullet(World world, EntityLivingBase thrower) {
        super(world, thrower);
        this.setSize(0.1F, 0.1F);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.003F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        } else if (this.ticksExisted > 80) {
            this.setDead();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        EntityLivingBase shooter = this.getThrower();
        if (result.entityHit != null) {
            Entity target = result.entityHit;
            if (target == shooter) return;
            if (shooter instanceof EntitySoldier && target instanceof EntitySoldier) return; // не бьют друг друга
            if (!this.world.isRemote) {
                target.attackEntityFrom(DamageSource.causeThrownDamage(this, shooter), this.damage);
                target.hurtResistantTime = 0; // чтобы очередь наносила урон каждой пулей
            }
        } else if (!this.world.isRemote) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_HIT,
                    SoundCategory.NEUTRAL, 0.6F, 1.8F);
        }
        if (!this.world.isRemote) {
            this.setDead();
        }
    }
}
