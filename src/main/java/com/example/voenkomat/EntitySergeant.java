package com.example.voenkomat;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Сержант: каска и бронежилет, автомат АК-47. */
public class EntitySergeant extends EntitySoldier {

    public static final ResourceLocation LOOT = new ResourceLocation(VoenkomatMod.MODID, "entities/sergeant");

    public EntitySergeant(World world) {
        super(world);
        this.experienceValue = 10;
    }

    @Override protected String getRankName() { return "Сержант"; }
    @Override protected double getRankHealth() { return 30.0D; }
    @Override protected ItemArmor[] getArmorSet() { return ModItems.SERGEANT_SET; }
    @Override protected ItemStack createWeapon() { return new ItemStack(ModItems.AK47); }
    @Override protected float getGunDamageMul() { return 0.6F; }
    @Override protected float getAimError() { return 1.0F; }
    @Override public String getSkinName() { return "sergeant"; }

    @Override
    protected ResourceLocation getLootTable() { return LOOT; }
}
