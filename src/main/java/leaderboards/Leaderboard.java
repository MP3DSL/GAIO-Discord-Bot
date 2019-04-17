package leaderboards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gaiobot.GaioBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class Leaderboard {
	private Map<Guild, LinkedList<Object>> leaderboards = new HashMap<>();
	private XSSFWorkbook workBook = null;
	private XSSFSheet sheet = null;
	private FileInputStream inputStream = null;
	
	public Leaderboard(GaioBot gaiobot){ //Creates an excel leaderboard for each Discord Server
		XSSFWorkbook wb;
		XSSFSheet sh;
		List<Guild> guilds = gaiobot.getJda().getGuilds();
		for(int i=0; i<guilds.size(); i++) {
			LinkedList<Object> guildLeaderboard = new LinkedList<Object>();
			try {
				String excelFilePath = "SERVER_SETTINGS/LEADERBOARDS/" + guilds.get(i).getName() + "_Leaderboards.xlsx";
				this.inputStream = new FileInputStream(new File(excelFilePath));
				wb = new XSSFWorkbook(this.inputStream);
			}
			catch (Exception e){
				System.out.println("Workbook doesn't exist! Creating new workbook!");
				wb = new XSSFWorkbook();
			}
			try {
				sh = wb.getSheetAt(0);
			}
			catch (Exception e) {
				System.out.println("Sheet doesn't exist! Creating new sheet!");
				sh = wb.createSheet(guilds.get(i).getName() + "_Leaderboards");
			}
		
			//Creating Cells
			CellStyle style = wb.createCellStyle();
			Font font = wb.createFont();
			font.setBold(true);
			font.setUnderline(HSSFFont.U_SINGLE);
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setFont(font);
			XSSFRow row = sh.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("USER NAME");
			cell.setCellStyle(style);
			Cell cell2 = row.createCell(1);
			cell2.setCellValue("POINTS");
			cell2.setCellStyle(style);
			
			try {
				FileOutputStream fileOut = new FileOutputStream("SERVER_SETTINGS/LEADERBOARDS/" + guilds.get(i).getName()+"_Leaderboards.xlsx");
				wb.write(fileOut);
				fileOut.close();
			} catch (Exception e) {
			}
			guildLeaderboard.add(wb);
			guildLeaderboard.add(sh);
			if(this.inputStream!=null)
				guildLeaderboard.add(this.inputStream);
			leaderboards.put(guilds.get(i), guildLeaderboard);
		}
	}
	private void setInfo(Guild guild) {
		LinkedList<Object> guildLeaderboard;
		try {
			guildLeaderboard = this.leaderboards.get(guild);
			
		}
		catch (Exception e) {
			System.out.println("Leaderboard for guild does not exist!");
			return;
		}
		this.workBook = (XSSFWorkbook) guildLeaderboard.get(0);
		this.sheet = (XSSFSheet) guildLeaderboard.get(1);
		if(guildLeaderboard.size()>2)
			this.inputStream = (FileInputStream) guildLeaderboard.get(2);
	}
	public void save(Guild guild) throws IOException { //Saves the excel workbook
		FileOutputStream fileOut = new FileOutputStream(guild.getName() + "_Leaderboards.xlsx");
        this.workBook.write(fileOut);
        fileOut.close();
        System.out.println("Successfully Saved Leaderboards workbook for " + guild.getName());
	}
	public void close(JDA jda) throws IOException { //Closes the excel workbook
		List<Guild> guilds = jda.getGuilds();
		for(int i=0; i<guilds.size(); i++) {
			setInfo(guilds.get(i));
			if(this.inputStream!=null)
        		this.inputStream.close();
        	workBook.close();
		}
	}
	public void addUser(User user, Guild guild) throws IOException { //Adds a user to the leaderboard
    	setInfo(guild);
		if(findUser(user)>-1) {
    		System.out.println("User is already on the leaderboard!");
    		return;
    	}
    	int rowNum = 1;
    	Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext()) {
        	Cell cell;
        	try {
        		cell = sheet.getRow(rowNum).getCell(0);
        	}catch (Exception e) {
        		break;
        	}
        	if(cell.getStringCellValue().compareTo(user.getName())>0) {
        		break;
        	}
        	rowNum++;
        }
        System.out.println(rowNum);
        try {
        	sheet.shiftRows(rowNum, sheet.getLastRowNum(), 1);
        }catch (Exception e) {
        }
        XSSFRow row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(user.getName());
        row.createCell(1).setCellValue(0);
        save(guild);
    }
    private int findUser(User user) throws IOException { //Finds the user in the leaderboard
    	Iterator<Row> iterator = sheet.iterator(); 
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Cell cell = nextRow.getCell(0);
            try {
            	if(cell.getStringCellValue().equals(user.getName())) {
            		System.out.println("Successfully found the user!");
            		return nextRow.getRowNum();
            	}
            }catch(Exception e) {
            }
        }
        System.out.println("Could not find the user :'(");
        return -1;
    }
    public void removeUser(User user, Guild guild) throws IOException { //Removes the user
        setInfo(guild);
    	try {
    		int row = findUser(user);
        	sheet.removeRow(sheet.getRow(row));
        	System.out.println("Successfully removed user \"" + user.getName() + "\" from the Leaderboard!");
        	sheet.shiftRows(row+1, sheet.getLastRowNum(), -1);
        	save(guild);
        }
        catch (Exception e){
        	System.out.println("User \"" + user.getName() + "\" does not exist on the leaderboard!");
        }
    }
    public int getPoints(User user, Guild guild) throws IOException { //Gets the points that user has
		setInfo(guild);
    	int rowNum = findUser(user);
		if(rowNum>-1) {
			return (int)sheet.getRow(rowNum).getCell(1).getNumericCellValue();
		}
		else {
			return -1;
		}
    }
    public void addPoints(User user, Guild guild, int points) throws IOException{ //Adds points to the user
    	setInfo(guild);
    	int rowNum = findUser(user);
		if(rowNum>-1) {
			Cell cell = sheet.getRow(rowNum).getCell(1);
			cell.setCellValue(cell.getNumericCellValue() + points);
			save(guild);
		}
		else {
			return;
		}
    }
    public void subtractPoints(User user, Guild guild, int points) throws IOException{ //Subtracts points from the user
    	setInfo(guild);
    	int rowNum = findUser(user);
		if(rowNum>-1) {
			Cell cell = sheet.getRow(rowNum).getCell(1);
			cell.setCellValue(cell.getNumericCellValue() - points);
			save(guild);
		}
		else {
			return;
		}
    }
    public void setPoints(User user, Guild guild, int points) throws IOException{ //Sets the user's points to the specified value
    	setInfo(guild);
    	int rowNum = findUser(user);
		if(rowNum>-1) {
			Cell cell = sheet.getRow(rowNum).getCell(1);
			cell.setCellValue(points);
			save(guild);
		}
		else {
			return;
		}
    }
    public void resetPoints(Guild guild) throws IOException {
    	setInfo(guild);
    	for(int i = 1; i<=sheet.getLastRowNum(); i++) {
    		Row row = sheet.getRow(i);
    		Cell cell = row.getCell(1);
    		cell.setCellValue(0);
    	}
    	save(guild);
    }
    
    public List<User> topUsers(Guild guild, int num) {
    	setInfo(guild);
    	List<User> topUsers = new LinkedList<User>();
    	List<Integer> points = new LinkedList<Integer>();   	
    	for(int i=1; i<=sheet.getLastRowNum(); i++) {
    		Row row = sheet.getRow(i);
    		Cell cell = row.getCell(1);
    		points.add((int) (cell.getNumericCellValue()));
    	}
    	Collections.sort(points, Collections.reverseOrder());
    	//System.out.println("one");
    	for(int i = 0; i<points.size(); i++) {
    		//System.out.println("a");
    		int compPoints = points.get(i);
    		//System.out.println("b");
    		for(int x = 1; x<=sheet.getLastRowNum(); x++) {
    			//System.out.println(1);
    			Row row = sheet.getRow(x);
    			Cell cell = row.getCell(1);
    			//System.out.println(2);
    			if((((int)cell.getNumericCellValue())==compPoints)&&(topUsers.size()<num)) {
    				//System.out.println("yes");
    				if(topUsers.contains(guild.getMembersByName(row.getCell(0).getStringCellValue(), false).get(0).getUser())) {
    					//System.out.println("TRUE");
    					continue;
    				}
    				//System.out.println("no");
    				topUsers.add(guild.getMembersByName(row.getCell(0).getStringCellValue(), false).get(0).getUser());
    			}
    			//System.out.println(3);
    		}
    		//System.out.println("c");
    	}
    	//System.out.println("two");
    	return topUsers;
    }
}
