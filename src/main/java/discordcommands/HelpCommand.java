package discordcommands;

import java.awt.Color;
import java.util.LinkedList;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import commandhandler.CommandMap;
import commandhandler.SimpleCommand;
import gaiobot.BotListener;
import gaiobot.Ref;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import settings.PrefixHandler;

public class HelpCommand {

	private final CommandMap commandMap;
	
	public HelpCommand(CommandMap commandMap) {
		this.commandMap = commandMap;
	}
	
	@Command(name = "help", description="Shows the list of bot commands", type=ExecutorType.USER, power = 0)
	private void help(User user, MessageChannel channel, Guild guild, String[] args) {
		EmbedBuilder builder = new EmbedBuilder();
		LinkedList<String> commandNames = new LinkedList<String>();
		LinkedList<String> commandDescriptions = new LinkedList<String>();
		builder.setColor(Color.CYAN);
		if(args.length == 1) {
			builder.setTitle(args[0]);
			try {
				SimpleCommand command = commandMap.getSimCom(args[0]);
				if(!(command.getPower() > CommandMap.getPowerUser(guild, user)))
					builder.setDescription(command.getDescription());
				else {
					channel.sendMessage(new EmbedBuilder().setColor(Color.red).setDescription("You do not have the privileges to view this command " + user.getAsMention() + "!").build()).queue();
					BotListener.noMsg = false;
					return;
				}
			}catch(Exception e) {
				e.printStackTrace();
				channel.sendMessage(new EmbedBuilder().setColor(Color.red).setDescription("The command that you are looking up does not exist, please enter a valid command or type \"" + PrefixHandler.getPrefix(guild) + "help\" for a list of all available commands " + user.getAsMention() + "!").build()).queue();
				BotListener.noMsg = false;
				return;
			}
		}
		else {
			String userPerm = "";
			switch(CommandMap.getPowerUser(guild, user)) {
				case 4:
					userPerm = "Owner";
					break;
				case 3:
					userPerm = "Admin";
					break;
				case 2:
					userPerm = "Sub Admin";
					break;
				case 1:
					userPerm = "Tagged";
					break;
				default:
					userPerm = "New Join";
			}
			builder.setTitle("List of Commands for " + userPerm +":");
			for(SimpleCommand command : commandMap.getCommands()) {
				if(command.getExecutorType() == ExecutorType.CONSOLE)
					continue;
				if(guild != null && command.getPower() > CommandMap.getPowerUser(guild, user))
					continue;
				if(command.name.equals("help"))
					continue;
				commandNames.add(command.getName());				
			}	
		}
		commandNames.sort(null);
		for(int i=0; i<commandNames.size(); i++) {
			for(SimpleCommand command: commandMap.getCommands()) {
				if(commandNames.get(i).equals(command.getName()))
					commandDescriptions.add(command.description);
			}
		}
		for(int i=0; i<commandNames.size(); i++) {
			builder.addField(commandNames.get(i), commandDescriptions.get(i), false);
		}
		
		if(!user.hasPrivateChannel())
			user.openPrivateChannel().complete();
		((UserImpl)user).getPrivateChannel().sendMessage(builder.build()).queue();
		
		channel.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription(user.getAsMention()+", please check your pm's!").build()).queue();
		BotListener.noMsg = false;
	}
}