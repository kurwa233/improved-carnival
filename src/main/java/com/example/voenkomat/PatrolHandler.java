package com.example.voenkomat;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Раз в INTERVAL тиков рядом с игроком появляется патруль: рядовые, сержанты, иногда капитан. */
public class PatrolHandler {

    /** 6000 тиков = 5 минут. */
    private static final int INTERVAL = 6000;
    private static final int MAX_NEARBY = 8;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent e) {
        World w = e.world;
        if (e.phase != TickEvent.Phase.END || w.isRemote || w.provider.getDimension() != 0) return;
        if (w.getDifficulty() == EnumDifficulty.PEACEFUL) return;
        if (w.getTotalWorldTime() % INTERVAL != 0) return;

        Random r = w.rand;
        for (EntityPlayer p : new ArrayList<>(w.playerEntities)) {
            if (p.isSpectator() || p.isCreative()) continue;
            AxisAlignedBB box = p.getEntityBoundingBox().grow(64);
            if (w.getEntitiesWithinAABB(EntitySoldier.class, box).size() >= MAX_NEARBY) continue;

            int count = 2 + r.nextInt(3);
            boolean spawned = false;
            for (int i = 0; i < count; i++) {
                double ang = r.nextDouble() * Math.PI * 2;
                double dist = 22 + r.nextInt(12);
                int x = MathHelper.floor(p.posX + Math.cos(ang) * dist);
                int z = MathHelper.floor(p.posZ + Math.sin(ang) * dist);
                BlockPos pos = new BlockPos(x, 0, z);
                if (!w.isBlockLoaded(pos)) continue;
                pos = w.getTopSolidOrLiquidBlock(pos);
                if (w.getBlockState(pos).getMaterial().isLiquid()
                        || w.getBlockState(pos.down()).getMaterial().isLiquid()) continue;

                EntitySoldier s = createRandomSoldier(w, r, i == 0 && r.nextFloat() < 0.35F);
                s.setLocationAndAngles(x + 0.5, pos.getY(), z + 0.5, r.nextFloat() * 360F, 0F);
                s.onInitialSpawn(w.getDifficultyForLocation(pos), null);
                w.spawnEntity(s);
                spawned = true;
            }
            if (spawned) {
                p.sendStatusMessage(new TextComponentString(
                        TextFormatting.RED + "Вам пришла повестка! Военные уже в пути..."), true);
            }
        }
    }

    private static EntitySoldier createRandomSoldier(World w, Random r, boolean leader) {
        if (leader) return new EntityCaptain(w);
        float f = r.nextFloat();
        if (f < 0.6F) return new EntityPrivate(w);
        return new EntitySergeant(w);
    }
}
