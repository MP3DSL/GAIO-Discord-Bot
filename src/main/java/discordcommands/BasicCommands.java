package discordcommands;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

import commandhandler.Command;
import commandhandler.CommandMap;
import commandhandler.Command.ExecutorType;
import gaiobot.BotListener;
import gaiobot.Ref;
import gaiobot.GaioBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import settings.PrefixHandler;

public class BasicCommands{
    private final GaioBot gaioBot;
    private final CommandMap commandMap;

    public BasicCommands(GaioBot gaioBot, CommandMap commandMap){
        this.gaioBot = gaioBot;
        this.commandMap = commandMap;
    }

    //\/\/\/Console Commands\/\/\/\\
    @Command(name="exit", type=ExecutorType.CONSOLE)
    private void exit(){
        gaioBot.setRunning(false);
    }

    //\/\/\/USER COMMANDS\/\/\/\\	
  	//\/\/\/ADMIN LVL BASED COMMANDS\/\/\/\\
  	//Lvl 4 Owner
    @Command(name="prefix", type=ExecutorType.USER, description="Use this command followed by the desired prefix that you want to use for commands", power = 0)
	private void prefix(User user, String[] args, MessageChannel messageChannel, Guild guild) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		if(args.length == 1) {
			System.out.println("Setting Prefix...");
			PrefixHandler.setPrefix(guild, args[0]);
			System.out.println("Successfully set Prefix!");
			if(BotListener.updatePrefixes())
				messageChannel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("The prefix has been changed to \"" + PrefixHandler.getPrefix(guild) + "\"!").build()).queue();
			else
				messageChannel.sendMessage(error.setDescription("Unable to successfully change prefix :frowning2:").build()).queue();
		}
		else {
			messageChannel.sendMessage(error.setDescription(user.getAsMention() + " This command only requires one argument! If you are having trouble using this specific command, use the \"" + PrefixHandler.getPrefix(guild) + "help prefix\"! Otherwise, use the \"" + PrefixHandler.getPrefix(guild) + "help\" command for a list of all available commands!").build()).queue();
			BotListener.noMsg = false;
		}
	}
    //Lvl 3 Admin
    @Command(name="setPermission", power=3, type=ExecutorType.USER, description="[**YOU CAN NOT GIVE SOMEONE A LEVEL HIGHER THAN YOUR OWN**] Use this command followed by the specified user and intended permission level to change the privileges for the specified user \"(Prefix)setPermission @IntendedUser 1\"")
    private void setPermission(User user, MessageChannel channel, Guild guild, Message message, String[] args) {
    	EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		if(args.length == 0 || message.getMentionedUsers().size() == 0) {
			channel.sendMessage("setPermission <@User> <PermLvl>").queue();
			BotListener.noMsg = false;
			return;
		}
		
		int power = 0;
		try {
			power = Integer.parseInt(args[1]);
		}catch(NumberFormatException nfe) {
			channel.sendMessage(error.setDescription("The administrator rights is numbers. The higher the number, the more rights the user has.").build()).queue();
			BotListener.noMsg = false;
			return;
		}
		if(power<CommandMap.getPowerUser(guild, user)) {
			User target = message.getMentionedUsers().get(0);
			CommandMap.addUserPower(target, power);
			commandMap.save();
			channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription(target.getAsMention() + " has administrator privileges of level " + power).build()).queue();
		}
		else {
			channel.sendMessage(error.setDescription("You can not assign another user an admin level equal or higher than your own " + user.getAsMention() + "!").build()).queue();
		}
    }
    
    @Command(name="clear",power=0, type=ExecutorType.USER,description="Use this command to clear 100 messages in a text channel, or specify a number of messages you'd like to be cleared! ***[Must be at least 2 and at most 100]***")
	private void clear(String[] args, MessageChannel messageChannel, TextChannel tc, Message msg) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		MessageHistory history = new MessageHistory(tc);
		int num = 100;
		List<Message> msgs;
		if(args.length==1) {
			try {
				num = Integer.parseInt(args[0]);
			}catch(Exception e) {
				messageChannel.sendMessage(error.setDescription("Please enter a valid number!").build()).queue();
				return;
			}
		}
		if(num<1) {
			messageChannel.sendMessage(error.setDescription("Please enter a number greater than 0!").build()).queue();
			BotListener.noMsg = false;
			return;
		}
		msg.delete().queueAfter(15, TimeUnit.MILLISECONDS);
		msgs = history.retrievePast(num).completeAfter(50, TimeUnit.MILLISECONDS);
		if(msgs.size()<num)
			num = msgs.size();
		if(num>1) {
			for(int x = 0; x<msgs.size(); x++)
				msgs.get(x).delete().queueAfter(50,  TimeUnit.MILLISECONDS);
		}
		else if(num==1) {
			msgs = history.retrievePast(num).completeAfter(50, TimeUnit.MILLISECONDS);
			msgs.get(0).delete().queueAfter(50, TimeUnit.MILLISECONDS);
		}
		else {
			messageChannel.sendMessage(error.setDescription("No messages to clear!").build()).queue();
			BotListener.noMsg = false;
			return;
		}
		messageChannel.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("Successfully deleted " + num + " messages!").build()).queue();
		BotListener.noMsg = false;
	}
    //Lvl 2 SubAdmin
    //Lvl 1 Tagged
    @Command(name="roll", type=ExecutorType.USER, power = 1, description = "Use this command by itself to roll a standard 6 sided die. Use this commmand followed by a number to roll a specified number die")
	private void roll(User user, MessageChannel messageChannel, String[] args, Guild guild) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		int sides = 6;
		if(args.length==1) {
			try {
				sides = Integer.parseInt(args[0]);
			}catch(Exception e) {
				messageChannel.sendMessage(error.setDescription("Sorry " + user.getAsMention() + ", that is not a valid number. If you are having trouble with this specific command, use the \"" + PrefixHandler.getPrefix(guild) + "help roll\" command. Otherwise, type \"" + PrefixHandler.getPrefix(guild) + "help\" for a list of all available commands!").build()).queue();
				BotListener.noMsg = false;
				return;
			}
		}
		else if(args.length>1) {
			messageChannel.sendMessage(error.setDescription("Sorry " + user.getAsMention() + ", that is not a valid number. If you are having trouble with this specific command, use the \"" + PrefixHandler.getPrefix(guild) + "help roll\" command. Otherwise, type \"" + PrefixHandler.getPrefix(guild) + "help\" for a list of all available commands!").build()).queue();
			BotListener.noMsg = false;
			return;
		}
		messageChannel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("You rolled a " + (int) (Math.random() * sides + 1) + " " + user.getAsMention()).build()).queue();
	}
}