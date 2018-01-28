package me.zeroeightsix.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 086 on 15/01/2018.
 */
public class MapArtBot extends ListenerAdapter {

    public MapArtBot() {
        reset();
    }

    JDA jda;

    private final ReplaceGroup woolToCarpet = new ReplaceGroup("wool -> carpet", new String[][]{
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

    private final ReplaceGroup sandstonePlanks = new ReplaceGroup("sandstone -> birch planks", new String[][]{{"24", "5:2"}}, "sandstoneplanks");
    private final ReplaceGroup plankSlab = new ReplaceGroup("planks -> plank slabs", new String[][]{
            {"5", "126"},
            {"5:1", "126:1"},
            {"5:2", "126:2"},
            {"5:3", "126:3"},
            {"5:4", "126:4"},
            {"5:5", "126:5"},
    }, "planksslabs");

    private final ReplaceGroup brewingstandBars = new ReplaceGroup("brewing stands -> iron bars", new String[][]{{"117", "101"}}, "brewingstandbars");
    private final ReplaceGroup tntRedstone = new ReplaceGroup("tnt -> redstone block", new String[][]{{"46", "152"}}, "tntredstone");
    private final ReplaceGroup icePacked = new ReplaceGroup("ice -> packed ice", new String[][]{{"79", "174"}}, "icepacked");
    private final ReplaceGroup stoneCobbleslab = new ReplaceGroup("stone -> cobblestone slab", new String[][]{{"1", "44:3"}}, "stonecobbleslab");
    private final ReplaceGroup goldpressureblock = new ReplaceGroup("golden pressure plate -> gold block", new String[][]{{"147", "41"}}, "goldpressureblock");
    private final ReplaceGroup birchlogdiorite = new ReplaceGroup("birch log -> diorite", new String[][]{{"17:2", "1:3"}}, "birchdiorite");
    private final ReplaceGroup prismarinediamond = new ReplaceGroup("prismarine brick -> diamond block", new String[][]{{"168:1", "57"}}, "prismarinediamond");

    private ArrayList<ReplaceGroup> replaceMap = new ArrayList<>();

    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length == 0) {
            System.err.println("Discord token required as argument");
            return;
        }

        MapArtBot bot = new MapArtBot();

        JDABuilder builder = new JDABuilder(AccountType.BOT);
        bot.jda = builder.setAutoReconnect(true).setToken(args[0]).buildBlocking();
        File tmp = new File("tmp");
        if (!tmp.isDirectory() || !tmp.exists()) tmp.mkdirs();

        bot.jda.addEventListener(new MapArtBot());
    }

    private void reset() {
        replaceMap.clear();
        replaceMap.add(woolToCarpet);
        replaceMap.add(sandstonePlanks);
        replaceMap.add(plankSlab);
        replaceMap.add(brewingstandBars);
        replaceMap.add(tntRedstone);
        replaceMap.add(icePacked);
        replaceMap.add(stoneCobbleslab);
        replaceMap.add(goldpressureblock);
        replaceMap.add(birchlogdiorite);
        replaceMap.add(prismarinediamond);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getMember().equals(event.getGuild().getSelfMember())) return;

