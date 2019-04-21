package commandhandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import commandhandler.Command.ExecutorType;
import discordcommands.BasicCommands;
import discordcommands.HelpCommand;
import events.Vote;
import gaiobot.GaioBot;
import music.MusicCommands;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import utilities.FileReading;
import utilities.FileWriting;

public class CommandMap {
	
	private final GaioBot gaioBot;
	private final static Map<Long, Integer> powers = new HashMap<>();
	private final Map<String, SimpleCommand> commands = new HashMap<>();
	
	/**
	 * Registers all commands with the bot
	 */
	public CommandMap(GaioBot gaioBot) {
		this.gaioBot = gaioBot;
		registerCommands(new BasicCommands(gaioBot, this), new HelpCommand(this), new MusicCommands(), new Vote());
		System.out.println("Loading Command Map Admins...");
		System.out.println("Done!");
	}
	
	/**
	 * Returns the SimpleCommand based off of the name of the command given
	 */
	public SimpleCommand getSimCom(String command) {
		return commands.get(command);
	}
	
	/**
	 * Adds or changes the specified user and administrative power to the map
	 */
	public static void addUserPower(User user, int power) {
		if(power == 0)
			removeUserPower(user);
		else
			powers.put(user.getIdLong(), power);
	}
	
	/**
	 * Removes the specified user from the map completely
	 */
	public static void removeUserPower(User user) {
		powers.remove(user.getIdLong());
	}
	
	/**
	 * 
	 */
	private void load() {
		File file = new File("SERVER_SETTINGS/userAdmins.json");
		if (!file.exists())
			return;
		
		try {
			FileReading reader = new FileReading(file);
			JSONArray array = reader.toJSONArray();
			
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				powers.put(object.getLong("id"), object.getInt("power"));
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void save() {
		JSONArray array = new JSONArray();
		
		for(Entry<Long, Integer> power : powers.entrySet()) {
			JSONObject object = new JSONObject();
			object.accumulate("id", power.getKey());
			object.accumulate("power", power.getValue());
			array.put(object);
		}
		
		try(FileWriting writer = new FileWriting("SERVER_SETTINGS/userAdmins.json")) {
			writer.write(array);
			writer.flush();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Returns the administrative power level tied to the specified user
	 */
	public static int getPowerUser(Guild guild, User user) {
		System.out.println("Got power");
		if(guild.getOwner().getUser().equals(user))
			return 4;
		return powers.containsKey(user.getIdLong()) ? powers.get(user.getIdLong()) : 0;
	}
	
	/**
	 * Returns the list of commands stored in the map
	 */
	public Collection<SimpleCommand> getCommands() {
		return commands.values();
	}
	
	/**
	 * Takes an unknowing amount of Object arguments (commands) and registers them
	 */
	public void registerCommands(Object...objects) {
		for(Object object : objects)
			registerCommand(object);
	}
	
	/**
	 * Creates and registers the command object for use later
	 */
	public void registerCommand(Object object) {
		for(Method method : object.getClass().getDeclaredMethods()) {
			if(method.isAnnotationPresent(Command.class)) {
				Command command = method.getAnnotation(Command.class);
				method.setAccessible(true);
				SimpleCommand simpleCommand = new SimpleCommand(command.name(), command.description(), command.type(), object, method, command.power());
				commands.put(command.name(),  simpleCommand);
			}
		}
	}
	
	/**
	 * Executes commands meant for the console
	 */
	public void commandConsole(String command) {
		Object[] object = getCommand(command);
		if(object[0] == null || ((SimpleCommand)object[0]).getExecutorType() == ExecutorType.USER) {
			System.out.println("Unkown Command");
			return;
		}
		try {
			execute(((SimpleCommand)object[0]), command, (String[])object[1], null);
		}
		catch(Exception e){
			System.out.println("The method \"" + ((SimpleCommand)object[0]).getMethod().getName() + "\" is not correctly initialized!");
		}
	}
	
	/**
	 * Returns true or false depending on whether the user has the ability to use the command
	 */
	public boolean commandUser(User user, String command, Message message) {
		Object[] object = getCommand(command);
		if(object[0] == null || ((SimpleCommand)object[0]).getExecutorType() == ExecutorType.CONSOLE)
			return false;
		if(message.getGuild() != null && ((SimpleCommand)object[0]).getPower() > getPowerUser(message.getGuild(), message.getAuthor()))
			return false;
		try {
			execute(((SimpleCommand)object[0]), command, (String[])object[1], message);
		}catch(Exception e) {
			System.out.println("The method \"" + ((SimpleCommand)object[0]).getMethod().getName() + "\" is not correctly initialized!");
		}
		return true;
	}
	
	/**
	 * Gets the registered command from the map
	 */
	private Object[] getCommand(String command) {
		String [] commandSplit = command.split(" ");
		String[] args = new String[commandSplit.length-1];
		for(int i = 1; i < commandSplit.length; i++)
			args[i-1] = commandSplit[i];
		SimpleCommand simpleCommand = commands.get(commandSplit[0]);
		return new Object[] {simpleCommand, args};
	}
	
	/**
	 * 
	 */
	private void execute(SimpleCommand simpleCommand, String command, String[] args, Message message) throws Exception{
		Parameter[] parameters = simpleCommand.getMethod().getParameters();
		Object[] objects = new Object[parameters.length];
		for(int i = 0; i < parameters.length; i++) {
			if(parameters[i].getType() == String[].class)
				objects[i] = args;
			else if(parameters[i].getType() == User.class)
				objects[i] = message == null ? null : message.getAuthor();
			else if (parameters[i].getType() == TextChannel.class)
				objects[i] = message == null ? null : message.getTextChannel();
			else if (parameters[i].getType() == PrivateChannel.class)
				objects[i] = message == null ? null : message.getPrivateChannel();
			else if (parameters[i].getType() == Guild.class)
				objects[i] = message == null ? null : message.getGuild();
			else if (parameters[i].getType() == String.class)
				objects[i] = command;
			else if (parameters[i].getType() == Message.class)
				objects[i] = message;
			else if (parameters[i].getType() == JDA.class)
				objects[i] = gaioBot.getJda();
			else if (parameters[i].getType() == MessageChannel.class)
				objects[i] = message == null ? null : message.getChannel();
		}
		simpleCommand.getMethod().invoke(simpleCommand.getObject(), objects);
    }	
}
