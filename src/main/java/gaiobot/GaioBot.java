package gaiobot;

import java.util.Scanner;

import javax.security.auth.login.LoginException;

import commandhandler.CommandMap;
import gaiobot.BotListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;

public class GaioBot implements Runnable{

    private final JDA jda;
    private final CommandMap commandMap = new CommandMap(this);
    private final Scanner scanner = new Scanner(System.in);
    
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
        jda = builder.buildBlocking();
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
        System.exit(0);
    }

    public static void main(String[] args){
        try {
			GaioBot gaiobot = new GaioBot();
			new Thread(gaiobot, "bot").start();
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