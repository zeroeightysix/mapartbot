package me.zeroeightsix.discord;

import org.jnbt.*;

import java.io.*;
import java.util.*;

/**
 * Created by 086 on 15/01/2018.
 */
public class SchematicProcessor {

    File from;
    File to;

    private Schematic schematic;

    HashMap<String, String> replaces = new HashMap<>();

    public SchematicProcessor(File from, File to) {
        this.from = from;
        this.to = to;
    }

    public static SchematicProcessor create(File from, File to) {
        return new SchematicProcessor(from, to);
    }

    public SchematicProcessor replaces(String from, String to) {
        replaces.put(from, to);
        return this;
    }

    public SchematicProcessor load() throws IOException {
        FileInputStream fis = new FileInputStream(from);
        NBTInputStream nbt = new NBTInputStream(fis);
        CompoundTag backuptag = (CompoundTag) nbt.readTag();
        Map<String, Tag> tagCollection = backuptag.getValue();

        short width = (Short) getChildTag(tagCollection, "Width").getValue();
        short height = (Short) getChildTag(tagCollection, "Height").getValue();
        short length = (Short) getChildTag(tagCollection, "Length").getValue();

        byte[] blocks = (byte[]) getChildTag(tagCollection, "Blocks").getValue();
        byte[] data = (byte[]) getChildTag(tagCollection, "Data").getValue();

        List entities = (List) getChildTag(tagCollection, "Entities").getValue();
        List tileentities = (List) getChildTag(tagCollection, "TileEntities").getValue();

        String materials = (String) getChildTag(tagCollection, "Materials").getValue();
        nbt.close();
        fis.close();

        schematic = new Schematic(width,height,length,blocks,data,entities,tileentities,materials);

        return this;
    }

    public SchematicProcessor replace() {
        for (int i = 0; i < schematic.blocks.length; i++) {
            int from_id = schematic.blocks[i] & 0xff;
            String from = from_id + (schematic.data[i]!=0 ? (":" + (schematic.data[i]&0xff)) : "");
            String to = replaces.get(from);
            if (to != null) {
                if (to.contains(":")) {
                    int[] con = Arrays.stream(to.split(":")).mapToInt(Integer::parseInt).toArray();
                    schematic.blocks[i] = (byte) con[0];
                    schematic.data[i] = (byte) con[1];
                }else
                    schematic.blocks[i] = (byte) (Integer.parseInt(replaces.get(from)));
            }
        }
        return this;
    }

    public void save() throws IOException {
        FileOutputStream fis = new FileOutputStream(to);
        NBTOutputStream nbt = new NBTOutputStream(fis);

        HashMap<String, Tag> compound = new HashMap<>();
        compound.put("Width", new ShortTag("Width", schematic.width));
        compound.put("Height", new ShortTag("Height", schematic.height));
        compound.put("Length", new ShortTag("Length", schematic.length));

        compound.put("Blocks", new ByteArrayTag("Blocks", schematic.blocks));
        compound.put("Data", new ByteArrayTag("Data", schematic.data));

        compound.put("Entities", new ListTag("Entities", CompoundTag.class, schematic.entities));
        compound.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, schematic.tileEntities));

        compound.put("Materials", new StringTag("Materials", schematic.materials));
        nbt.writeTag(new CompoundTag("Schematic", compound));
        nbt.close();
        fis.close();
    }

    private static Tag getChildTag(Map<String, Tag> items, String key) {
        Tag tag = items.get(key);
        return tag;
    }

    public void dump(ArrayList<String> to) {
        if (schematic == null) {
            to.add("no schematic loaded");
            return;
        }

        to.add("width: " + schematic.width);
        to.add("height: " + schematic.height);
        to.add("depth: " + schematic.length);
        to.add("blocks: " + Arrays.toString(schematic.blocks));
        to.add("data: " + Arrays.toString(schematic.data));
    }

    private class Schematic {
        short width;
        short height;
        short length;
        byte[] blocks;
        byte[] data;
        List entities;
        List tileEntities;
        String materials;

        public Schematic(short width, short height, short length, byte[] blocks, byte[] data, List entities, List tileEntities, String materials) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.data = data;
            this.entities = entities;
            this.tileEntities = tileEntities;
            this.materials = materials;
        }
    }

}
