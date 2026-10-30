package com.example.voenkomat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Рендер солдат: у каждого звания свой скин с лицом (textures/entity/<звание>.png, 64x32). */
@SideOnly(Side.CLIENT)
public class RenderSoldier extends RenderBiped<EntitySoldier> {

    private static final Map<String, ResourceLocation> SKINS = new HashMap<>();

    public RenderSoldier(RenderManager manager) {
        super(manager, new AimModel(0.0F), 0.5F);
        this.addLayer(new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                this.modelLeggings = new AimModel(0.5F);
                this.modelArmor = new AimModel(1.0F);
            }
        });
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySoldier entity) {
        String name = entity.getSkinName();
        ResourceLocation rl = SKINS.get(name);
        if (rl == null) {
            rl = new ResourceLocation(VoenkomatMod.MODID, "textures/entity/" + name + ".png");
            SKINS.put(name, rl);
        }
        return rl;
    }

    @Override
    protected void preRenderCallback(EntitySoldier entity, float partialTicks) {
        float s = entity.getModelScale();
        GlStateManager.scale(s, s, s);
    }
}
