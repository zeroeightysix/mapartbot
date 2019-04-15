package me.zeroeightsix.discord.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultGroups {

    public static final ReplaceGroup woolToCarpet = new ReplaceGroup("wool -> carpet", new String[][]{
            {"35", "171"},
            {"35:1", "171:1"},
            {"35:2", "171:2"},
            {"35:3", "171:3"},
            {"35:4", "171:4"},
            {"35:5", "171:5"},
            {"35:6", "171:6"},
            {"35:7", "171:7"},
            {"35:8", "171:8"},
            {"35:9", "171:9"},
            {"35:10", "171:10"},
            {"35:11", "171:11"},
            {"35:12", "171:12"},
            {"35:13", "171:13"},
            {"35:14", "171:14"},
            {"35:15", "171:15"}
    }, "woolcarpet");

    public static final ReplaceGroup sandstonePlanks = new ReplaceGroup("sandstone -> birch planks", new String[][]{{"24", "5:2"}}, "sandstoneplanks");
    public static final ReplaceGroup plankSlab = new ReplaceGroup("planks -> plank slabs", new String[][]{
            {"5", "126"},
            {"5:1", "126:1"},
            {"5:2", "126:2"},
            {"5:3", "126:3"},
            {"5:4", "126:4"},
            {"5:5", "126:5"},
    }, "planksslabs");

    public static final ReplaceGroup brewingstandBars = new ReplaceGroup("brewing stands -> iron bars", new String[][]{{"117", "101"}}, "brewingstandbars");
    public static final ReplaceGroup tntRedstone = new ReplaceGroup("tnt -> redstone block", new String[][]{{"46", "152"}}, "tntredstone");
    public static final ReplaceGroup icePacked = new ReplaceGroup("ice -> packed ice", new String[][]{{"79", "174"}}, "icepacked");
    public static final ReplaceGroup stoneCobbleslab = new ReplaceGroup("stone -> cobblestone slab", new String[][]{{"1", "44:3"}}, "stonecobbleslab");
    public static final ReplaceGroup goldpressureblock = new ReplaceGroup("golden pressure plate -> gold block", new String[][]{{"147", "41"}}, "goldpressureblock");
    public static final ReplaceGroup birchlogdiorite = new ReplaceGroup("birch log -> diorite", new String[][]{{"17:2", "1:3"}}, "birchdiorite");
    public static final ReplaceGroup prismarinediamond = new ReplaceGroup("prismarine brick -> diamond block", new String[][]{{"168:1", "57"}}, "prismarinediamond");

    public static final Collection<ReplaceGroup> defaults = new ArrayList<>();

    static {
        defaults.add(woolToCarpet);
        defaults.add(sandstonePlanks);
        defaults.add(plankSlab);
        defaults.add(brewingstandBars);
        defaults.add(tntRedstone);
        defaults.add(icePacked);
        defaults.add(stoneCobbleslab);
        defaults.add(goldpressureblock);
        defaults.add(birchlogdiorite);
        defaults.add(prismarinediamond);
    }

}
