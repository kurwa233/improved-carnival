package com.example.voenkomat;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
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

/**
 * Военный комиссариат: 3 этажа + крыша, двор с забором и КПП, флагштоки, вестибюль со стойкой,
 * оружейная, офисы, казарма и кабинет генерала с боссом.
 * Постройка помещается ровно в область 16x16, поэтому не вызывает «каскадной» генерации чанков.
 */
public class WorldGenVoenkomat implements IWorldGenerator {

    public static final ResourceLocation CHEST_LOOT = new ResourceLocation(VoenkomatMod.MODID, "chests/voenkomat");
    public static final ResourceLocation ARMORY_LOOT = new ResourceLocation(VoenkomatMod.MODID, "chests/armory");
    public static final ResourceLocation SAFE_LOOT = new ResourceLocation(VoenkomatMod.MODID, "chests/safe");

    /** В среднем одно здание на RARITY чанков (для теста поставьте 3). */
    private static final int RARITY = 100;

    // ---- блоки
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState STONE = Blocks.STONEBRICK.getDefaultState();
    private static final IBlockState SAND = Blocks.SANDSTONE.getDefaultState()
            .withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH);
    private static final IBlockState PLANKS = Blocks.PLANKS.getDefaultState()
            .withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState GLASS = Blocks.GLASS_PANE.getDefaultState();
    private static final IBlockState BARS = Blocks.IRON_BARS.getDefaultState();
    private static final IBlockState GLOW = Blocks.GLOWSTONE.getDefaultState();
    private static final IBlockState WALLC = Blocks.COBBLESTONE_WALL.getDefaultState();
    private static final IBlockState FENCE = Blocks.OAK_FENCE.getDefaultState();
    private static final IBlockState BOOKS = Blocks.BOOKSHELF.getDefaultState();
    private static final IBlockState TABLE = Blocks.CRAFTING_TABLE.getDefaultState();
    private static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState();
    private static final IBlockState GOLD = Blocks.GOLD_BLOCK.getDefaultState();

    private static IBlockState carpet(EnumDyeColor c) {
        return Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, c);
    }

    private static IBlockState wool(EnumDyeColor c) {
        return Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, c);
    }

    private static IBlockState stairs(Block block, EnumFacing facing) {
        return block.getDefaultState().withProperty(BlockStairs.FACING, facing)
                .withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator gen, IChunkProvider provider) {
        if (world.provider.getDimension() != 0) return;
        if (random.nextInt(RARITY) != 0) return;

        // +8: вся площадка 16x16 лежит в области «populate» этого чанка
        int x0 = chunkX * 16 + 8;
        int z0 = chunkZ * 16 + 8;

        int[][] pts = { {0, 0}, {15, 0}, {0, 15}, {15, 15}, {8, 8}, {8, 0}, {0, 8}, {15, 8}, {8, 15} };
        int min = 999, max = -999, sum = 0;
        for (int[] p : pts) {
            BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x0 + p[0], 0, z0 + p[1]));
            if (world.getBlockState(top).getMaterial().isLiquid()) return; // вода — не строим
            min = Math.min(min, top.getY());
            max = Math.max(max, top.getY());
            sum += top.getY();
        }
        if (max - min > 4) return; // слишком неровно

        int floorY = sum / pts.length - 1;
        if (floorY < 40 || floorY > 200) return;

        build(new Builder(world, x0, floorY, z0), random);
    }

    // ======================================================================== постройка
    private void build(Builder b, Random rand) {
        clearAndFoundation(b);
        shell(b);
        windows(b);
        entrance(b);
        yard(b);
        interiorWalls(b);
        stairwell(b);
        lamps(b);
        floor1(b, rand);
        floor2(b, rand);
        floor3(b, rand);
        roof(b);
        spawnGarrison(b, rand);
    }

    private void clearAndFoundation(Builder b) {
        b.fill(0, 1, 0, 15, 24, 15, AIR);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean inside = x >= 1 && x <= 14 && z >= 3 && z <= 14;
                IBlockState top;
                if (!inside) top = GRASS;
                else if (x == 1 || x == 14 || z == 3 || z == 14) top = STONE;
                else top = PLANKS;
                b.set(x, 0, z, top);
                for (int dy = -1; dy >= -8; dy--) {
                    Material m = b.get(x, dy, z).getMaterial();
                    if (!m.isSolid() || m.isReplaceable() || m == Material.LEAVES) {
                        b.set(x, dy, z, inside ? STONE : DIRT);
                    } else {
                        break;
                    }
                }
            }
        }
        b.fill(7, 0, 0, 8, 0, 2, STONE); // дорожка от КПП
    }

    private void ring(Builder b, int y, IBlockState s) {
        for (int x = 1; x <= 14; x++) { b.set(x, y, 3, s); b.set(x, y, 14, s); }
        for (int z = 3; z <= 14; z++) { b.set(1, y, z, s); b.set(14, y, z, s); }
    }

    private void shell(Builder b) {
        for (int y = 1; y <= 14; y++) ring(b, y, SAND);
        // угловые колонны
        int[][] corners = { {1, 3}, {14, 3}, {1, 14}, {14, 14} };
        for (int[] c : corners) b.fill(c[0], 0, c[1], c[0], 15, c[1], STONE);
        // пояса между этажами и перекрытия
        ring(b, 5, STONE);
        ring(b, 10, STONE);
        b.fill(2, 5, 4, 13, 5, 13, PLANKS);
        b.fill(2, 10, 4, 13, 10, 13, PLANKS);
        // крыша и парапет
        b.fill(1, 15, 3, 14, 15, 14, STONE);
        ring(b, 16, WALLC);
    }

    private void windows(Builder b) {
        int[] front = {3, 5, 10, 12};
        int[] back = {3, 6, 9, 12};
        int[] side = {6, 9, 12};
        for (int f = 0; f < 3; f++) {
            for (int dy = 2; dy <= 3; dy++) {
                int y = 5 * f + dy;
                for (int x : front) b.set(x, y, 3, GLASS);
                for (int x : back) b.set(x, y, 14, GLASS);
                for (int z : side) { b.set(1, y, z, GLASS); b.set(14, y, z, GLASS); }
            }
        }
    }

    private void entrance(Builder b) {
        // проём и двойная дверь
        b.fill(7, 1, 3, 8, 2, 3, AIR);
        ItemDoor.placeDoor(b.w, b.pos(7, 1, 3), EnumFacing.NORTH, Blocks.OAK_DOOR, false);
        ItemDoor.placeDoor(b.w, b.pos(8, 1, 3), EnumFacing.NORTH, Blocks.OAK_DOOR, true);
        b.fill(7, 3, 3, 8, 3, 3, GLASS); // фрамуга

        // портик: колонны, перекрытие и ограждение
        b.fill(5, 1, 1, 5, 4, 1, STONE);
        b.fill(10, 1, 1, 10, 4, 1, STONE);
        b.fill(5, 5, 1, 10, 5, 2, STONE);
        b.fill(5, 6, 1, 10, 6, 1, WALLC);
        b.set(5, 6, 2, WALLC);
        b.set(10, 6, 2, WALLC);

        // вывеска
        b.set(7, 4, 2, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, EnumFacing.NORTH));
        sign(b, 7, 4, 2, "", "ВОЕННЫЙ", "КОМИССАРИАТ", "");
        b.set(8, 4, 2, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, EnumFacing.NORTH));
        sign(b, 8, 4, 2, "", "ВОЕНКОМАТ", "Призывной пункт", "");
    }

    private void sign(Builder b, int x, int y, int z, String l0, String l1, String l2, String l3) {
        TileEntity te = b.w.getTileEntity(b.pos(x, y, z));
        if (te instanceof TileEntitySign) {
            TileEntitySign s = (TileEntitySign) te;
            s.signText[0] = new TextComponentString(l0);
            s.signText[1] = new TextComponentString(l1);
            s.signText[2] = new TextComponentString(l2);
            s.signText[3] = new TextComponentString(l3);
            s.markDirty();
        }
    }

    private void yard(Builder b) {
        // забор из решёток
        for (int i = 0; i < 16; i++) {
            if (i != 7 && i != 8) b.fill(i, 1, 0, i, 2, 0, BARS);
            b.fill(i, 1, 15, i, 2, 15, BARS);
            b.fill(0, 1, i, 0, 2, i, BARS);
            b.fill(15, 1, i, 15, 2, i, BARS);
        }
        // КПП: столбы с фонарями
        int[][] posts = { {6, 0}, {9, 0}, {0, 0}, {15, 0}, {0, 15}, {15, 15} };
        for (int[] p : posts) {
            b.fill(p[0], 1, p[1], p[0], 3, p[1], STONE);
            b.set(p[0], 4, p[1], GLOW);
        }
        // флагшток слева: триколор
        b.fill(2, 1, 1, 2, 9, 1, FENCE);
        b.fill(3, 9, 1, 4, 9, 1, wool(EnumDyeColor.WHITE));
        b.fill(3, 8, 1, 4, 8, 1, wool(EnumDyeColor.BLUE));
        b.fill(3, 7, 1, 4, 7, 1, wool(EnumDyeColor.RED));
        // флагшток справа: красный стяг со звездой
        b.fill(13, 1, 1, 13, 9, 1, FENCE);
        b.fill(11, 7, 1, 12, 9, 1, wool(EnumDyeColor.RED));
        b.set(12, 8, 1, wool(EnumDyeColor.YELLOW));
    }

    private void interiorWalls(Builder b) {
        for (int base = 0; base <= 10; base += 5) {
            b.fill(11, base + 1, 4, 11, base + 4, 13, SAND); // стена лестничной клетки
        }
        b.fill(11, 1, 5, 11, 2, 5, AIR);     // 1 этаж: проход из вестибюля
        b.fill(11, 6, 13, 11, 7, 13, AIR);   // 2 этаж: выход с лестницы
        b.fill(11, 11, 6, 11, 12, 6, AIR);   // 3 этаж: вход в кабинет генерала

        b.fill(2, 1, 9, 10, 4, 9, SAND);     // стена оружейной
        b.fill(6, 1, 9, 6, 2, 9, AIR);
        ItemDoor.placeDoor(b.w, b.pos(6, 1, 9), EnumFacing.SOUTH, Blocks.IRON_DOOR, false);

        b.fill(2, 6, 9, 10, 9, 9, SAND);     // стена казармы (2 этаж)
        b.fill(6, 6, 9, 6, 7, 9, AIR);
    }

    private void stairwell(Builder b) {
        IBlockState up = stairs(Blocks.STONE_BRICK_STAIRS, EnumFacing.SOUTH);
        IBlockState down = stairs(Blocks.STONE_BRICK_STAIRS, EnumFacing.NORTH);
        b.fill(12, 5, 9, 12, 5, 12, AIR);    // проём в перекрытии над первым маршем
        b.fill(13, 10, 9, 13, 10, 11, AIR);  // проём над вторым маршем
        for (int i = 0; i < 5; i++) {
            b.set(12, 1 + i, 9 + i, up);     // 1-й марш: вверх на юг, к посадочной (13, 6, 13)
            b.set(13, 6 + i, 12 - i, down);  // 2-й марш: вверх на север
        }
    }

    private void lamps(Builder b) {
        int[][] spots = { {4, 5}, {9, 5}, {4, 8}, {9, 8}, {4, 12}, {8, 12}, {12, 6}, {12, 11} };
        for (int base = 0; base <= 10; base += 5) {
            for (int[] s : spots) b.set(s[0], base + 4, s[1], GLOW);
        }
    }

    // ---- 1 этаж: вестибюль, стойка, служебные помещения, оружейная
    private void floor1(Builder b, Random rand) {
        IBlockState red = carpet(EnumDyeColor.RED);
        b.fill(7, 1, 4, 8, 1, 8, red);
        // стойка и окошки
        for (int x = 3; x <= 10; x++) {
            if (x == 7 || x == 8) continue;
            b.set(x, 1, 6, PLANKS);
        }
        b.fill(3, 2, 6, 6, 3, 6, GLASS);
        b.fill(9, 2, 6, 10, 3, 6, GLASS);
        // скамейки ожидания
        IBlockState bench = stairs(Blocks.SPRUCE_STAIRS, EnumFacing.SOUTH);
        b.fill(2, 1, 4, 5, 1, 4, bench);
        b.fill(9, 1, 4, 10, 1, 4, bench);
        // служебная зона
        b.set(3, 1, 8, TABLE);
        b.set(5, 1, 8, TABLE);
        b.set(9, 1, 8, TABLE);
        b.fill(2, 1, 7, 2, 2, 8, BOOKS);
        chest(b, 10, 1, 7, EnumFacing.WEST, CHEST_LOOT, rand);
        // оружейная
        chest(b, 3, 1, 13, EnumFacing.NORTH, ARMORY_LOOT, rand);
        chest(b, 5, 1, 13, EnumFacing.NORTH, ARMORY_LOOT, rand);
        chest(b, 7, 1, 13, EnumFacing.NORTH, ARMORY_LOOT, rand);
        chest(b, 9, 1, 13, EnumFacing.NORTH, ARMORY_LOOT, rand);
        chest(b, 2, 1, 10, EnumFacing.EAST, CHEST_LOOT, rand);
        b.fill(4, 1, 11, 4, 2, 12, BARS);    // стойки для оружия
        b.fill(8, 1, 11, 8, 2, 12, BARS);
    }

    // ---- 2 этаж: кабинеты и казарма
    private void floor2(Builder b, Random rand) {
        b.fill(2, 6, 4, 10, 6, 8, carpet(EnumDyeColor.GREEN));
        b.set(3, 6, 6, TABLE);
        b.set(9, 6, 6, TABLE);
        b.set(3, 6, 7, stairs(Blocks.SPRUCE_STAIRS, EnumFacing.NORTH));
        b.set(9, 6, 7, stairs(Blocks.SPRUCE_STAIRS, EnumFacing.NORTH));
        b.fill(2, 6, 8, 4, 7, 8, BOOKS);
        b.fill(8, 6, 8, 10, 7, 8, BOOKS);
        chest(b, 6, 6, 4, EnumFacing.SOUTH, CHEST_LOOT, rand);
        // казарма
        for (int x = 2; x <= 8; x += 2) bed(b, x, 6, 13);
        chest(b, 10, 6, 13, EnumFacing.WEST, CHEST_LOOT, rand);
        b.fill(10, 6, 11, 10, 7, 12, BOOKS);
    }

    // ---- 3 этаж: кабинет генерала
    private void floor3(Builder b, Random rand) {
        b.fill(2, 11, 4, 10, 11, 13, carpet(EnumDyeColor.RED));
        b.fill(4, 11, 7, 8, 11, 7, PLANKS);                      // стол
        b.set(6, 11, 8, stairs(Blocks.SPRUCE_STAIRS, EnumFacing.NORTH)); // кресло
        b.set(6, 11, 5, TABLE);                                  // карта
        b.set(3, 11, 5, GOLD);
        b.set(9, 11, 5, GOLD);
        b.fill(2, 11, 13, 10, 12, 13, BOOKS);                    // библиотека
        chest(b, 2, 11, 12, EnumFacing.EAST, SAFE_LOOT, rand);
        chest(b, 10, 11, 12, EnumFacing.WEST, SAFE_LOOT, rand);
        b.fill(2, 12, 5, 2, 13, 7, wool(EnumDyeColor.RED));      // знамя
        b.set(2, 12, 6, wool(EnumDyeColor.YELLOW));
        b.fill(10, 12, 5, 10, 13, 7, wool(EnumDyeColor.RED));
        b.set(10, 12, 6, wool(EnumDyeColor.YELLOW));
    }

    private void roof(Builder b) {
        // антенна
        b.fill(12, 16, 12, 12, 22, 12, BARS);
        b.set(12, 23, 12, GLOW);
        // флаг на крыше
        b.fill(7, 16, 8, 7, 21, 8, FENCE);
        b.fill(8, 19, 8, 10, 21, 8, wool(EnumDyeColor.RED));
        b.set(9, 20, 8, wool(EnumDyeColor.YELLOW));
    }

    private void spawnGarrison(Builder b, Random rand) {
        if (b.w.getDifficulty() == EnumDifficulty.PEACEFUL) return;
        World w = b.w;
        spawn(b, new EntityGeneral(w), 6, 11, 9, rand);
        spawn(b, new EntityCaptain(w), 4, 6, 5, rand);
        spawn(b, new EntityCaptain(w), 8, 6, 5, rand);
        spawn(b, new EntitySergeant(w), 4, 1, 8, rand);
        spawn(b, new EntitySergeant(w), 9, 1, 7, rand);
        spawn(b, new EntitySergeant(w), 4, 1, 1, rand);
        spawn(b, new EntitySergeant(w), 11, 1, 1, rand);
        spawn(b, new EntityPrivate(w), 6, 1, 1, rand);
        spawn(b, new EntityPrivate(w), 9, 1, 1, rand);
        spawn(b, new EntityPrivate(w), 6, 1, 12, rand);
        spawn(b, new EntityPrivate(w), 3, 1, 11, rand);
        spawn(b, new EntityPrivate(w), 3, 6, 11, rand);
        spawn(b, new EntityPrivate(w), 7, 6, 11, rand);
    }

    private void spawn(Builder b, EntitySoldier e, int dx, int dy, int dz, Random rand) {
        BlockPos p = b.pos(dx, dy, dz);
        e.setLocationAndAngles(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, rand.nextFloat() * 360F, 0F);
        e.onInitialSpawn(b.w.getDifficultyForLocation(p), null);
        e.enablePersistence();
        e.setHomePosAndDistance(p, 10);
        b.w.spawnEntity(e);
    }

    private void chest(Builder b, int x, int y, int z, EnumFacing facing, ResourceLocation loot, Random rand) {
        b.set(x, y, z, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, facing));
        TileEntity te = b.w.getTileEntity(b.pos(x, y, z));
        if (te instanceof TileEntityChest) {
            ((TileEntityChest) te).setLootTable(loot, rand.nextLong());
        }
    }

    private void bed(Builder b, int x, int y, int footZ) {
        IBlockState foot = Blocks.BED.getDefaultState().withProperty(BlockBed.FACING, EnumFacing.NORTH)
                .withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);
        IBlockState head = Blocks.BED.getDefaultState().withProperty(BlockBed.FACING, EnumFacing.NORTH)
                .withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
        b.set(x, y, footZ, foot);
        b.set(x, y, footZ - 1, head);
    }

    /** Размещение блоков относительно угла площадки (dx, dy, dz). dy = 0 — уровень пола 1 этажа. */
    private static class Builder {
        final World w;
        final int x0, y0, z0;

        Builder(World w, int x0, int y0, int z0) {
            this.w = w;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
        }

        BlockPos pos(int dx, int dy, int dz) {
            return new BlockPos(x0 + dx, y0 + dy, z0 + dz);
        }

        void set(int dx, int dy, int dz, IBlockState s) {
            w.setBlockState(pos(dx, dy, dz), s, 2);
        }

        IBlockState get(int dx, int dy, int dz) {
            return w.getBlockState(pos(dx, dy, dz));
        }

        void fill(int x1, int y1, int z1, int x2, int y2, int z2, IBlockState s) {
            for (int x = x1; x <= x2; x++)
                for (int y = y1; y <= y2; y++)
                    for (int z = z1; z <= z2; z++)
                        set(x, y, z, s);
        }
    }
}
