package com.example.voenkomat;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/** Базовый класс солдата. Звания: рядовой, сержант, капитан, генерал. */
public abstract class EntitySoldier extends EntityMob {

    private static final DataParameter<Boolean> AIMING =
            EntityDataManager.createKey(EntitySoldier.class, DataSerializers.BOOLEAN);

    public EntitySoldier(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
    }

    // ---- параметры звания (должны возвращать константы: вызываются из конструктора)
    protected abstract String getRankName();
    protected abstract double getRankHealth();
    protected abstract ItemArmor[] getArmorSet();
    protected abstract ItemStack createWeapon();
    protected abstract float getGunDamageMul();
    protected abstract float getAimError();
    public abstract String getSkinName();

    protected double getKnockbackResist() { return 0.0D; }
    public float getModelScale() { return 1.0F; }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(AIMING, Boolean.FALSE);
    }

    public boolean isAiming() { return this.dataManager.get(AIMING); }
    public void setAiming(boolean aiming) { this.dataManager.set(AIMING, aiming); }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIGunAttack(this, 1.0D, 28.0F));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 0.9D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getRankHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(this.getKnockbackResist());
    }

    /** Форма по званию + оружие. */
    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        ItemArmor[] set = this.getArmorSet();
        this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(set[0]));
        this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(set[1]));
        this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(set[2]));
        this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(set[3]));
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, this.createWeapon());
        this.setDropChance(EntityEquipmentSlot.HEAD, 0.12F);
        this.setDropChance(EntityEquipmentSlot.CHEST, 0.12F);
        this.setDropChance(EntityEquipmentSlot.LEGS, 0.12F);
        this.setDropChance(EntityEquipmentSlot.FEET, 0.12F);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.15F);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(difficulty, data);
        this.setEquipmentBasedOnDifficulty(difficulty);
        return data;
    }

    // ---- стрельба
    public boolean hasGun() {
        return this.getHeldItemMainhand().getItem() instanceof ItemGun;
    }

    private ItemGun gun() {
        return (ItemGun) this.getHeldItemMainhand().getItem();
    }

    public int getBurstSize() { return this.hasGun() ? this.gun().getBurstSize() : 1; }
    public int getShotDelay() { return this.hasGun() ? this.gun().getShotDelay() : 10; }
    public int getBurstPause() { return this.hasGun() ? this.gun().getBurstPause() : 20; }

    public void shootAt(EntityLivingBase target) {
        if (!this.hasGun()) return;
        ItemGun gun = this.gun();
        EntityBullet bullet = new EntityBullet(this.world, this);
        bullet.setDamage(gun.getDamage() * this.getGunDamageMul());
        double dx = target.posX - this.posX;
        double dz = target.posZ - this.posZ;
        double dy = target.getEntityBoundingBox().minY + target.height * 0.6D - bullet.posY;
        double dist = MathHelper.sqrt(dx * dx + dz * dz);
        float err = gun.getInaccuracy() + this.getAimError() * (4 - this.world.getDifficulty().getDifficultyId());
        bullet.shoot(dx, dy + dist * 0.01D, dz, gun.getSpeed(), err);
        this.world.spawnEntity(bullet);
        this.playSound(gun.getSound(), 1.2F, gun.getPitch() + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
        this.swingArm(EnumHand.MAIN_HAND);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getCustomNameTag() : this.getRankName();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }
}
