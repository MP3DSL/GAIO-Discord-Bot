package group;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import commandhandler.CommandMap;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class GroupCommands {
	private final CommandMap commandmap;
	
	public GroupCommands(CommandMap commandmap) {
		this.commandmap = commandmap;
	}
	
	@Command(name="invite", type = ExecutorType.USER, power=3, description="Use this command to invite a new user to the group")
	public void invite(User user, MessageChannel messagechannel, Message message, String args[]) {
		Category category = message.getCategory();
		String link;
		link = GroupHandler.GetLink(category);
		if(link == null) {
			GroupHandler.AddLink(category);
			System.out.println(2);
			link = GroupHandler.GetLink(category);
			System.out.println(3);
		}

		System.out.println("Here's the link: " + link);
		if(!user.hasPrivateChannel())
			user.openPrivateChannel().complete();
		((UserImpl)user).getPrivateChannel().sendMessage("Here is a link to the group: " + link).queue();
	}
}