        String message = event.getMessage().getContentDisplay();
        if (message.startsWith("!")) {
            message = message.substring(1);
            String[] split = message.split(" |\\n");
            if (split.length > 0) {
                String response = "";
                String key = split[0].toLowerCase();
                case_:
                switch (key) {
                    case "reset":
                        reset();
                        response = "Replace map reset!";
                        break;
                    case "map":
                        if (replaceMap.isEmpty()) response += "The replace map is empty.";
                        for (ReplaceGroup group : replaceMap) {
                            response += "**(" + group.shortname + ")** " + group.description + "\n";
                        }
                        break;
                    case "open":
                        if (split.length == 1) {
                            response = "Please specify a group id to view.";
                            break;
                        }
                        ReplaceGroup group = replaceMap.stream().filter(replaceGroup -> replaceGroup.shortname.equalsIgnoreCase(split[1])).findFirst().orElse(null);
                        if (group == null) response = "No group by that id found.";
                        else
                        {
                            for (String[] strings : group.map)
                                response += strings[0] + " -> " + strings[1] + "\n";
                        }
                        break;
                    case "remove":
                        if (split.length == 1) {
                            response = "Please specify a group id to view.";
                            break;
                        }
                        group = replaceMap.stream().filter(replaceGroup -> replaceGroup.shortname.equalsIgnoreCase(split[1])).findFirst().orElse(null);
                        if (group == null) {
                            response = "No group by that id found.";
                            break;
                        }
                        replaceMap.remove(group);
                        response = "Group removed!";
                        break;
                    case "help":
                        response =
                                "This bot converts schematics for easy mapart-ready schematics without the need of mcedit, worldedit..\n" +
                                        "What blocks it replaces with which, is completely up to you. Use the commands below to update the replace map.\n" +
                                        "To convert a file, just drop it in this chat.\n\n" +
                                        "reset ~ Return the replace map to defaults\n" +
                                        "map ~ View the entries (groups) in the current replace map\n" +
                                        "open <groupid> ~ View the mappings of a group\n" +
                                        "remove <groupid> ~ Removes a group from the map\n" +
                                        "help ~ displays this message\n\n" +
                                        "add <group> ~ Add a new replacegroup. Requires specific formatting, f.e: (**Must be one message!**)" +
                                        "\n\tadd" +
                                        "\n\t1:1 -> 1:3" +
                                        "\n\t5 -> 6" +
                                        "\n\t0 -> 3:1";
                        break;
                    case "add":
                        HashMap<String, String> stringStringHashMap = new HashMap<>();
                        String[] parts = message.split("\n");
                        for (int i = 1; i < parts.length; i++) {
                            String s = parts[i];
                            s = s.trim();
                            String[] parts1 = s.split("->");
                            if (parts1.length != 2) {
                                response = s + ": Line must have **one** '->' splitting symbol";
                                break case_;
                            }
                            for (String a : parts1) {
                                a = a.replace(" ", "");
                                int c = a.length() - a.replace(":", "").length();
                                if (c > 1) {
                                    response = a + ": ID may not be composed of multiple data values";
                                    break case_;
                                }
                                if (!(a.replaceAll("\\d|:", "")).isEmpty()) {
                                    response = a + ": ID must be a numerical composition";
                                    break case_;
                                }
                            }
                            stringStringHashMap.put(parts1[0], parts1[1]);
                        }
                        if (stringStringHashMap.isEmpty()) {
                            response = "Group is empty!";
                            break;
                        }
                        final String groupName = event.getMember().getUser().getName();
                        final int[] a = new int[]{0};
                        while (replaceMap.stream().anyMatch(replaceGroup -> replaceGroup.shortname.equalsIgnoreCase(groupName + a[0]))) a[0]++;
                        ReplaceGroup group1 = new ReplaceGroup("Custom group by " + event.getMember().getUser().getName(),
                                toArray(stringStringHashMap),
                                groupName + a[0]);
                        replaceMap.add(group1);
                        response = "Group added.";
                        break;
                    default:
                        response = "Unknown command. Use help for information on this bot.";
                        break;
                }

                if (response.isEmpty()) return;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);
                builder.setDescription(response);
                event.getMessage().getChannel().sendMessage(builder.build()).queue();
            }
        }

        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty() || attachments.stream().allMatch(Message.Attachment::isImage)) return;

        Message m;
        StringBuilder content = new StringBuilder();
        ArrayList<File> files = new ArrayList<>();
        content.append("Downloading attachments");
        try {
            m = event.getTextChannel().sendMessage(generate(content.toString(), false)).complete(true);
        } catch (RateLimitedException e) {
            return;
        }

        for (Message.Attachment attachment : attachments) {
            if (attachment.isImage()) continue;
            File f = new File("tmp/" + attachment.getFileName());
            if (f.exists()) f.delete();
            boolean success = attachment.download(new File("tmp/" + attachment.getFileName()));
            content.append(System.lineSeparator());
            if (success) {
                content.append("Downloaded: ").append(attachment.getFileName());
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString().replace("*", "\\*").replace("_", "\\_"), false)).queue();
                files.add(f);
            }else{
                content.append("**Download failed: ").append(attachment.getFileName().replace("*", "\\*").replace("_", "\\_")).append("**");
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), true)).queue();
                flushTmp();
                return;
            }
        }

        content.append("\nAll attachments downloaded, processing...");

        ArrayList<File> toList = new ArrayList<>();
        for (File from : files) {
            String toN = from.getAbsolutePath();
            if (toN.endsWith(".schematic"))
                toN = toN.substring(0,toN.length() - ".schematic".length()) + ".converted.schematic";
            else
                toN += ".converted";

            File to = new File(toN);
            if (to.exists()) to.delete();
            SchematicProcessor schematicProcessor;
            try {
                schematicProcessor = SchematicProcessor.create(from, to).load();
            } catch (IOException e) {
                content.append("\nFailed (is this file even a schematic?): ").append(from.getName());
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), true)).queue();
                return;
            }

            for (ReplaceGroup group : replaceMap) {
                for (String[] s : group.map) {
                    schematicProcessor.replaces(s[0], s[1]);
                }
            }

            schematicProcessor.replace();
            content.append("\nPlacing blocks: ").append(from.getName());
            event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();
            try {
                schematicProcessor.save();
                toList.add(to);
                content.append("\nSaved: ").append(from.getName());
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }

        content.append("\nDone! Uploading..");
        event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();

        for (File file : toList)
            event.getMessage().getChannel().sendFile(file).queue();
    }

    private String[][] toArray(Map<String, String> map) {
        String[][] array = new String[map.size()][2];
        int count = 0;
        for(Map.Entry<String,String> entry : map.entrySet()){
            array[count][0] = entry.getKey();
            array[count][1] = entry.getValue();
            count++;
        }
        return array;
    }

    private MessageEmbed generate(String content, boolean fail) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(fail ? Color.RED : Color.GREEN);
        builder.setDescription(content);
        return builder.build();
    }

    private void flushTmp() {
        File file = new File("tmp");
        File[] files = file.listFiles();
        for (File f : files) f.delete();
    }

    private class ReplaceGroup {
        String description;
        String[][] map;
        String shortname;

        public ReplaceGroup(String description, String[][] map, String shortname) {
            this.description = description;
            this.map = map;
            this.shortname = shortname;
        }
    }

}
