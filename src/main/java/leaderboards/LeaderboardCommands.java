package leaderboards;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import gaiobot.Ref;
import commandhandler.CommandMap;
import gaiobot.GaioBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import settings.PrefixHandler;

public class LeaderboardCommands {
	
	@SuppressWarnings("unused")
	private CommandMap commandmap;

	public LeaderboardCommands(CommandMap commandmap) {
		this.commandmap = commandmap;
	}
	
	/**
	 * Allows the user to request that the leaderboard be displayed
	 */
	@Command(name="lb", type = ExecutorType.USER, power=1, description="Use this command to display the latest version of the activity leaderbaord")
	public void displayBoard(User user, MessageChannel messageChannel, Message message, String args[], Guild guild) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		if(messageChannel instanceof TextChannel) {
			TextChannel textChannel = (TextChannel)messageChannel;
			if(!textChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS))
				return;
		}
		List<User> topUsers;
		int num = 5;
		if(args.length == 0) {
			//System.out.println("one");
			topUsers = GaioBot.leaderboard.topUsers(message.getGuild(), num);
			//System.out.println("two");
		}
		else if(args.length == 1) {
			try {
				num = Integer.parseInt(args[0]);
			}catch (Exception e) {
				messageChannel.sendMessage(error.setDescription("Please provide an actual number!").build()).queue();
				return;
			}
			if(num<=30)
				topUsers = GaioBot.leaderboard.topUsers(message.getGuild(), num);
			else {
				messageChannel.sendMessage(error.setDescription("The max number of users that can be requested is 30 " + user.getAsMention()).build()).queue();
				return;
			}
		}
		else {
			messageChannel.sendMessage(error.setDescription("Sorry, you didn't exicute the command properly " + user.getAsMention() + ". If you are having trouble, please use the \"" + PrefixHandler.getPrefix(guild) + "help leaderboard\" command for information on how to use this specific command!").build()).queue();
			return;
		}
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Here's a list of the top " + num + " users on the Leaderboard:");
		builder.setColor(Color.orange);
		for(int i=0;i<topUsers.size();i++) {
			try {
				builder.addField(topUsers.get(i).getName(), ""+GaioBot.leaderboard.getPoints(topUsers.get(i), message.getGuild()), false);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		messageChannel.sendMessage(builder.build()).queue();
	}
	
	@Command(name="points", power=1, type=ExecutorType.USER, description="Use this command to check how many points you have! Or if you want to see how many points someone else has, use the same command followed by a space and the target user!")
	private void points(User user, Guild guild, MessageChannel messageChannel, String[] args, Message message) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		int points = -1;
		if(args.length==0) {
			try {
				points = GaioBot.leaderboard.getPoints(user, guild);
			}catch (Exception e) {
				System.out.println("Unable to get user points...");
			}
			if(points == -1) {
				try {
					GaioBot.leaderboard.addUser(user, guild);
					points = GaioBot.leaderboard.getPoints(user, guild);
				}catch (Exception e) {
					System.out.println("Unable to add user to leaderboards...");
				}
			}
			messageChannel.sendMessage(new EmbedBuilder().setColor(Color.orange).setDescription("You have " + points + " points " + user.getAsMention() + "!").build()).queue();
		}
		else if(args.length==1) {
			User target = message.getMentionedUsers().get(0);
			if(CommandMap.getPowerUser(guild, user)>=1) {
				try {
					points = GaioBot.leaderboard.getPoints(target, guild);
				}catch (Exception e) {
					System.out.println("Unable to get user points...");
					e.printStackTrace();
				}
				if(points == -1) {
					try {
						GaioBot.leaderboard.addUser(target, guild);
						points = GaioBot.leaderboard.getPoints(target, guild);
					}catch (Exception e) {
						System.out.println("Unable to add user to leaderboards...");
						e.printStackTrace();
					}
				}
				messageChannel.sendMessage(new EmbedBuilder().setColor(Color.orange).setDescription(target.getAsMention() + " has " + points + " points!").build()).queue();
			}
			else {
				messageChannel.sendMessage(error.setDescription("The user you specified has not been tagged or is not a member of this discord, and therefore is not on the leaderboard!").build()).queue();
			}
		}
		else {
			messageChannel.sendMessage(error.setDescription("Sorry, you didn't exicute the command properly " + user.getAsMention() + ". If you are having trouble, please use the \"" + PrefixHandler.getPrefix(guild) + "help points\" command for information on how to use this specific command!").build()).queue();
		}
	}
	
	@Command(name="addPoints", power = 3, type=ExecutorType.USER, description="Use this command followed by a space and a target user(@targetUser) to add points to said user!")
	private void addPoints(User user, Guild guild, String[] args, MessageChannel messageChannel, Message message) {
		EmbedBuilder error = new EmbedBuilder().setColor(Color.red);
		if(args.length == 2) {
			int points = 0;
			User target;
			try {
				target = message.getMentionedUsers().get(0);
			}catch (Exception e) {
				messageChannel.sendMessage(error.setDescription("Please @ the user you want to add points to!").build()).queue();
				return;
			}
			try {
				points = Integer.parseInt(args[1]);
			}catch (Exception e) {
				messageChannel.sendMessage(error.setDescription("Please provide an actual number "+user.getAsMention()).build()).queue();
				e.printStackTrace();
				return;
			}
			int pastPoints = 0;
			try{
				pastPoints = GaioBot.leaderboard.getPoints(target, guild);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(pastPoints == -1) {
				try {
					GaioBot.leaderboard.addUser(target, guild);
					pastPoints = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(CommandMap.getPowerUser(guild, user)>=1) {
				try {
					GaioBot.leaderboard.addPoints(target, guild, points);
					points = GaioBot.leaderboard.getPoints(target, guild);
					GaioBot.leaderboard.save(guild);
				}catch (Exception e) {
					System.out.println("Unable to add points to the user...");
					e.printStackTrace();
				}
				messageChannel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription(target.getAsMention() + " had " + pastPoints + " points, but now has " + points).build()).queue();
			}
			else {
				messageChannel.sendMessage(error.setDescription("The user you specified has not been tagged or is not a member of this discord, and therefore is not on the leaderboard!").build()).queue();
				return;
			}
		}
		else {
			messageChannel.sendMessage(error.setDescription("Sorry, you didn't exicute the command properly " + user.getAsMention() + ". If you are having trouble, please use the \"" + PrefixHandler.getPrefix(guild) + "help addPoints\" command for information on how to use this specific command!").build()).queue();
		}
	}

}
