package com.example.voenkomat;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/** Рядовой: кепка и куртка, пистолет Макарова. */
public class EntityPrivate extends EntitySoldier {

    public static final ResourceLocation LOOT = new ResourceLocation(VoenkomatMod.MODID, "entities/private");

    public EntityPrivate(World world) {
        super(world);
        this.experienceValue = 6;
    }

    @Override protected String getRankName() { return "Рядовой"; }
    @Override protected double getRankHealth() { return 20.0D; }
    @Override protected ItemArmor[] getArmorSet() { return ModItems.PRIVATE_SET; }
    @Override protected ItemStack createWeapon() { return new ItemStack(ModItems.MAKAROV); }
    @Override protected float getGunDamageMul() { return 0.6F; }
    @Override protected float getAimError() { return 1.2F; }
    @Override public String getSkinName() { return "private"; }

    @Override
    protected ResourceLocation getLootTable() { return LOOT; }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        // у рядовых форма не всегда полная
        if (this.rand.nextFloat() < 0.3F) this.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);
        if (this.rand.nextFloat() < 0.3F) this.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);
    }
}
