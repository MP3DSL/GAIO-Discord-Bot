package music;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class TrackManager extends AudioEventAdapter {

	private final AudioPlayer player;
	private final Queue<AudioInfo> queue;
	
	public TrackManager(AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public void queue(AudioTrack track, Member author) {
		AudioInfo info = new AudioInfo(track, author);
		queue.add(info);
		
		if(player.getPlayingTrack() == null) {
			player.playTrack(track);
		}
	}
	
	public Set<AudioInfo> getQueue(){
		return new LinkedHashSet<>(queue);
	}
	
	public AudioInfo getInfo(AudioTrack track) {
		return queue.stream().filter(info -> info.getTrack().equals(track)).findFirst().orElse(null);
	}
	
	public void purgeQueue() {
		queue.clear();
	}
	
	public void shuffleQueue() {
		List<AudioInfo> cQueue = new ArrayList<>(getQueue());
		AudioInfo current = cQueue.get(0);
		cQueue.remove(0);
		Collections.shuffle(cQueue);
		cQueue.add(0, current);
		purgeQueue();
		queue.addAll(cQueue);
	}
	
	public void remove(AudioInfo track) {
		queue.remove((AudioInfo) track);
	}
	
	public void playFirst() {
		List<AudioInfo> list = new ArrayList<AudioInfo>(getQueue());
		AudioInfo pQueue = list.get(list.size()-1);
		list.remove(list.size()-1);
		list.add(1, pQueue);
		purgeQueue();
		queue.addAll(list);
		System.out.println(pQueue.getTrack().getInfo().title);
	}
	
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		AudioInfo info = queue.element();
		VoiceChannel vChan = info.getAuthor().getVoiceState().getChannel();
		
		if(vChan == null)
			player.stopTrack();
		else
			info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		Guild g = queue.poll().getAuthor().getGuild();
		
		if(queue.isEmpty())
			g.getAudioManager().closeAudioConnection();
		else
			player.playTrack(queue.element().getTrack());
	}
	
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		Guild g = queue.poll().getAuthor().getGuild();
		
		MusicCommands.skip(g);
		g.getTextChannels().get(0).sendMessage(new EmbedBuilder().setColor(Color.red).setDescription("Player is stuck!!! Skipping to next song!").build()).queue();
	}
}
