package thedeepsociety.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Lyrics {
    private final String artist;
    private final String track;
    private final String text;
    private final String source;
}