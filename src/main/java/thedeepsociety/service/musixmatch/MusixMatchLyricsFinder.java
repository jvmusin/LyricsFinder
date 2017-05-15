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
    public MusixMatchLyricsFinder(MusixMatch musixMatch, @SuppressWarnings("SpringJavaAutowiringInspection") CacheManager cm) {
        this.musixMatch = musixMatch;
        cache = cm.getCache(cacheName);
    }

    @Override
    public Lyrics find(String artist, String track) {
        try {
            Lyrics result = fromCache(artist, track);
            if (result != null)
                return result;

            Track matchingTrack = musixMatch.getMatchingTrack(track, artist);
            TrackData trackData = matchingTrack.getTrack();
            if (trackData.getHasLyrics() == 0) {
                cache(artist, track, null);
                return null;
            }

            org.jmusixmatch.entity.lyrics.Lyrics lyrics = musixMatch.getLyrics(trackData.getTrackId());
            log.info(() -> "Successfully found for " + artist + " " + track);

            result = Lyrics.builder()
                    .artist(trackData.getArtistName())
                    .track(trackData.getTrackName())
                    .text(lyrics.getLyricsBody())
                    .source(getName())
                    .build();
            cache(artist, track, result);
            return result;
        } catch (MusixMatchException e) {
            log.throwing(MusixMatchLyricsFinder.class.toString(), "find(String, String)", e);
            cache(artist, track, null);
            return null;
        }
    }

    private void cache(String artist, String track, Lyrics lyrics) {
        cache.put(cacheKey(artist, track), lyrics);
    }

    private Lyrics fromCache(String artist, String track) {
        Cache.ValueWrapper cached = cache.get(cacheKey(artist, track));
        if (cached == null)
            return null;
        return (Lyrics) cached.get();
    }

    private String cacheKey(String artist, String track) {
        return artist + " - " + track;
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