package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;

/**
 * Models a Board Game Geek boardgame status elements
 */
public class CollectionStatus {

    @Attribute(name = "own", required = false)
    private String own;

    @Attribute(name = "prevowned", required = false)
    private String prevowned;

    @Attribute(name = "fortrade", required = false)
    private String fortrade;

    @Attribute(name = "want", required = false)
    private String want;

    @Attribute(name = "wanttoplay", required = false)
    private String wanttoplay;

    @Attribute(name = "wanttobuy", required = false)
    private String wanttobuy;

    @Attribute(name = "wishlist", required = false)
    private String wishlist;

    @Attribute(name = "preordered", required = false)
    private String preordered;

    @Attribute(name = "lastmodified", required = false)
    private String lastmodified;

    public String getOwn() {
        return own;
    }

    public String getPrevowned() {
        return prevowned;
    }

    public String getFortrade() {
        return fortrade;
    }

    public String getWant() {
        return want;
    }

    public String getWanttoplay() {
        return wanttoplay;
    }

    public String getWanttobuy() {
        return wanttobuy;
    }

    public String getWishlist() {
        return wishlist;
    }

    public String getPreordered() {
        return preordered;
    }

    public String getLastmodified() {
        return lastmodified;
    }
}
