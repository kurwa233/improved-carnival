package com.example.voenkomat;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/** Враждебный солдат в военной форме (окрашенная кожаная броня цвета хаки). */
public class EntitySoldier extends EntityMob {

    public static final ResourceLocation LOOT = new ResourceLocation(VoenkomatMod.MODID, "entities/soldier");

    public EntitySoldier(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
        this.experienceValue = 8;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
    }

    private static ItemStack dyed(Item item, int color) {
        ItemStack stack = new ItemStack(item);
        ((ItemArmor) item).setColor(stack, color);
        return stack;
    }

    /** Военная форма: хаки-шапка (иногда стальная каска), китель, штаны, чёрные берцы + клинок. */
    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        this.setItemStackToSlot(EntityEquipmentSlot.HEAD,
                this.rand.nextFloat() < 0.25F ? new ItemStack(Items.IRON_HELMET)
                                              : dyed(Items.LEATHER_HELMET, 0x556B2F));
        this.setItemStackToSlot(EntityEquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE, 0x556B2F));
        this.setItemStackToSlot(EntityEquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS, 0x4A5A2A));
        this.setItemStackToSlot(EntityEquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS, 0x2B2B2B));
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
                new ItemStack(this.rand.nextFloat() < 0.3F ? Items.IRON_SWORD : Items.STONE_SWORD));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(difficulty, data);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        return data;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getCustomNameTag() : "Солдат";
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT;
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
