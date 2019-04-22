package leaderboards;

import java.util.LinkedList;
import java.util.List;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import group.GroupHandler;
import commandhandler.CommandMap;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class LeaderboardCommands {
	
	@SuppressWarnings("unused")
	private CommandMap commandmap;

	public LeaderboardCommands(CommandMap commandmap) {
		this.commandmap = commandmap;
	}
	
	/**
	 * Allows the user to request that the leaderboard be displayed
	 */
	@Command(name="lb", type = ExecutorType.USER, power=3, description="Use this command to display the latest version of the activity leaderbaord")
	public void displayBoard(User user, MessageChannel messagechannel, Message message, String args[]) {
		List<User> lb = new LinkedList<User>();
		lb = Leaderboard.topUsers(guilds, 10);
		System.out.println("Here is the Leader Board as it currently stands: ");
		System.out.println(lb);

//		System.out.println("Here's the link: " + link);
		if(!user.hasPrivateChannel())
			user.openPrivateChannel().complete();
		((UserImpl)user).getPrivateChannel().sendMessage("Here is the Leader Board as it currently stands: \n" + lb).queue();
	}

}
