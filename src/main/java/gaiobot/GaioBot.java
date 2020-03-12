package gaiobot;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import commandhandler.CommandMap;
import gaiobot.BotListener;
import leaderboards.Leaderboard;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import settings.PrefixHandler;

public class GaioBot implements Runnable{

    private final JDA jda;
    private final CommandMap commandMap = new CommandMap(this);
    private final Scanner scanner = new Scanner(System.in);
    public static Leaderboard leaderboard;
    
    private boolean running;

    /**
     * Builds the bot
     * @throws LoginException Token is incorrect
     * @throws InterruptedException Connection to the discord was lost
     * @throws IllegalArgumentException Incorrect arguments were used
     */
	public GaioBot() throws LoginException, InterruptedException, IllegalArgumentException{
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Ref.token);
        builder.addEventListener(new BotListener(commandMap));
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.setGame(new Game("v" + Ref.version, null, GameType.DEFAULT){
            @Override
            public String getName(){
                return "v" + Ref.version;
            }

            @Override
            public String getUrl(){
                return null;
            }

            @Override
            public GameType getType(){
                return GameType.DEFAULT;
            }
        });
        jda = builder.build();
        jda.awaitReady();
        System.out.println("GaioBot has connected!");
    }
    
    /**
     * Returns the JDA
     */
    public JDA getJda() {
		return jda;
    }
    
    /**
     * Used to turn the bot off
     */
    public void setRunning(boolean running){
        this.running = running;
    }

    /**
     * Starts the bot
     */
    @Override
    public void run(){
        running = true;
        while(running) {
			if(scanner.hasNextLine())
				commandMap.commandConsole(scanner.nextLine());
		}
        
        scanner.close();
        System.out.println("GaioBOt has stopped!");
        jda.shutdown();
        System.out.println("Saving Command Privileges...");
		commandMap.save();
		System.out.println("Done!");
		System.out.println("Saving Guild Prefixes...");
		if(!PrefixHandler.close(jda))
			System.out.println("Faild to save Guild Prefixes :(");
		System.out.println("Done!");
		System.out.println("Saving Leaderboards...");
		try {
			leaderboard.close(jda);
		} catch (IOException e) {
			System.out.println("Failed to save leaderboards :(");
			e.printStackTrace();
		}
		System.out.println("Done!");
        System.exit(0);
    }

    public static void main(String[] args){
        try {
        	File file = new File("SERVER_SETTINGS");
        	if(!file.exists())
        		new File("SERVER_SETTINGS").mkdirs();
        	file = new File("LEADERBOARDS");
        	if(!file.exists())
        		new File("LEADERBOARDS").mkdirs();
        	file = new File("DISCORD_EMOJIS");
        	if(!file.exists())
        		new File("DISCORD_EMOJIS").mkdirs();
			GaioBot gaiobot = new GaioBot();
			new Thread(gaiobot, "bot").start();
			System.out.println("Creating/Loading Guild Prefixes...");
			try {
				PrefixHandler.loadPrefixes(gaiobot);
				BotListener.setPrefixes(gaiobot);
			}catch(Exception e) {
				System.out.println("Failed to create/load guild prefixes");
			}
			System.out.println("Creating/Loading Leaderboards...");
			try {
				leaderboard = new Leaderboard(gaiobot);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to load leaderboards :(");
			}
			System.out.println("Done!");
		}catch(LoginException e) {
			System.out.println("Bot failed to connect :(");
			e.printStackTrace();
		}catch(IllegalArgumentException e) {
			System.out.println("Bot failed to connect :(");
			e.printStackTrace();
		}catch(InterruptedException e) {
			System.out.println("Bot failed to connect :(");
			e.printStackTrace();
		}
    }
}