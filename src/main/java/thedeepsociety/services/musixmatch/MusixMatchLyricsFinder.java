package thedeepsociety.services.musixmatch;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import thedeepsociety.services.Lyrics;
import thedeepsociety.services.LyricsFinder;

@Service
public class MusixMatchLyricsFinder implements LyricsFinder {

    private final MusixMatch musixMatch;

    @Autowired
    public MusixMatchLyricsFinder(MusixMatch musixMatch) {
        this.musixMatch = musixMatch;
    }

    @Override
    public Lyrics find(String artist, String track) {
        try {
            Track matchingTrack = musixMatch.getMatchingTrack(track, artist);
            TrackData trackData = matchingTrack.getTrack();
            org.jmusixmatch.entity.lyrics.Lyrics lyrics = musixMatch.getLyrics(trackData.getTrackId());

            return Lyrics.builder()
                    .artist(trackData.getArtistName())
                    .track(trackData.getTrackName())
                    .text(lyrics.getLyricsBody())
                    .source(getName())
                    .build();
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return "MusixMatch";
    }

    @Bean
    public static MusixMatch musixMatch(@Value("${services.musixmatch.apiKey}") String apiKey) {
        return new MusixMatch(apiKey);
    }
}