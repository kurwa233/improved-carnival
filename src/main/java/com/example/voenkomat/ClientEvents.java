package com.example.voenkomat;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = VoenkomatMod.MODID, value = Side.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntitySoldier.class, RenderSoldier::new);
    }
}
