package com.example.voenkomat;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Огнестрельное оружие: ПКМ — выстрел, у автоматов зажмите ПКМ для очереди. */
public class ItemGun extends Item {

    private final float damage;
    private final int fireDelay;
    private final boolean auto;
    private final float inaccuracy;
    private final float speed;
    private final Item ammo;
    private final SoundEvent sound;
    private final float pitch;
    private final int burstSize;
    private final int shotDelay;
    private final int burstPause;

    public ItemGun(float damage, int fireDelay, boolean auto, float inaccuracy, float speed, Item ammo,
                   SoundEvent sound, float pitch, int burstSize, int shotDelay, int burstPause) {
        this.damage = damage;
        this.fireDelay = fireDelay;
        this.auto = auto;
        this.inaccuracy = inaccuracy;
        this.speed = speed;
        this.ammo = ammo;
        this.sound = sound;
        this.pitch = pitch;
        this.burstSize = burstSize;
        this.shotDelay = shotDelay;
        this.burstPause = burstPause;
        this.setMaxStackSize(1);
    }

    public float getDamage() { return damage; }
    public float getInaccuracy() { return inaccuracy; }
    public float getSpeed() { return speed; }
    public SoundEvent getSound() { return sound; }
    public float getPitch() { return pitch; }
    public int getBurstSize() { return burstSize; }
    public int getShotDelay() { return shotDelay; }
    public int getBurstPause() { return burstPause; }

    private boolean hasAmmo(EntityPlayer p) {
        if (p.capabilities.isCreativeMode) return true;
        for (ItemStack s : p.inventory.mainInventory) {
            if (!s.isEmpty() && s.getItem() == ammo) return true;
        }
        return false;
    }

    private void consumeAmmo(EntityPlayer p) {
        if (p.capabilities.isCreativeMode) return;
        for (ItemStack s : p.inventory.mainInventory) {
            if (!s.isEmpty() && s.getItem() == ammo) {
                s.shrink(1);
                return;
            }
        }
    }

    private void fire(World world, EntityPlayer p) {
        if (world.isRemote) return;
        if (p.getCooldownTracker().hasCooldown(this)) return;
        if (!hasAmmo(p)) {
            world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_DISPENSER_FAIL,
                    SoundCategory.PLAYERS, 0.6F, 1.8F);
            p.getCooldownTracker().setCooldown(this, 10);
            return;
        }
        consumeAmmo(p);
        EntityBullet bullet = new EntityBullet(world, p);
        bullet.setDamage(damage);
        bullet.shoot(p, p.rotationPitch, p.rotationYaw, 0.0F, speed, inaccuracy);
        world.spawnEntity(bullet);
        world.playSound(null, p.posX, p.posY, p.posZ, sound, SoundCategory.PLAYERS, 1.2F,
                pitch + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F);
        p.getCooldownTracker().setCooldown(this, fireDelay);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        fire(world, player);
        if (auto) player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (!auto || !(living instanceof EntityPlayer)) return;
        fire(living.world, (EntityPlayer) living);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
     tooltip.add(TextFormatting.GRAY + "Патроны: " + I18n.format("item." + VoenkomatMod.MODID + "." + ammo.getRegistryName().getResourcePath() + ".name"));
        tooltip.add(TextFormatting.GRAY + "Урон: " + damage + (auto ? "  (автоматический огонь — зажмите ПКМ)" : ""));
    }
}
