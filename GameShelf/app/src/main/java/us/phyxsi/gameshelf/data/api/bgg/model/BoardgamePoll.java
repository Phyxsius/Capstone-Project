package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Models a Board Game Geek boardgame poll elements
 */

@Root(name = "poll", strict = false)
public class BoardgamePoll {

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "title", required = false)
    private String title;

    @Attribute(name = "totalvotes", required = false)
    private int sortindex;

    @ElementList(name = "results", required = false, inline = true)
    private List<BoardgameResults> results;

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getSortindex() {
        return sortindex;
    }

    public List<BoardgameResults> getResults() {
        return results;
    }
}
