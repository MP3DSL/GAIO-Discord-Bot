package group;

import java.util.HashMap;
import java.util.Map;

import gaiobot.GaioBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Role;

public class GroupHandler {
	private final GaioBot gaiobot;
	private final JDA jda;
	private final static Map<Category, Role> tags = new HashMap<>();
	private final Map<Category, String> groups = new HashMap<>();
	
	public GroupHandler(JDA jda, GaioBot gaiobot) {
		this.jda = jda;
		this.gaiobot = gaiobot;
	}
	
	public void AddCategory(String categoryName) {
		if(groups.containsKey(jda.getCategoriesByName(categoryName, true).get(0))) {
			System.out.println("Category is already in the map!");
		}
		else {
			this.AddLink(jda.getCategoriesByName(categoryName, true).get(0));
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
	
	public String GetLink(Category category) {
		return groups.get(category);
	}
	
	public void AddLink(Category category) {
		groups.put(category, category.createInvite().toString());
	}
	
	public void RemoveLink(Category category) {
		groups.remove(category);
	}

}
