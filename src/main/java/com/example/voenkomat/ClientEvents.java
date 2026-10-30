package com.example.voenkomat;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = VoenkomatMod.MODID, value = Side.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityPrivate.class, RenderSoldier::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySergeant.class, RenderSoldier::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCaptain.class, RenderSoldier::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGeneral.class, RenderSoldier::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, RenderBullet::new);

        for (Item item : ModItems.ALL) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
