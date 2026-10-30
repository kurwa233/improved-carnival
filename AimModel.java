package com.example.voenkomat;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Биped-модель, у которой при стрельбе руки поднимаются вперёд (как у скелета с луком). */
@SideOnly(Side.CLIENT)
public class AimModel extends ModelBiped {

    public AimModel(float size) {
        super(size);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        if (entity instanceof EntitySoldier && ((EntitySoldier) entity).isAiming()) {
            float half = (float) Math.PI / 2.0F;
            this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY;
            this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY + 0.4F;
            this.bipedRightArm.rotateAngleX = -half + this.bipedHead.rotateAngleX;
            this.bipedLeftArm.rotateAngleX = -half + this.bipedHead.rotateAngleX;
        }
    }
}
