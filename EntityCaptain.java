package com.example.voenkomat;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Капитан: фуражка, китель с погонами; снайперская винтовка СВД или АК-47. */
public class EntityCaptain extends EntitySoldier {

    public static final ResourceLocation LOOT = new ResourceLocation(VoenkomatMod.MODID, "entities/captain");

    public EntityCaptain(World world) {
        super(world);
        this.experienceValue = 20;
    }

    @Override protected String getRankName() { return "Капитан"; }
    @Override protected double getRankHealth() { return 45.0D; }
    @Override protected ItemArmor[] getArmorSet() { return ModItems.CAPTAIN_SET; }
    @Override protected float getGunDamageMul() { return 0.6F; }
    @Override protected float getAimError() { return 0.4F; }
    @Override public String getSkinName() { return "captain"; }

    @Override
    protected ItemStack createWeapon() {
        return new ItemStack(this.rand.nextFloat() < 0.6F ? ModItems.SVD : ModItems.AK47);
    }

    @Override
    protected double getKnockbackResist() { return 0.3D; }

    @Override
    protected ResourceLocation getLootTable() { return LOOT; }
}
