package com.example.voenkomat;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Пуля невидима — след рисуют частицы дыма. */
@SideOnly(Side.CLIENT)
public class RenderBullet extends Render<EntityBullet> {

    public RenderBullet(RenderManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBullet entity) {
        return null;
    }
}
