package thedeepsociety.service.musixmatch;

import lombok.extern.java.Log;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import thedeepsociety.service.Lyrics;
import thedeepsociety.service.LyricsFinder;

@Service
@Log
public class MusixMatchLyricsFinder implements LyricsFinder {

    private static final String cacheName = "lyrics.musixmatch";

    private final MusixMatch musixMatch;
    private final Cache cache;

    @Autowired
    public MusixMatchLyricsFinder(MusixMatch musixMatch, CacheManager cm) {
        this.musixMatch = musixMatch;
        cache = cm.getCache(cacheName);
    }

    @Override
    @Cacheable(value = cacheName, key = "#artist + ' - ' + #track", unless = "#result == null")
    public Lyrics find(String artist, String track) {
        try {
            Track matchingTrack = musixMatch.getMatchingTrack(track, artist);
            TrackData trackData = matchingTrack.getTrack();
            if (trackData.getHasLyrics() == 0) {
                cache.put(artist + " - " + track, null);
                return null;
            }

            org.jmusixmatch.entity.lyrics.Lyrics lyrics = musixMatch.getLyrics(trackData.getTrackId());

            log.info(() -> "Successfully found for " + artist + " " + track);

            return Lyrics.builder()
                    .artist(trackData.getArtistName())
                    .track(trackData.getTrackName())
                    .text(lyrics.getLyricsBody())
                    .source(getName())
                    .build();
        } catch (MusixMatchException e) {
            log.warning(() -> "Thrown exception on " + artist + " - " + track +": " + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return "MusixMatch";
    }

    @Bean
    public static MusixMatch musixMatch(@Value("${service.musixmatch.apiKey}") String apiKey) {
        return new MusixMatch(apiKey);
    }
}