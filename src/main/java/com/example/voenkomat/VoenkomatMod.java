package com.example.voenkomat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = VoenkomatMod.MODID, name = "Voenkomat", version = "1.0.0", acceptedMinecraftVersions = "[1.12.2]")
public class VoenkomatMod {
    public static final String MODID = "voenkomat";

    @Mod.Instance(MODID)
    public static VoenkomatMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Солдаты по званиям (+ яйца призыва) и пуля
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "private"), EntityPrivate.class,
                "private", 0, instance, 64, 3, true, 0x6F7F3C, 0x2A2A2A);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "sergeant"), EntitySergeant.class,
                "sergeant", 1, instance, 64, 3, true, 0x4E6A3A, 0x8B938A);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "captain"), EntityCaptain.class,
                "captain", 2, instance, 64, 3, true, 0x35503F, 0xD4A92B);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "general"), EntityGeneral.class,
                "general", 3, instance, 80, 3, true, 0x26332B, 0xB22222);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "bullet"), EntityBullet.class,
                "bullet", 4, instance, 64, 1, true);

        LootTableList.register(EntityPrivate.LOOT);
        LootTableList.register(EntitySergeant.LOOT);
        LootTableList.register(EntityCaptain.LOOT);
        LootTableList.register(EntityGeneral.LOOT);
        LootTableList.register(WorldGenVoenkomat.CHEST_LOOT);
        LootTableList.register(WorldGenVoenkomat.ARMORY_LOOT);
        LootTableList.register(WorldGenVoenkomat.SAFE_LOOT);

        GameRegistry.registerWorldGenerator(new WorldGenVoenkomat(), 10);
        MinecraftForge.EVENT_BUS.register(new PatrolHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        List<Biome> list = new ArrayList<>();
        for (Biome b : Biome.REGISTRY) {
            if (BiomeDictionary.hasType(b, BiomeDictionary.Type.NETHER)
                    || BiomeDictionary.hasType(b, BiomeDictionary.Type.END)
                    || BiomeDictionary.hasType(b, BiomeDictionary.Type.OCEAN)) continue;
            if (b.getSpawnableList(EnumCreatureType.MONSTER).isEmpty()) continue;
            list.add(b);
        }
        Biome[] biomes = list.toArray(new Biome[0]);
        // Редкий естественный спавн; генерал в природе не появляется — только в военкомате
        addSpawn(EntityPrivate.class, 8, 1, 3, biomes);
        addSpawn(EntitySergeant.class, 4, 1, 2, biomes);
        addSpawn(EntityCaptain.class, 2, 1, 1, biomes);
    }

    private static void addSpawn(Class<? extends EntityLiving> cls, int weight, int min, int max, Biome[] biomes) {
        EntityRegistry.addSpawn(cls, weight, min, max, EnumCreatureType.MONSTER, biomes);
    }
}
