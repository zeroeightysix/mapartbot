package me.zeroeightsix.discord;

import me.zeroeightsix.discord.groups.ReplaceGroup;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by 086 on 15/01/2018.
 */
public class Main {

    static JDA jda;

    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length == 0) {
            System.err.println("Discord token required as argument");
            return;
        }

        MapArtBot bot = new MapArtBot();

        JDABuilder builder = new JDABuilder(AccountType.BOT);
        jda = builder.setAutoReconnect(true).setToken(args[0]).buildBlocking();
        File tmp = new File("tmp");
        if (!tmp.isDirectory() || !tmp.exists()) tmp.mkdirs();

        jda.addEventListener(bot);
    }

}
