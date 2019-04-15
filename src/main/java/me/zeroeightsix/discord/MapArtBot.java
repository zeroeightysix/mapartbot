package me.zeroeightsix.discord;

import me.zeroeightsix.discord.groups.DefaultGroups;
import me.zeroeightsix.discord.groups.ReplaceGroup;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapArtBot extends ListenerAdapter {

    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(16);
    private static final HashMap<Member, Consumer<MessageReceivedEvent>> responseMap = new HashMap<>();
    private ArrayList<ReplaceGroup> replaceMap = new ArrayList<>();

    MapArtBot() {
        new Thread(() -> {
            while (true) {
                try {
                    queue.take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }, "Job thread").start();
    }

    private void reset() {
        replaceMap.clear();
        replaceMap.addAll(DefaultGroups.defaults);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getMember().equals(event.getGuild().getSelfMember())) return;

        if (responseMap.containsKey(event.getMember())) {
            responseMap.remove(event.getMember()).accept(event);
        }

        String message = event.getMessage().getContentDisplay();
        if (processCommand(event, message)) return;

        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty()) return;
        processFiles(event, attachments);
    }

    private void processFiles(MessageReceivedEvent event, List<Message.Attachment> attachments) {
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
            File f = new File("tmp/" + attachment.getFileName());
            if (f.exists()) f.delete();
            boolean success = attachment.download(new File("tmp/" + attachment.getFileName()));

            content.append(System.lineSeparator());

            if (success) {
                content.append("Downloaded: ").append(attachment.getFileName());
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString().replace("*", "\\*").replace("_", "\\_"), false)).queue();
            } else {
                content.append("**Download failed: ").append(attachment.getFileName().replace("*", "\\*").replace("_", "\\_")).append("**");
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), true)).queue();
                return;
            }

            if (attachment.isImage()) {
                processImage(event, m, content, f);
                continue;
            } else {
                files.add(f);
            }
        }

        content.append("\nAll attachments downloaded, processing and uploading. Please wait.");

        processSchematics(event, m, content, files);
    }

    private void processSchematics(MessageReceivedEvent event, Message m, StringBuilder content, List<File> files) {
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
            event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();
            try {
                schematicProcessor.save();
                toList.add(to);
                event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }

        event.getMessage().getChannel().editMessageById(m.getId(), generate(content.toString(), false)).queue();

        for (File file : toList) {
            event.getMessage().getChannel().sendFile(file).queue();
            file.delete();
        }
    }

    private void processImage(MessageReceivedEvent event, Message m, StringBuilder builder, File file) {
        queue.add(() -> {
            try {
                Class converter = Class. forName("MapConverter");
                converter.getDeclaredMethod("main", String[].class).invoke(null, new Object[] { new String[] { file.getAbsolutePath() } });

                Message preview = event.getChannel().sendFile(new File("tmp/out/png/completeImage.png")).complete(true);

                Files.list(Paths.get("tmp/out/png")).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                List<File> files = Files.list(Paths.get("tmp/out/schematic")).map(Path::toFile).collect(Collectors.toList());
                if (files.size() > 9) {
                    builder.append("\nYour image is quite large and generated **" + files.size() + "** schematics.\nWould you like to proceed? **(yes/no)**");
                    event.getMessage().getChannel().editMessageById(m.getId(), generate(builder.toString(), true)).queue();

                    responseMap.put(event.getMember(), messageReceivedEvent -> {
                        String s = messageReceivedEvent.getMessage().getContentDisplay();
                        if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y")) {
                            processSchematics(event, m, builder, files);
                        } else {
                            files.forEach(File::delete);
                            m.delete().queue();
                            preview.delete().queue();
                            messageReceivedEvent.getChannel().sendMessage(generate("Cancelled.", true)).queue();
                        }
                    });
                } else {
                    processSchematics(event, m, builder, files);
                    files.forEach(File::delete);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException | RateLimitedException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean processCommand(MessageReceivedEvent event, String message) {
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
                            response += "**(" + group.shortName + ")** " + group.description + "\n";
                        }
                        break;
                    case "open":
                        if (split.length == 1) {
                            response = "Please specify a group id to view.";
                            break;
                        }
                        ReplaceGroup group = replaceMap.stream().filter(replaceGroup -> replaceGroup.shortName.equalsIgnoreCase(split[1])).findFirst().orElse(null);
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
                        group = replaceMap.stream().filter(replaceGroup -> replaceGroup.shortName.equalsIgnoreCase(split[1])).findFirst().orElse(null);
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
                        while (replaceMap.stream().anyMatch(replaceGroup -> replaceGroup.shortName.equalsIgnoreCase(groupName + a[0]))) a[0]++;
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

                if (response.isEmpty()) return true;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);
                builder.setDescription(response);
                event.getMessage().getChannel().sendMessage(builder.build()).queue();
            }
        }
        return false;
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

}
