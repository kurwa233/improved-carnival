package com.example.voenkomat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

/** Генерал — босс здания: полная парадная форма, пулемёт ПКМ, 150 HP, полоса здоровья. */
public class EntityGeneral extends EntitySoldier {

    public static final ResourceLocation LOOT = new ResourceLocation(VoenkomatMod.MODID, "entities/general");

    private final BossInfoServer bossInfo = new BossInfoServer(new TextComponentString("Генерал"),
            BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10);

    public EntityGeneral(World world) {
        super(world);
        this.setSize(0.7F, 2.25F);
        this.experienceValue = 100;
    }

    @Override protected String getRankName() { return "Генерал"; }
    @Override protected double getRankHealth() { return 150.0D; }
    @Override protected ItemArmor[] getArmorSet() { return ModItems.GENERAL_SET; }
    @Override protected ItemStack createWeapon() { return new ItemStack(ModItems.PKM); }
    @Override protected float getGunDamageMul() { return 0.8F; }
    @Override protected float getAimError() { return 0.6F; }
    @Override protected double getKnockbackResist() { return 0.6D; }
    @Override public String getSkinName() { return "general"; }
    @Override public float getModelScale() { return 1.15F; }

    @Override
    protected ResourceLocation getLootTable() { return LOOT; }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }
}
