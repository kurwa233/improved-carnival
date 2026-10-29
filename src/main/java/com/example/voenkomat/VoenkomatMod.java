package com.example.voenkomat;

import java.util.ArrayList;
import java.util.List;

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
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "soldier"),
                EntitySoldier.class, "soldier", 0, instance, 64, 3, true, 0x4B5320, 0x8B0000);

        LootTableList.register(EntitySoldier.LOOT);
        LootTableList.register(WorldGenVoenkomat.CHEST_LOOT);

        GameRegistry.registerWorldGenerator(new WorldGenVoenkomat(), 10);

        MinecraftForge.EVENT_BUS.register(new PatrolHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        List<Biome> biomes = new ArrayList<>();
        for (Biome b : Biome.REGISTRY) {
            if (BiomeDictionary.hasType(b, BiomeDictionary.Type.NETHER)
                    || BiomeDictionary.hasType(b, BiomeDictionary.Type.END)
                    || BiomeDictionary.hasType(b, BiomeDictionary.Type.OCEAN)) continue;
            if (b.getSpawnableList(EnumCreatureType.MONSTER).isEmpty()) continue;
            biomes.add(b);
        }
        EntityRegistry.addSpawn(EntitySoldier.class, 8, 1, 3, EnumCreatureType.MONSTER,
                biomes.toArray(new Biome[0]));
    }
}
