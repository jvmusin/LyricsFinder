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
    private static final String NO_LYRICS_TEXT = "NOT FOUND";
    private static final Lyrics NO_LYRICS = Lyrics.builder().text(NO_LYRICS_TEXT).build();

    private final MusixMatch musixMatch;
    private final Cache cache;

    @Autowired
    public MusixMatchLyricsFinder(MusixMatch musixMatch, @SuppressWarnings("SpringJavaAutowiringInspection") CacheManager cm) {
        this.musixMatch = musixMatch;
        this.cache = cm.getCache(cacheName);
    }

    @Override
    public Lyrics find(String artist, String track) {
        String fullTrackName = cacheKey(artist, track);
        try {
            Lyrics result = fromCache(fullTrackName);
            if (isNoLyrics(result))
                return successFromCache(fullTrackName, result);

            Track matchingTrack = musixMatch.getMatchingTrack(track, artist);
            TrackData trackData = matchingTrack.getTrack();
            if (trackData.getHasLyrics() == 0) {
                cache.put(fullTrackName, NO_LYRICS);
                return fail(fullTrackName, "hasLyrics = false");
            }

            org.jmusixmatch.entity.lyrics.Lyrics lyrics = musixMatch.getLyrics(trackData.getTrackId());

            result = Lyrics.builder()
                    .artist(trackData.getArtistName())
                    .track(trackData.getTrackName())
                    .text(lyrics.getLyricsBody())
                    .source(getName())
                    .build();
            cache.put(fullTrackName, result);
            return successWithQuery(fullTrackName, result);
        } catch (MusixMatchException e) {
            cache.put(fullTrackName, NO_LYRICS);
            return fail(fullTrackName, e.getMessage());
        } catch (Exception e) {
            return fail(fullTrackName, e.getMessage());
        }
    }

    private Lyrics successFromCache(String fullTrackName, Lyrics lyrics) {
        boolean noLyrics = isNoLyrics(lyrics);
        log.info("Successfully found in cache for " + fullTrackName + (noLyrics ? " (no lyrics)" : ""));
        return noLyrics ? null : lyrics;
    }

    private Lyrics successWithQuery(String fullTrackName, Lyrics lyrics) {
        log.info("Successfully found with query for " + fullTrackName);
        return lyrics;
    }

    private Lyrics fail(String fullTrackName, String reason) {
        log.warning("Not found for " + fullTrackName + ": " + reason);
        return null;
    }

    private Lyrics fromCache(String fullTrackName) {
        Cache.ValueWrapper cached = cache.get(fullTrackName);
        if (cached == null) return null;
        return (Lyrics) cached.get();
    }

    private boolean isNoLyrics(Lyrics lyrics) {
        return NO_LYRICS.equals(lyrics);
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