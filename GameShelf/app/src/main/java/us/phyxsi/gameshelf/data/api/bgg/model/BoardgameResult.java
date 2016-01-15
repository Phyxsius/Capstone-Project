package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Models a Board Game Geek boardgame result elements
 */

@Root(name = "result")
public class BoardgameResult {

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "level", required = false)
    private String level;

    @Attribute(name = "numvotes", required = false)
    private int numvotes;

    public String getValue() {
        return value;
    }

    public String getLevel() {
        return level;
    }

    public int getNumvotes() {
        return numvotes;
    }

}
