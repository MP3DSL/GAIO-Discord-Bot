package music;

import java.awt.Color;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import commandhandler.Command;
import commandhandler.Command.ExecutorType;
import gaiobot.BotListener;
import gaiobot.Ref;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class MusicCommands {
	
	private static Guild guild;
	private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();
	private static Map<Guild, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();
	
	public MusicCommands() {
		AudioSourceManagers.registerRemoteSources(manager);
	}
	
	private static AudioPlayer createPlayer(Guild g) {
		guild = g;
		manager.setTrackStuckThreshold(10000);
		AudioPlayer p = manager.createPlayer();
		TrackManager m = new TrackManager(p);
		p.addListener(m);
		guild.getAudioManager().setSendingHandler(new PlayerSendHandler(p));
		
		players.put(g, new AbstractMap.SimpleEntry<>(p, m));
		
		return p;
	}
	
	private static boolean hasPlayer(Guild g) {
		return players.containsKey(g);
	}
	
	private static AudioPlayer getPlayer(Guild g) {
		if(hasPlayer(g)) {
			return players.get(g).getKey();
		}
		else {
			return createPlayer(g);
		}
	}
	
	private TrackManager getManager(Guild g) {
		return players.get(g).getValue();
	}
	
	private boolean isIdle(Guild g) {
		return !hasPlayer(g) || getPlayer(g).getPlayingTrack() == null;
	}
	
	private void loadTrack(String identifier, Member author, Message msg) {
		Guild guild = author.getGuild();
		getPlayer(guild);	
		manager.setFrameBufferDuration(Ref.musicBuffer); //Changed Depending on Internet Speed (kb/s)
		manager.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				if(identifier.contains("twitch.tv")) {
					msg.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setThumbnail("https://pbs.twimg.com/profile_images/832351122635382784/mbcECY96_400x400.jpg").setDescription("The stream \"" + track.getInfo().title + "\" was added to the playlist!").build()).queue();
					msg.getTextChannel().sendMessage(identifier).queue();
					getManager(guild).queue(track, author);
				}
				else{
					boolean inQueue = false;
					List<String> tracks = new ArrayList<String>();
					getManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));
					for(int x = 0; x < tracks.size(); x++) {
						if(tracks.get(x).replaceAll(" ", "").contains(track.getInfo().title.replaceAll(" ", ""))) {
							inQueue = true;
							break;
						}
					}
					if(!inQueue) {
						msg.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("The song \"" + "[" + getTimestamp(track.getDuration()) + "] " + track.getInfo().title + "\" was added to the playlist!").build()).queue();
						getManager(guild).queue(track, author);
					}
					else
						msg.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Song \"" + track.getInfo().title + "\" is already in Queue!\n").build()).queue();
				}
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				EmbedBuilder builder = new EmbedBuilder().setColor(Color.GREEN).setTitle("***" + playlist.getName() + " Playlist:***");
				if(identifier.contains("ytsearch: ")) {
					boolean inQueue = false;
					List<String> tracks = new ArrayList<String>();
					getManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));
					for(int x = 0; x < tracks.size(); x++) {
						if(tracks.get(x).replaceAll(" ", "").contains(playlist.getTracks().get(0).getInfo().title.replaceAll(" ", ""))) {
							inQueue = true;
							break;
						}
					}
					builder.setTitle(null);
					if(!inQueue) {
						getManager(guild).queue(playlist.getTracks().get(0), author);
						builder.setDescription("The song \"" + playlist.getTracks().get(0).getInfo().title + "\" was added to the playlist!");
					}
					else {
						builder.setColor(Color.RED);
						builder.setDescription(builder.getDescriptionBuilder() + "Song \"" + playlist.getTracks().get(0).getInfo().title + "\" is already in Queue!\n");
					}
				}
				else {
					for(int i = 0; i < (playlist.getTracks().size() > Ref.playlistLimit ? Ref.playlistLimit : playlist.getTracks().size()); i++){
						boolean inQueue = false;
						List<String> tracks = new ArrayList<String>();
						getManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));
						for(int x = 0; x < tracks.size(); x++) {
							if(tracks.get(x).replaceAll(" ", "").contains(playlist.getTracks().get(i).getInfo().title.replaceAll(" ", ""))) {
								System.out.println("TRUE");
								inQueue = true;
								break;
							}
						}
						if(!inQueue) {
							builder.setDescription(builder.getDescriptionBuilder() + "[" + getTimestamp(playlist.getTracks().get(i).getDuration()) + "] " + playlist.getTracks().get(i).getInfo().title + "\n");
							getManager(guild).queue(playlist.getTracks().get(i), author);
						}
						else {
							builder.setDescription(builder.getDescriptionBuilder() + "Song \"" + playlist.getTracks().get(i).getInfo().title + "\" is already in Queue!\n");
						}
					}
				}
				msg.getTextChannel().sendMessage(builder.build()).queue();
			}
			
			@Override
			public void noMatches() {
				sendErrorMsg(null, msg.getTextChannel(), "The song \"" + identifier + "\" was not found :sob:");
				return;
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				sendErrorMsg(null, msg.getTextChannel(), "It's impossible to play the song(s) because of this reason: " + exception.getLocalizedMessage());
				return;
			}
		});
	}
	
	public static void skip(Guild g) {
		getPlayer(g).stopTrack();
	}
	
	private String getTimestamp(long milis) {
		long seconds = milis/ 1000;
		long hours = Math.floorDiv(seconds, 3600);
		seconds = seconds - (hours * 3600);
		long mins = Math.floorDiv(seconds,  60);
		seconds = seconds - (mins * 60);
		return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
	}
	
	private String buildQueueMessage(AudioInfo info) {
		AudioTrackInfo trackInfo = info.getTrack().getInfo();
		String title = trackInfo.title;
		long length = trackInfo.length;
		
		return "*[" + getTimestamp(length) + "]* " + title + "\n";
	}
	
	private void sendErrorMsg(User user, TextChannel tc, String content) {
		BotListener.noMsg = false;
		if(content.equals("help")) {
			if(!user.hasPrivateChannel())
				user.openPrivateChannel().complete();
			EmbedBuilder builder = new EmbedBuilder();
			builder.setColor(Color.CYAN)
					.setTitle("***VALID MUSIC COMMANDS***")
					.setDescription("Here's a list of the available music argument commands!\nExample: \"" + Ref.prefix + "music [insert argument(s)]\"")
					.addField("nowplaying or np or info", "Use this argument to see information on the current song! Provide a song number to get info on that specific song!", false)
					.addField("pause", "Use this argument to pause the current song!", false)
					.addField("play or p", "Use this argument along with a valid YouTube/Twitch/Soundcloud url to add a song or playlist to the queue! If you are searching for a song, be as specifc as possible! If the music player is paused, use this command by itself to continue playing the song that was paused.", false)
					.addField("playfirst or pf", "Use this argument to add a song to the beginning of the queue!", false)
					.addField("queue or q", "Use this argument to see the first 20 songs in the queue! If you'd like to see the rest of the queue, you can navigate through the pages by providing a page number!", false)
					.addField("remove or r", "Use this argument to remove the most recently queued song! Specifying a song number removes that specific song from the queue!", false)
					.addField("shuffle or sh", "Use this argument to shuffle the queue!", false)
					.addField("seek or s", "Use this argument to seek the current playing track to the designated point! \n***DOES NOT WORK ON LIVE STREAMS*** \nTime Format = hrs:min:sec Example: \"" + Ref.prefix + "music seek 01:20:02\" ", false)
					.addField("skip or sk", "Use this argument by itself to skip ahead 1 song! If you provide a number, the music player will skip that many number of songs ahead!", false)
					.addField("stop or st", "Use this argument to stop the current song and clear the queue!", false)
					.build();
			((UserImpl)user).getPrivateChannel().sendMessage(builder.build()).queue();
			tc.sendMessage(new EmbedBuilder().setColor(Color.red).setDescription(user.getAsMention()+", please check your pm's!").build()).queue();
			return;
		}
		tc.sendMessage(new EmbedBuilder().setColor(Color.red).setDescription(content).build()).queue();
	}
	
	@Command(name = "music", power = 0, type=ExecutorType.USER, description="Use this command in conjunction with other arguments to use the music player! For a list of available arguments, just type this command without any other arguments.")
	public void music(String[] args, TextChannel tc, Guild guild, Message message, User user) {
		if(args.length<1) {
			sendErrorMsg(user, tc, "help");
			return;
		}
		switch(args[0].toLowerCase()) {
			case "playfirst":
			case "pf":
				if(guild.getMember(user).getVoiceState().getChannel() == null) {
					sendErrorMsg(user, tc, "Please connect to a voice channel before using this command " + user.getAsMention() + "!");
					return;
				}
				if(args.length < 2) {
					sendErrorMsg(user, tc, "Please enter a valid source!");
					return;
				}
				String play = Arrays.stream(args).skip(1).map(s -> " " + s).collect(Collectors.joining()).substring(1);
				if(!(play.startsWith("http://") || play.startsWith("https://")))
					play = "ytsearch: " + play;
				loadTrack(play, message.getMember(), message);
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						getManager(guild).playFirst();
					}
				}, 2000);
				break;
			case "remove":
			case "r":
				List<AudioInfo> songList = new ArrayList<AudioInfo>();
				songList.addAll(getManager(guild).getQueue());
				int remove = songList.size() - 1;
				if(args.length>1) {
					try {
						remove = Integer.parseInt(args[1]) - 1;
					}catch(Exception e) {
						sendErrorMsg(null, tc, "Please provide a specified song number! If you don't know what number your song is, use the queue argument to search for it!");
						return;
					}
				}
				for(int i=0; i<songList.size(); i++) {
					if(remove==i) {
						getManager(guild).remove(songList.get(i));
						tc.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("Successfully removed \"" + songList.get(i).getTrack().getInfo().title + "\" from the queue!").build()).queue();
					}
				}
				break;
				
			case "seek":
			case "s":
				String[] time = args[1].split(":");
				int hours = 0;
				int mins = 0;
				int secs = 0;
				long seekTime = 0; //Milliseconds
				int k = time.length;
				switch (k) {
					case 3:
						try {
							hours = Integer.parseInt(time[0]);
							mins = Integer.parseInt(time[1]);
							secs = Integer.parseInt(time[2]);
						}catch(Exception e) {
						}
						seekTime = (hours * 3600000) + (mins * 60000) + (secs * 1000);
						break;
						
					case 2:
						System.out.println(time[0]);
						System.out.println(time[1]);
						try {
							mins = Integer.parseInt(time[0]);
							secs = Integer.parseInt(time[1]);
						}catch(Exception e) {
							e.printStackTrace();
						}
						seekTime = (mins * 60000) + (secs * 1000);
						System.out.println(seekTime);
						break;
						
					default:
						try {
							secs = Integer.parseInt(time[0]);
						}catch(Exception e) {
						}
						seekTime = (secs * 1000);
				}
				getPlayer(guild).getPlayingTrack().setPosition(seekTime);
				tc.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("Seeking current track to *[" + getTimestamp(getPlayer(guild).getPlayingTrack().getPosition()) + "]*!").build()).queue();
				break;
				
			case "pause":
				if(guild.getAudioManager().isConnected() && !getPlayer(guild).isPaused()) {
					AudioPlayer player = getPlayer(guild);
					player.setPaused(true);
					tc.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("The music player is now paused!").build()).queue();
				}
				else {
					sendErrorMsg(user, tc, "The music player is either already paused or not in a voice channel " + user.getAsMention() + "!");
					return;
				}
				
				break;
				
			case "play":
			case "p":
				if(guild.getMember(user).getVoiceState().getChannel() == null) {
					sendErrorMsg(user, tc, "Please connect to a voice channel before using this command " + user.getAsMention() + "!");
					return;
				}
				if(getPlayer(guild).isPaused()) {
					getPlayer(guild).setPaused(false);
					tc.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("The music player is now unpaused!").build()).queue();
					return;
				}
				if(args.length < 2) {
					sendErrorMsg(user, tc, "Please enter a valid source!");
					return;
				}
				String input = Arrays.stream(args).skip(1).map(s -> " " + s).collect(Collectors.joining()).substring(1);
				if(!(input.startsWith("http://") || input.startsWith("https://")))
					input = "ytsearch: " + input;
				loadTrack(input, message.getMember(), message);
				
				break;
				
			case "skip":
			case "sk":
				if(isIdle(guild))
					return;
				int num = 1;
				if(args.length > 1) {
					try {
						num = Integer.parseInt(args[1]);
					}catch(Exception e) {
						sendErrorMsg(user, tc, "Please provide a valid number so songs you want to skip!");
						return;
					}
				}
				final int songSK = num;
				for(int i = 0; i<songSK && i<getManager(guild).getQueue().size(); i++){
					Timer skipSongs = new Timer();
					skipSongs.schedule(new TimerTask() {
						@Override
						public void run() {
							skip(guild);
						}
					}, 2000);
				}
				if(getPlayer(guild).getPlayingTrack() == null)
					tc.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("There are no more songs in the queue! See ya later!").build()).queue();
				else {
					Timer nowPlaying = new Timer();
					nowPlaying.schedule(new TimerTask() {
						@Override
						public void run() {
							tc.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("Successfully skipped " + songSK + " songs!").addField("Now Playing", getPlayer(guild).getPlayingTrack().getInfo().title, false).build()).queue();
						}
					}, songSK * 50);
				}
				
				break;
				
			case "stop":
			case "st":
				if(isIdle(guild))
					return;
				getManager(guild).purgeQueue();
				skip(guild);
				tc.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Successfully stopped and cleared the queue! Adios!").build()).queue();
				
				guild.getAudioManager().closeAudioConnection();
				
				break;
				
			case "shuffle":
			case "sh":
				if(isIdle(guild))
					return;
				getManager(guild).shuffleQueue();
				tc.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("The queue has successfully been shuffled!").build()).queue();
				BotListener.noMsg = false;
				
				break;
				
			case "nowplaying":
			case "np":
			case "info":
				if(isIdle(guild))
					return;
				int songNum = 0;
				if(args.length>1) {
					try {
						songNum = Integer.parseInt(args[1])-1;
					}catch(Exception e) {
						sendErrorMsg(null, tc, "Please provide a specified song number! If you don't know what number your song is, use the queue argument to search for it!");
						return;
					}
					List<AudioInfo> songInfo = new ArrayList<AudioInfo>();
					songInfo.addAll(getManager(guild).getQueue());
					for(int i=0; i<songInfo.size(); i++) {
						if(i==songNum) {
							tc.sendMessage(
									new EmbedBuilder()
									.setColor(Color.CYAN)
									.setDescription("**TRACK INFO**")
									.addField("Title", songInfo.get(i).getTrack().getInfo().title, false)
									.addField("Duration", "[ " + getTimestamp(songInfo.get(i).getTrack().getPosition()) + " / " + getTimestamp(songInfo.get(i).getTrack().getDuration()) + " ]", false)
									.addField("Author", songInfo.get(i).getTrack().getInfo().author, false)
									.build()
							).queue();
							break;
						}
					}
					break;
				}
				AudioTrack track = getPlayer(guild).getPlayingTrack();
				AudioTrackInfo info = track.getInfo();
				
				tc.sendMessage(
						new EmbedBuilder()
							.setColor(Color.cyan)
							.setDescription("**CURRENT TRACK INFO:**")
							.addField("Title", info.title, false)
							.addField("Duration", "[ " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getDuration()) + " ]", false)
							.addField("Author", info.author, false)
							.build())
				.queue();
				
				break;
			
			case "queue":
			case "q":
				if(isIdle(guild))
					return;
				int page = 1;
				if(args.length == 2) {
					try {
						page = Integer.parseInt(args[1]);
						if(page<1) {
							sendErrorMsg(user, tc, "Please provide a number greater than 0!");
							return;
						}
					}catch(Exception e) {
						sendErrorMsg(user, tc, "Please provide a number greater than 0!");
						return;
					}
				}
				List<String> tracks = new ArrayList<String>();
				List<String> trackSublist;
				
				getManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));
				
				int pageAll = tracks.size()/20;
				if(tracks.size()>20)
					pageAll++;
				else
					pageAll = 1;
				if(page>pageAll)
					page = pageAll;
				
				int startPage = (page-1)*20;
				int endPage = (page-1)*20+20;
				if(tracks.size() > 20) {					
					if(endPage>tracks.size())
						endPage = tracks.size();
					trackSublist = tracks.subList(startPage, endPage);
				}
				else
					trackSublist = tracks;
				
				String out = "";
				for(int i = 0; i<trackSublist.size(); i++) {
					int trackNum = 0;
					for(int x = startPage; x<tracks.size();x++) {
						if(tracks.get(x).equals(trackSublist.get(i))) {
							trackNum = x + 1;
							break;
						}
					}
					out += "\n#" + trackNum + " " + trackSublist.get(i);
				}
				
				tc.sendMessage(
						new EmbedBuilder()
						.setColor(Color.cyan)
						.setDescription("**CURRENT QUEUE:**\n" + 
								"*[" + getManager(guild).getQueue().size() + " Tracks | Page " + page + " / " + pageAll + "]*\n" +
								out
						)
						.build()
				).queue();
				
				break;
			default:
				sendErrorMsg(user, tc, "help");
				return;
		}
	}
}
