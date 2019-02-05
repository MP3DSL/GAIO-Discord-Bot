package gaiobot;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import commandhandler.CommandMap;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * Breaks down possible discord events in order to code accordingly for each one
 */
public class BotListener implements EventListener{
    private final CommandMap commandMap;
    public static boolean noMsg = true;
    private static boolean pinned = false;
    
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
    }

    /**
     * Event is triggered when bot is online and ready for use
     */
	private void onReady(ReadyEvent evt){//JDA Events **NEEDS WORK**
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
		if(msg.startsWith(Ref.prefix)) {
			msg = msg.replaceFirst(Ref.prefix, "");
			if(commandMap.commandUser(evt.getAuthor(), msg, evt.getMessage())) {
				if(evt.getTextChannel() != null && evt.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
					if(!msg.contains("clear"))
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
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
		if(msg.startsWith(Ref.prefix)) {
			msg = msg.replaceFirst(Ref.prefix, "");
			if(commandMap.commandUser(evt.getAuthor(), msg, evt.getMessage())) {
				if(evt.getTextChannel() != null && evt.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
					if(!msg.contains("clear"))
						evt.getMessage().delete().queueAfter(30, TimeUnit.MILLISECONDS);
				}
			}
		}
    }
    //\/\/\/Guild Member Events\/\/\/\\
    /**
     * Event is triggered when a user joins the discord
     */
	private void onGuildMemberJoin(GuildMemberJoinEvent evt) {
		evt.getGuild().getTextChannelsByName(Ref.welcome, true).get(0).sendMessage("Welcome to the " + evt.getGuild().getName() + " discord " + evt.getUser().getAsMention() + "! For a list of commands, use the \"" + Ref.prefix + "help\" command and I will pm you the list of commands with a description for each one!").queue();
	}
	
	/**
     * Event is triggered when a user leaves the discord
     */
	private void onGuildMemberLeave(GuildMemberLeaveEvent evt) {
		evt.getGuild().getTextChannelsByName(Ref.welcome, true).get(0).sendMessage("No don't go!!! I won't forget you " + evt.getUser().getAsMention() + "! :sob:").queue();
		CommandMap.removeUserPower(evt.getUser());
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
}