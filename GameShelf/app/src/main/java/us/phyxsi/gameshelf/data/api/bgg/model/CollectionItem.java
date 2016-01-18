package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Models a Board Game Geek boardgame items elements
 */

@Root(name = "item", strict = false)
public class CollectionItem {

    @Attribute(name = "objecttype", required = false)
    private String objecttype;

    @Attribute(name = "objectid", required = false)
    private String objectid;

    @Attribute(name = "subtype", required = false)
    private String subtype;

    @Attribute(name = "collid", required = false)
    private String collid;

    @Element(name = "name", required = false)
    private BoardgameName name;

    @Element(name = "yearpublished", required = false)
    private String yearpublished;

    @Element(name = "image", required = false)
    private String image;

    @Element(name = "thumbnail", required = false)
    private String thumbnail;

    @Element(name = "numplays", required = false)
    private String numplays;

    @Element(name = "stats", required = false)
    private CollectionStats stats;

    @Element(name = "status", required = false)
    private CollectionStatus status;

    public String getObjecttype() {
        return objecttype;
    }

    public String getObjectid() {
        return objectid;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getCollid() {
        return collid;
    }

    public BoardgameName getName() {
        return name;
    }

    public String getYearpublished() {
        return yearpublished;
    }

    public String getImage() {
        return image;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getNumplays() {
        return numplays;
    }

    public CollectionStats getStats() {
        return stats;
    }

    public CollectionStatus getStatus() {
        return status;
    }
}
