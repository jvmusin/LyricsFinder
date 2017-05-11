package thedeepsociety.services;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Lyrics {
    public final String artist;
    public final String track;
    public final String text;
    public final String source;
}