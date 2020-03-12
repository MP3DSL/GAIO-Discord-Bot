package events;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import commandhandler.CommandMap;
import gaiobot.BotListener;
import gaiobot.Ref;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import settings.PrefixHandler;

public class Vote{
	
	protected static TextChannel tc;
	private static HashMap<Guild, Poll> polls = new HashMap<>();
	private static final String[] EMOTI = {":one:",":two:",":three:",":four:",":five:",":six:",":seven:",":eight:",":nine:",":keycap_ten:"};
	
	class Poll {
		
		private String creator;
		private String heading;
		List<String> answers;
		HashMap<String, Integer> votes;
		
		Poll(Member creator, String heading, List<String> answers) {
			this.creator = creator.getUser().getId();
			this.heading = heading;
			this.answers = answers;
			this.votes = new HashMap<>();
		}
		Member getCreator(Guild guild) {
			return guild.getMember(guild.getJDA().getUserById(creator));
		}
	}
	
	EmbedBuilder getParsedPoll(Poll poll, Guild guild) {
		StringBuilder ansSTR = new StringBuilder();
		final AtomicInteger count = new AtomicInteger();
		for(int i = 0; i<poll.answers.size(); i++) {
			int votescount = 0;
			List<Integer> voteNumber = new ArrayList<Integer>(poll.votes.values());
			for(int x = 0; x<voteNumber.size(); x++) {
				if(voteNumber.get(x) == (i+1))
					votescount++;
			}
			ansSTR.append(EMOTI[count.get()] + " - " + poll.answers.get(i) + " - Votes: " + votescount + " \n");
			count.addAndGet(1);
		}
		
		return new EmbedBuilder()
				.setAuthor(poll.getCreator(guild).getEffectiveName() + "'s poll", null, guild.getIconUrl())
				.setDescription(":pencil:   " + poll.heading + "\n\n" + ansSTR.toString())
				.setFooter("Enter " + PrefixHandler.getPrefix(guild) + "vote v [number] to vote!", null)
				.setColor(Color.cyan);
	}
	
	private static void deletePoll(Guild guild){
		String saveFile = "SERVER_SETTINGS/POLLS/" + guild.getName() + "_Poll.json";
		File file = new File(saveFile);
		if(file.exists()) {
			try {
				file.delete();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void message(String content, Color color, User user, Guild guild) {
		if(content.equals("help")) {
			EmbedBuilder eb = new EmbedBuilder().setTitle("***VOTE ARGUMENTS***");
					eb.setDescription("Here's a list of valid vote arguments:");
				if(CommandMap.getPowerUser(guild, user)>=3) {
					eb.addField("create", "Use this argument to create a poll! \nFormat = \"" + PrefixHandler.getPrefix(guild) + "vote create 'question'|'answer1'|'answer2'|'answer3'|...", false);
					eb.addField("close", "Use this argument to close an existing poll! (***Must be the creator or an owner of the discord!***)", false);
					eb.addField("stats", "Use this argument to see the stats of an existing poll!", false);
				}
					eb.addField("v", "Use this argument followed by the number that corresponds to the answer you want to vote for!", false);
					eb.setColor(color);
			if(!user.hasPrivateChannel())
				user.openPrivateChannel().complete();
			((UserImpl)user).getPrivateChannel().sendMessage(eb.build()).queue();
			message(user.getAsMention()+", please check your pm's!", Color.green, user, guild);
			BotListener.noMsg = false;
		}
		else {
			EmbedBuilder eb = new EmbedBuilder().setDescription(content).setColor(color);
			tc.sendMessage(eb.build()).queue();
		}
	}
	
	@Command(name="vote", power = 0, type=ExecutorType.USER, description="Use this command to learn the arguments to make a poll!")
	private void vote(String[]args, Guild guild, User user, TextChannel tc) {
		Vote.tc = tc;
		if(args.length<1) {
			message("help", Color.cyan, user, guild);
			return;
		}
		switch(args[0].toLowerCase()) {
			case "create":
				if(CommandMap.getPowerUser(guild, user) >= 0) {
					if(polls.containsKey(guild)) {
						message("There is already a vote running on this discord!", Color.red, user, guild);
						BotListener.noMsg = false;
						return;
					}
		
					String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
					List<String> content = Arrays.asList(argsSTRG.split("\\|"));
					String heading = content.get(0);
					if(content.size()>11) {
						message("The max amount of answers is 10!", Color.red, user, guild);
						BotListener.noMsg = false;
						return;
					}
					List<String> answers = new ArrayList<>(content.subList(1, content.size()));
				
					Poll poll = new Poll(guild.getMember(user), heading, answers);
					polls.put(guild, poll);
		
					tc.sendMessage(getParsedPoll(poll, guild).build()).queue();
				}
				else {
					message("You must be an admin to use this command!", Color.red, user, guild);
					BotListener.noMsg = false;
				}
				break;
		
			case "v":
				if(!polls.containsKey(guild)) {
					message("There is currently no poll running on this discord!", Color.RED, user, guild);
					BotListener.noMsg = false;
					return;
				}
			
				Poll vote = polls.get(guild);
				int voteNum;
				try {
					voteNum = Integer.parseInt(args[1]);
					if(voteNum > vote.answers.size())
						throw new Exception();
				}catch(Exception e) {
					message("Please enter a valid number to vote for!", Color.red, user, guild);
					BotListener.noMsg = false;
					return;
				}
			
				if(vote.votes.containsKey(user.getId())) {
					message("Sorry, but you can only vote **once** per poll " + user.getAsMention() + "! If you made a mistake voting, please contact an admin!", Color.RED, user, guild);
					BotListener.noMsg = false;
					return;
				}

				vote.votes.put(user.getId(), voteNum);
				polls.replace(guild, vote);
			
				if(!user.hasPrivateChannel())
					user.openPrivateChannel().complete();
				((UserImpl)user).getPrivateChannel().sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("You have voted for answer number '" + voteNum + "'!").build()).queue();
			
				break;
			
			case "stats":
				if(CommandMap.getPowerUser(guild, user) >= 0) {
					if(!polls.containsKey(guild)) {
						message("There is currently no poll running on this discord!", Color.RED, user, guild);
						BotListener.noMsg = false;
						return;
					}
					tc.sendMessage(getParsedPoll(polls.get(guild), guild).build()).queue();
				}
				else {
					message("You must be an admin to use this command!", Color.red, user, guild);
					BotListener.noMsg = false;
				}
			
				break;
			
			case "close":
				if(CommandMap.getPowerUser(guild, user) >= 0) {
					if(!polls.containsKey(guild)) {
						message("There is currently no poll running on this discord!", Color.RED, user, guild);
						BotListener.noMsg = false;
						return;
					}
					
					Poll close = polls.get(guild);
			
					if(!close.getCreator(guild).equals(guild.getMember(user)) && CommandMap.getPowerUser(guild, user)!=4) {
						message("Only the creator of the poll (" + close.getCreator(guild).getAsMention() +") or the owner(s) of the discord can close this poll!",Color.red, user, guild);
						BotListener.noMsg = false;
						return;
					}
				
					polls.remove(guild);
					tc.sendMessage(getParsedPoll(close, guild).build()).queue();
					message("Poll closed by " + user.getAsMention() + "!", Color.green, user, guild);
					deletePoll(guild);
				}
				else {
					message("You must be an admin to use this command!", Color.red, user, guild);
					BotListener.noMsg = false;
				}
				break;
			default:
				message("help", Color.red, user, guild);
		}
	}
}
