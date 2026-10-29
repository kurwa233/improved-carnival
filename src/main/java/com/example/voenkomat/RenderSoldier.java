package com.example.voenkomat;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSoldier extends RenderBiped<EntitySoldier> {

    // Можно заменить своей текстурой: assets/voenkomat/textures/entity/soldier.png (64x64)
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/steve.png");

    public RenderSoldier(RenderManager manager) {
        super(manager, new ModelBiped(), 0.5F);
        this.addLayer(new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                this.modelLeggings = new ModelBiped(0.5F);
                this.modelArmor = new ModelBiped(1.0F);
            }
        });
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySoldier entity) {
        return TEXTURE;
    }
}
