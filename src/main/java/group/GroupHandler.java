package group;

import java.util.HashMap;
import java.util.Map;

import gaiobot.GaioBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.Role;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class GroupHandler {
	private final GaioBot gaiobot;
	private final JDA jda;
	private final static Map<Category, Role> tags = new HashMap<>();
	private final static Map<Category, String> groups = new HashMap<>();
	
	public GroupHandler(JDA jda, GaioBot gaiobot) {
		this.jda = jda;
		this.gaiobot = gaiobot;
	}
	
	public void AddCategory(String categoryName) {
		if(groups.containsKey(jda.getCategoriesByName(categoryName, true).get(0))) {
			System.out.println("Category is already in the map!");
		}
		else {
			GroupHandler.AddLink(jda.getCategoriesByName(categoryName, true).get(0));
		}
	}
	
	public void RemoveCategory(String categoryName) {
		try {
			groups.remove(jda.getCategoriesByName(categoryName, true).get(0));
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Group doesn't exist!");
		}
	}
	
	public void AddRole(Category category, String roleName) {
		try {
			jda.getRolesByName(roleName, true).get(0);
			System.out.println("Found Role!");
			tags.put(category, jda.getRolesByName(roleName, true).get(0));
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Role Doesn't Exist! Please make sure role is valid!");
		}
		
	}
	
	public void RemoveRole(Category category) {
		tags.remove(category);		
	}
	
	public static String GetLink(Category category) {
		return groups.get(category);
	}
	
	public static void AddLink(Category category) {
		Invite invite = category.getGuild().getTextChannels().get(0).createInvite().setMaxAge(0).setMaxUses(0).complete();
		groups.put(category, invite.getURL());
	}
	
	public void RemoveLink(Category category) {
		groups.remove(category);
	}

}
