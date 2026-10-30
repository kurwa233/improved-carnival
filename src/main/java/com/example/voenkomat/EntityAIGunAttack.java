package com.example.voenkomat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;

/** Стрельба очередями по цели; без оружия — ближний бой. */
public class EntityAIGunAttack extends EntityAIBase {

    private final EntitySoldier mob;
    private final double speed;
    private final float rangeSq;
    private int cooldown = 20;
    private int seeTime;
    private int burstCount;

    public EntityAIGunAttack(EntitySoldier mob, double speed, float range) {
        this.mob = mob;
        this.speed = speed;
        this.rangeSq = range * range;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase t = this.mob.getAttackTarget();
        return t != null && t.isEntityAlive();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.mob.setAiming(this.mob.hasGun());
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.mob.setAiming(false);
        this.seeTime = 0;
        this.burstCount = 0;
        this.cooldown = 20;
    }

    @Override
    public void updateTask() {
        EntityLivingBase t = this.mob.getAttackTarget();
        if (t == null) return;
        double d = this.mob.getDistanceSq(t.posX, t.getEntityBoundingBox().minY, t.posZ);
        boolean see = this.mob.getEntitySenses().canSee(t);
        this.seeTime = see ? this.seeTime + 1 : 0;
        this.mob.getLookHelper().setLookPositionWithEntity(t, 30.0F, 30.0F);

        if (!this.mob.hasGun()) {
            this.mob.setAiming(false);
            this.mob.getNavigator().tryMoveToEntityLiving(t, this.speed);
            if (this.cooldown > 0) {
                this.cooldown--;
            } else if (d < 4.0D) {
                this.mob.swingArm(EnumHand.MAIN_HAND);
                this.mob.attackEntityAsMob(t);
                this.cooldown = 20;
            }
            return;
        }

        this.mob.setAiming(true);
        if (see && d <= this.rangeSq * 0.5D && this.seeTime >= 5) {
            this.mob.getNavigator().clearPath();
        } else {
            this.mob.getNavigator().tryMoveToEntityLiving(t, this.speed);
        }

        if (this.cooldown > 0) {
            this.cooldown--;
        } else if (see && d <= this.rangeSq) {
            this.mob.shootAt(t);
            this.burstCount++;
            if (this.burstCount >= this.mob.getBurstSize()) {
                this.burstCount = 0;
                this.cooldown = this.mob.getBurstPause();
            } else {
                this.cooldown = this.mob.getShotDelay();
            }
        }
    }
}
