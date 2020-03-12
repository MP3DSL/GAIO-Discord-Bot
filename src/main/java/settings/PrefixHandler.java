package settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import gaiobot.Ref;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

public class PrefixHandler {
	private static Map<Guild, String> prefixes = new HashMap<Guild, String>();
	private static FileInputStream inputStream = null;
	private static XSSFWorkbook workBook = null;
	private static XSSFSheet sheet = null;
	
	public static Map<Guild, String> loadPrefixes(GaioBot bot){
		List<Guild> guilds = bot.getJda().getGuilds();
		try {
			String excelFilePath = "SERVER_SETTINGS/GuildPrefixes.xlsx";
			inputStream = new FileInputStream(new File(excelFilePath));
			workBook = new XSSFWorkbook(inputStream);
		}
		catch (Exception e){
			System.out.println("Workbook doesn't exist! Creating new workbook!");
			workBook = new XSSFWorkbook();
		}
		try {
			sheet = workBook.getSheetAt(0);
		}
		catch (Exception e) {
			System.out.println("Sheet doesn't exist! Creating new sheet!");
			sheet = workBook.createSheet("GuildPrefixes");
		}
		CellStyle style = workBook.createCellStyle();
		Font font = workBook.createFont();
		font.setBold(true);
		font.setUnderline(HSSFFont.U_SINGLE);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);
		if(sheet.getRow(0)==null) {
			XSSFRow row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("GUILD");
			cell.setCellStyle(style);
			Cell cell2 = row.createCell(1);
			cell2.setCellValue("PREFIX");
			cell2.setCellStyle(style);
		}
		Iterator<Row> guildRow;
		Boolean exists = false;
		XSSFRow row =  null;
		Row tempRow = null;
		Cell cell = null;
		Cell cell2 = null;
		for(Guild g:guilds) {
			exists = false;
			guildRow = sheet.iterator();
			while(guildRow.hasNext()) {
				try {
					tempRow = guildRow.next();
					if(tempRow.getCell(0).getStringCellValue().equals(g.getName())) {
						prefixes.put(g, tempRow.getCell(1).getStringCellValue());
						exists = true;
						break;
					}
				}catch(Exception e) {
					System.out.println("Cell is null: Guild Prefixes");
				}
			}
			if(!exists) {
				row = sheet.createRow(tempRow.getRowNum() + 1);
				cell = row.createCell(0);
				cell.setCellValue(g.getName());
				cell.setCellStyle(style);
				cell2 = row.createCell(1);
				cell2.setCellValue(Ref.prefix);
				cell2.setCellStyle(style);
				prefixes.put(g, Ref.prefix);
			}
		}
		try {
			FileOutputStream fileOut = new FileOutputStream("SERVER_SETTINGS/GuildPrefixes.xlsx");
			workBook.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			System.out.println("Faild to write to workbook: GuildPrefixes");
		}
		return prefixes;
	}
	
	public static String getPrefix(Guild guild) {
		return prefixes.get(guild);
	}
	
	public static Map<Guild, String> getPrefixes() {
		return prefixes;
	}
	
	public static boolean setPrefix(Guild guild, String prefix) {
		int rowNum = findPrefix(guild);
		if(rowNum == -1) {
			rowNum = sheet.getLastRowNum() + 1;
		}
		CellStyle style = workBook.createCellStyle();
		Font font = workBook.createFont();
		font.setBold(true);
		font.setUnderline(HSSFFont.U_SINGLE);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);
		XSSFRow row = sheet.createRow(rowNum);
		Cell cell = row.createCell(0);
		cell.setCellValue(guild.getName());
		cell.setCellStyle(style);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(prefix);
		cell2.setCellStyle(style);
		System.out.println("Changing Prefix in map...");
		prefixes.put(guild, prefix);
		System.out.println("Done! Saving Prefix to workbook...");
		savePrefixes();
		return true;
	}
	
	private static int findPrefix(Guild guild) {
		Iterator<Row> guildRow;
		Row tempRow = null;
		System.out.println("Getting sheet iterator");
		guildRow = sheet.iterator();
		System.out.println("Searching for the guild in the database");
		while(guildRow.hasNext()) {
			try {
				tempRow = guildRow.next();
				if(tempRow.getCell(0).getStringCellValue().equals(guild.getName())) {
					System.out.println("Found the guild in the database!");
					return tempRow.getRowNum();
				}
			}catch(Exception e) {
				
			}
		}
		System.out.println("Guild does not exist in database");
		return -1;
	}
	
	public static boolean savePrefixes() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("SERVER_SETTINGS/GuildPrefixes.xlsx");
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find/open file: SERVER_SETTINGS/GuildPrefixes.xlsx");
			return false;
		}
        try {
			PrefixHandler.workBook.write(fileOut);
		} catch (IOException e) {
			System.out.println("Could not write to the workbook: GuildPrefixes");
			return false;
		}
        try {
			fileOut.close();
		} catch (IOException e) {
			System.out.println("Could not close file: SERVER_SETTINGS/GuildPrefixes.xlsx");
			return false;
		}
		return true;
	}
	
	public static boolean close(JDA jda) {
		try {
			inputStream.close();
			workBook.close();
		} catch (IOException e) {
			System.out.println("Could not successfully close workbook: GuildPrefixes");
			return false;
		}
		return true;
	}
}
