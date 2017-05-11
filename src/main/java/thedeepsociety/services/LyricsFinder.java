package thedeepsociety.services;

public interface LyricsFinder {
    Lyrics find(String artist, String song);
    String getName();
}