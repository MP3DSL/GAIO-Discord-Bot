package gaiobot;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import commandhandler.CommandMap;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.managers.GuildController;
import settings.PrefixHandler;

/**
 * Breaks down possible discord events in order to code accordingly for each one
 */
public class BotListener implements EventListener{
    private final CommandMap commandMap;
    public static boolean noMsg = true;
    private static boolean pinned = false;
    private static Map <Guild, String> prefixes;
    
    public BotListener(CommandMap commandMap){
        this.commandMap = commandMap;
    }

    static List<Message> messages = new LinkedList<Message>();

    /**
     * Separates the events into their respective categories
     */
    @Override
    public void onEvent(Event evt){
        System.out.println(evt.getClass().getSimpleName());
		if(evt instanceof MessageReceivedEvent)
			onMessageReceived((MessageReceivedEvent) evt);
		else if(evt instanceof GuildMemberJoinEvent)
			onGuildMemberJoin((GuildMemberJoinEvent)evt);
		else if(evt instanceof GuildMemberLeaveEvent)
			onGuildMemberLeave((GuildMemberLeaveEvent) evt);
		else if(evt instanceof MessageUpdateEvent)
			onMessageUpdate((MessageUpdateEvent) evt);
		else if(evt instanceof GuildMemberRoleAddEvent)
			onGuildMemberRoleAdd((GuildMemberRoleAddEvent) evt);
		else if(evt instanceof GuildMemberRoleRemoveEvent)
			onGuildMemberRoleRemove((GuildMemberRoleRemoveEvent) evt);
		else if(evt instanceof RoleCreateEvent)
			onRoleCreate((RoleCreateEvent) evt);
		else if(evt instanceof RoleDeleteEvent)
			onRoleDelete((RoleDeleteEvent) evt);
		else if(evt instanceof ReadyEvent)
			onReady((ReadyEvent) evt);
		else if (evt instanceof MessageReactionAddEvent)
			onMessageReactionAddEvent((MessageReactionAddEvent) evt);
		else if (evt instanceof MessageReactionRemoveEvent) {
			onMessageReactionRemoveEvent((MessageReactionRemoveEvent) evt);
		}
    }

