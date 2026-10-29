package com.example.voenkomat;

import java.util.Random;

import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDoor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/** Генерирует здание ВОЕНКОМАТ (15x11, один этаж с флагом на крыше). */
public class WorldGenVoenkomat implements IWorldGenerator {

    public static final ResourceLocation CHEST_LOOT = new ResourceLocation(VoenkomatMod.MODID, "chests/voenkomat");

    /** Шанс: в среднем 1 здание на RARITY чанков. Меньше число — чаще. */
    private static final int RARITY = 120;

    private static final int W = 15; // по X
    private static final int D = 11; // по Z

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator gen, IChunkProvider provider) {
        if (world.provider.getDimension() != 0) return;
        if (random.nextInt(RARITY) != 0) return;

        // +8: здание целиком попадает в область «populate» (без каскадной генерации чанков)
        int x0 = chunkX * 16 + 8;
        int z0 = chunkZ * 16 + 8;

        int[][] pts = { {0, 0}, {W - 1, 0}, {0, D - 1}, {W - 1, D - 1}, {W / 2, D / 2} };
        int min = 999, max = -999, sum = 0;
        for (int[] p : pts) {
            BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x0 + p[0], 0, z0 + p[1]));
            if (world.getBlockState(top).getMaterial().isLiquid()) return; // вода/лава — не строим
            min = Math.min(min, top.getY());
            max = Math.max(max, top.getY());
            sum += top.getY();
        }
        if (max - min > 3) return; // слишком неровно

        int floorY = sum / pts.length - 1;
        Builder b = new Builder(world, x0, floorY, z0);
        build(b, random);
    }

    private void build(Builder b, Random rand) {
        IBlockState stone = Blocks.STONEBRICK.getDefaultState();
        IBlockState wall = Blocks.SANDSTONE.getDefaultState();
        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState planks = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);

        // 1. Расчистка объёма
        b.fill(0, 1, 0, W - 1, 10, D - 1, air);

        // 2. Фундамент + пол
        for (int dx = 0; dx < W; dx++) {
            for (int dz = 0; dz < D; dz++) {
                boolean edge = dx == 0 || dz == 0 || dx == W - 1 || dz == D - 1;
                b.set(dx, 0, dz, edge ? stone : planks);
                for (int dy = -1; dy >= -6; dy--) {
                    IBlockState s = b.get(dx, dy, dz);
                    Material m = s.getMaterial();
                    if (!m.isSolid() || m.isReplaceable() || m == Material.LEAVES) {
                        b.set(dx, dy, dz, stone);
                    } else break;
                }
            }
        }

        // 3. Стены (песчаник), угловые колонны (каменный кирпич)
        for (int dy = 1; dy <= 4; dy++) {
            for (int dx = 0; dx < W; dx++) { b.set(dx, dy, 0, wall); b.set(dx, dy, D - 1, wall); }
            for (int dz = 0; dz < D; dz++) { b.set(0, dy, dz, wall); b.set(W - 1, dy, dz, wall); }
            b.set(0, dy, 0, stone); b.set(W - 1, dy, 0, stone);
            b.set(0, dy, D - 1, stone); b.set(W - 1, dy, D - 1, stone);
        }

        // 4. Крыша
        b.fill(0, 5, 0, W - 1, 5, D - 1, stone);

        // 5. Окна
        IBlockState pane = Blocks.GLASS_PANE.getDefaultState();
        for (int x : new int[] {2, 4, 10, 12}) {
            b.fill(x, 2, 0, x, 3, 0, pane);
            b.fill(x, 2, D - 1, x, 3, D - 1, pane);
        }
        for (int z : new int[] {3, 7}) {
            b.fill(0, 2, z, 0, 3, z, pane);
            b.fill(W - 1, 2, z, W - 1, 3, z, pane);
        }

        // 6. Дверь + крыльцо
        b.fill(7, 1, 0, 7, 2, 0, air);
        ItemDoor.placeDoor(b.w, b.pos(7, 1, 0), EnumFacing.NORTH, Blocks.OAK_DOOR, false);
        b.fill(6, 0, -1, 8, 0, -1, stone);

        // 7. Вывеска «ВОЕНКОМАТ» над дверью
        b.set(7, 4, -1, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, EnumFacing.NORTH));
        TileEntity te = b.w.getTileEntity(b.pos(7, 4, -1));
        if (te instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) te;
            sign.signText[1] = new TextComponentString("ВОЕНКОМАТ");
            sign.signText[2] = new TextComponentString("Призыв здесь");
            sign.markDirty();
        }

        // 8. Интерьер: красная ковровая дорожка, стойка, столы, шкафы, лампы
        IBlockState carpet = Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.RED);
        for (int z = 1; z <= 8; z++) b.set(7, 1, z, carpet);

        for (int x = 3; x <= 11; x++) if (x != 7) b.set(x, 1, 3, planks); // стойка приёма
        b.set(2, 1, 7, Blocks.CRAFTING_TABLE.getDefaultState());
        b.set(12, 1, 7, Blocks.CRAFTING_TABLE.getDefaultState());
        b.fill(4, 1, D - 2, 5, 2, D - 2, Blocks.BOOKSHELF.getDefaultState());
        b.fill(9, 1, D - 2, 10, 2, D - 2, Blocks.BOOKSHELF.getDefaultState());

        IBlockState glow = Blocks.GLOWSTONE.getDefaultState();
        b.set(4, 4, 2, glow); b.set(10, 4, 2, glow);
        b.set(4, 4, 7, glow); b.set(10, 4, 7, glow);
        b.set(7, 4, 5, glow);

        // 9. Сундуки с лутом (в секретной комнате/архиве у задней стены)
        for (int x : new int[] {2, 7, 12}) {
            b.set(x, 1, D - 2, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH));
            TileEntity c = b.w.getTileEntity(b.pos(x, 1, D - 2));
            if (c instanceof TileEntityChest) {
                ((TileEntityChest) c).setLootTable(CHEST_LOOT, rand.nextLong());
            }
        }

        // 10. Флагшток с красным флагом (жёлтая «звезда» в центре)
        b.fill(7, 6, 2, 7, 10, 2, Blocks.OAK_FENCE.getDefaultState());
        IBlockState red = Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED);
        IBlockState yellow = Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
        b.fill(8, 8, 2, 10, 10, 2, red);
        b.set(9, 9, 2, yellow);

        // 11. Охрана внутри
        if (b.w.getDifficulty() != EnumDifficulty.PEACEFUL) {
            int[][] spots = { {4, 6}, {10, 6}, {7, 8} };
            for (int[] s : spots) {
                BlockPos p = b.pos(s[0], 1, s[1]);
                EntitySoldier guard = new EntitySoldier(b.w);
                guard.setLocationAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rand.nextFloat() * 360F, 0F);
                guard.onInitialSpawn(b.w.getDifficultyForLocation(p), null);
                guard.enablePersistence();
                guard.setHomePosAndDistance(p, 12);
                b.w.spawnEntity(guard);
            }
        }
    }

    /** Мини-помощник для размещения блоков относительно угла здания. */
    private static class Builder {
        final World w;
        final int x0, y0, z0;

        Builder(World w, int x0, int y0, int z0) { this.w = w; this.x0 = x0; this.y0 = y0; this.z0 = z0; }

        BlockPos pos(int dx, int dy, int dz) { return new BlockPos(x0 + dx, y0 + dy, z0 + dz); }

        void set(int dx, int dy, int dz, IBlockState s) { w.setBlockState(pos(dx, dy, dz), s, 2); }

        IBlockState get(int dx, int dy, int dz) { return w.getBlockState(pos(dx, dy, dz)); }

        void fill(int x1, int y1, int z1, int x2, int y2, int z2, IBlockState s) {
            for (int x = x1; x <= x2; x++)
                for (int y = y1; y <= y2; y++)
                    for (int z = z1; z <= z2; z++)
                        set(x, y, z, s);
        }
    }
}
