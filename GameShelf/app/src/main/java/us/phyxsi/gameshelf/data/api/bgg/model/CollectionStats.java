package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Models a Board Game Geek boardgame stats elements
 */
public class CollectionStats {

    @Attribute(name = "minplayers", required = false)
    private int minplayers;

    @Attribute(name = "maxplayers", required = false)
    private int maxplayers;

    @Attribute(name = "minplaytime", required = false)
    private int minplaytime;

    @Attribute(name = "maxplaytime", required = false)
    private int maxplaytime;

    @Attribute(name = "playingtime", required = false)
    private int playingtime;

    @Attribute(name = "numowned", required = false)
    private long numowned;

    @Element(name = "rating", required = false)
    private CollectionRating rating;

    public int getMinplayers() {
        return minplayers;
    }

    public int getMaxplayers() {
        return maxplayers;
    }

    public int getMinplaytime() {
        return minplaytime;
    }

    public int getMaxplaytime() {
        return maxplaytime;
    }

    public int getPlayingtime() {
        return playingtime;
    }

    public long getNumowned() {
        return numowned;
    }

    public CollectionRating getRating() {
        return rating;
    }
}
