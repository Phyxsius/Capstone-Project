package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Models a Board Game Geek boardgame poll elements
 */

@Root(name = "boardgamepublisher")
public class BoardgamePublisher {

    @Attribute(name = "objectid", required = false)
    private long objectId;

    @Text
    private String name;

    public long getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }
}
