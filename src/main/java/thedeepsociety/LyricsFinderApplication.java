package thedeepsociety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thedeepsociety.service.Lyrics;
import thedeepsociety.service.LyricsFinder;

@RestController
@SpringBootApplication
@EnableCaching
public class LyricsFinderApplication {

    private final LyricsFinder[] lyricsFinders;

    @Autowired
    public LyricsFinderApplication(LyricsFinder[] lyricsFinders) {
        this.lyricsFinders = lyricsFinders;
    }

    @GetMapping("/lyrics")
    public ResponseEntity<Lyrics> getLyrics(@RequestParam("artist") String artist, @RequestParam("track") String track) {
        artist = artist.toLowerCase();
        track = track.toLowerCase();

        Lyrics lyrics = lyricsFinders[0].find(artist, track);

        return lyrics == null
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.OK).body(lyrics);
    }

    @RequestMapping("/")
    public String info() {
        return "Use '/lyrics?artist=ARTIST_NAME&track=TRACK_NAME' to find lyrics";
    }

	public static void main(String[] args) {
		SpringApplication.run(LyricsFinderApplication.class, args);
	}
}
