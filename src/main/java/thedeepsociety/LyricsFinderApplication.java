package thedeepsociety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thedeepsociety.services.Lyrics;
import thedeepsociety.services.LyricsFinder;

@RestController
@SpringBootApplication
public class LyricsFinderApplication {

    private final LyricsFinder[] lyricsFinders;

    @Autowired
    public LyricsFinderApplication(LyricsFinder[] lyricsFinders) {
        this.lyricsFinders = lyricsFinders;
    }

    @GetMapping("/lyrics")
    public Lyrics getLyrics(@RequestParam("artist") String artist, @RequestParam("track") String track) {
        return lyricsFinders[0].find(artist, track);
    }

	public static void main(String[] args) {
		SpringApplication.run(LyricsFinderApplication.class, args);
	}
}