    /**
     * Event is triggered when bot is online and ready for use
     */
	private void onReady(ReadyEvent evt){//JDA Events **NEEDS WORK**
		List<Guild> guilds = evt.getJDA().getGuilds();
		for(int i = 0; i<guilds.size(); i++) {
			if(guilds.get(i).getTextChannelsByName("rules", true).size() != 0) {
				//System.out.println(guilds.get(i).getName());
				TextChannel rules = guilds.get(i).getTextChannelsByName("rules", true).get(0);
				//System.out.println(rules.getName());
				MessageHistory history = new MessageHistory(rules);
				if(history != null) {
					Message msg = history.retrievePast(1).complete().get(0);
					//System.out.println(msg.getContentDisplay());
					msg.addReaction("\u2705").completeAfter(50, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	//\/\/\/Message Events\/\/\/\\
    /**
     * Event is triggered when a messaged is sent to the discord server
     */
	private void onMessageReceived(MessageReceivedEvent evt) {
		if(pinned) {
			evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
			pinned = false;
			return;
		}
		if(evt.getAuthor().equals(evt.getJDA().getSelfUser())) {
			if(noMsg)
				return;
			else if(evt.getPrivateChannel() != null) {
				return;
			}
			else {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
						noMsg = true;
					}
				}, Ref.msgTimer);
			}
		}
		String msg = evt.getMessage().getContentRaw();
		if(msg.startsWith(prefixes.get(evt.getGuild()))) {
			msg = msg.replaceFirst(prefixes.get(evt.getGuild()), "");
			if(commandMap.commandUser(evt.getAuthor(), msg, evt.getMessage())) {
				if(evt.getTextChannel() != null && evt.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
					if(!msg.contains("clear"))
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
				}
			}
		}
		if(evt.getMessage().getEmotes().size()>0) {
			List<Emote> emotes = evt.getMessage().getEmotes();
			String message = evt.getMessage().getContentRaw();
			int first = 0;
			int last = 0;
			int count = 0;
			int numSpaces = -1;
			for(int i = 0; i<emotes.size(); i++) {
				first = message.indexOf("<", first);
				last = message.indexOf(">", last) + 1;
				count += (last-first);
				first++;
				numSpaces++;
			}
			for(int i = 0; i<emotes.size(); i++) {
				emotes.get(i).getName();
				try {
					evt.getTextChannel().sendFile(new File("DISCORD_EMOJIS/" + emotes.get(i).getName() + ".png")).queue();
					if(message.length() - (count + numSpaces)==0)
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
				}catch(Exception e) {
				}
			}
		}
    }

	/**
     * Event is triggered when a messaged updated and resubmitted to the discord server
     */
    private void onMessageUpdate(MessageUpdateEvent evt) {
		if(evt.getMessage().isPinned())
			pinned = true;
		if(evt.getAuthor().equals(evt.getJDA().getSelfUser())) {
			if(noMsg)
				return;
			else if(evt.getPrivateChannel() != null) {
				return;
			}
			else {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
						noMsg = true;
					}
				}, Ref.msgTimer);
			}
		}
		String msg = evt.getMessage().getContentRaw();
		if(msg.startsWith(prefixes.get(evt.getGuild()))) {
			msg = msg.replaceFirst(prefixes.get(evt.getGuild()), "");
			if(commandMap.commandUser(evt.getAuthor(), msg, evt.getMessage())) {
				if(evt.getTextChannel() != null && evt.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
					if(!msg.contains("clear"))
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
				}
			}
		}
		if(evt.getMessage().getEmotes().size()>0) {
			List<Emote> emotes = evt.getMessage().getEmotes();
			String message = evt.getMessage().getContentRaw();
			int first = 0;
			int last = 0;
			int count = 0;
			int numSpaces = -1;
			for(int i = 0; i<emotes.size(); i++) {
				first = message.indexOf("<", first);
				last = message.indexOf(">", last) + 1;
				count += (last-first);
				first++;
				numSpaces++;
			}
			for(int i = 0; i<emotes.size(); i++) {
				emotes.get(i).getName();
				try {
					evt.getTextChannel().sendFile(new File("DISCORD_EMOJIS/" + emotes.get(i).getName() + ".png")).queue();
					if(message.length() - (count + numSpaces)==0)
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
				}catch(Exception e) {
				}
			}
		}
    }
    //\/\/\/Guild Member Events\/\/\/\\
    /**
     * Event is triggered when a user joins the discord
     */
	private void onGuildMemberJoin(GuildMemberJoinEvent evt) {
		evt.getGuild().getTextChannelsByName(Ref.welcome, true).get(0).sendMessage("Welcome to the " + evt.getGuild().getName() + " discord " + evt.getUser().getAsMention() + "! For a list of commands, use the \"" + prefixes.get(evt.getGuild()) + "help\" command and I will pm you the list of commands with a description for each one!").queue();
	}
	
	/**
     * Event is triggered when a user leaves the discord
     */
	private void onGuildMemberLeave(GuildMemberLeaveEvent evt) {
		evt.getGuild().getTextChannelsByName(Ref.welcome, true).get(0).sendMessage("No don't go!!! I won't forget you " + evt.getUser().getAsMention() + "! :sob:").queue();
		CommandMap.removeUserPower(evt.getUser());
		try {
			if(CommandMap.getPowerUser(evt.getGuild(), evt.getUser())>=1)
				GaioBot.leaderboard.removeUser(evt.getUser(), evt.getGuild());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Event is triggered when a user has a role added to them
     */
	private void onGuildMemberRoleAdd(GuildMemberRoleAddEvent evt) {
	}
	
	/**
     * Event is triggered when a user has a role removed from them
     */
	private void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent evt) {
	}
	
	//\/\/\/Role Events\/\/\/\\
	/**
     * Event is triggered when a new role is created
     */
	private void onRoleCreate(RoleCreateEvent evt) {
	}
	
	/**
     * Event is triggered when an existing role is deleted
     */
	private void onRoleDelete(RoleDeleteEvent evt) {
	}
	
	private void onMessageReactionAddEvent(MessageReactionAddEvent evt) {
		//System.out.println(evt.getUser().getName());
		if(evt.getTextChannel().getName().equals(Ref.verificationTextChannel)) {
			if(evt.getReactionEmote().getName().equals("✅")) {
				//System.out.println("check");
				//System.out.println(evt.getMember().getUser().getName());
				Role role = evt.getGuild().getRolesByName(Ref.verificationRole, true).get(0);
				GuildController controller = new GuildController(evt.getGuild());
				controller.addRolesToMember(evt.getMember(), role).complete();
			}
		}
	}
	
	private void onMessageReactionRemoveEvent(MessageReactionRemoveEvent evt) {
		//System.out.println(evt.getUser().getName());
				if(evt.getTextChannel().getName().equals(Ref.verificationTextChannel)) {
					if(evt.getReactionEmote().getName().equals("✅")) {
						//System.out.println("check");
						//System.out.println(evt.getMember().getUser().getName());
						Role role = evt.getGuild().getRolesByName(Ref.verificationRole, true).get(0);
						GuildController controller = new GuildController(evt.getGuild());
						controller.removeRolesFromMember(evt.getMember(), role).complete();
					}
				}
	}
	
	public static void setPrefixes(GaioBot bot) {
		prefixes = PrefixHandler.loadPrefixes(bot);
	}
	
	public static boolean updatePrefixes() {
		prefixes = PrefixHandler.getPrefixes();
		return true;
	}
}