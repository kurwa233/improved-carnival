package com.example.voenkomat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

/** Все предметы мода: патроны, оружие и 4 комплекта формы по званиям. */
public class ModItems {

    /** Список обязательно объявлен ПЕРВЫМ — в него добавляет reg(). */
    public static final List<Item> ALL = new ArrayList<>();

    // ---- патроны
    public static final Item AMMO_9X18 = reg(new Item(), "ammo_9x18");
    public static final Item AMMO_762 = reg(new Item(), "ammo_762");

    // ---- оружие: урон, пауза(тики), авто, разброс, скорость пули, патрон, звук, тон,
    //              очередь у мобов: размер / пауза между выстрелами / пауза после очереди
    public static final ItemGun MAKAROV = reg(new ItemGun(5.0F, 8, false, 2.0F, 3.5F, AMMO_9X18,
            SoundEvents.ENTITY_FIREWORK_BLAST, 1.7F, 2, 8, 30), "makarov");
    public static final ItemGun AK47 = reg(new ItemGun(6.0F, 3, true, 3.0F, 3.5F, AMMO_762,
            SoundEvents.ENTITY_FIREWORK_BLAST, 1.0F, 4, 4, 32), "ak47");
    public static final ItemGun SVD = reg(new ItemGun(12.0F, 25, false, 0.2F, 6.0F, AMMO_762,
            SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 0.8F, 1, 1, 50), "svd");
    public static final ItemGun PKM = reg(new ItemGun(6.0F, 2, true, 4.5F, 3.5F, AMMO_762,
            SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 1.1F, 8, 2, 38), "pkm");

    // ---- материалы формы (reduction: ботинки, штаны, нагрудник, шлем)
    public static final ItemArmor.ArmorMaterial MAT_PRIVATE = EnumHelper.addArmorMaterial("VOENKOMAT_PRIVATE",
            "voenkomat:private", 12, new int[] {1, 1, 2, 1}, 9, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
    public static final ItemArmor.ArmorMaterial MAT_SERGEANT = EnumHelper.addArmorMaterial("VOENKOMAT_SERGEANT",
            "voenkomat:sergeant", 18, new int[] {2, 2, 3, 2}, 9, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0F);
    public static final ItemArmor.ArmorMaterial MAT_CAPTAIN = EnumHelper.addArmorMaterial("VOENKOMAT_CAPTAIN",
            "voenkomat:captain", 26, new int[] {3, 3, 5, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F);
    public static final ItemArmor.ArmorMaterial MAT_GENERAL = EnumHelper.addArmorMaterial("VOENKOMAT_GENERAL",
            "voenkomat:general", 36, new int[] {3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F);

    // ---- комплекты: [0]=шлем [1]=нагрудник [2]=штаны [3]=ботинки
    public static final ItemArmor[] PRIVATE_SET = armorSet("private", MAT_PRIVATE);
    public static final ItemArmor[] SERGEANT_SET = armorSet("sergeant", MAT_SERGEANT);
    public static final ItemArmor[] CAPTAIN_SET = armorSet("captain", MAT_CAPTAIN);
    public static final ItemArmor[] GENERAL_SET = armorSet("general", MAT_GENERAL);

    private static ItemArmor[] armorSet(String rank, ItemArmor.ArmorMaterial mat) {
        ItemArmor[] s = new ItemArmor[4];
        s[0] = reg(new ItemArmor(mat, 0, EntityEquipmentSlot.HEAD), rank + "_helmet");
        s[1] = reg(new ItemArmor(mat, 0, EntityEquipmentSlot.CHEST), rank + "_chestplate");
        s[2] = reg(new ItemArmor(mat, 0, EntityEquipmentSlot.LEGS), rank + "_leggings");
        s[3] = reg(new ItemArmor(mat, 0, EntityEquipmentSlot.FEET), rank + "_boots");
        return s;
    }

    private static <T extends Item> T reg(T item, String name) {
        item.setRegistryName(VoenkomatMod.MODID, name);
        item.setUnlocalizedName(VoenkomatMod.MODID + "." + name);
        item.setCreativeTab(CreativeTabs.COMBAT);
        ALL.add(item);
        return item;
    }
}
