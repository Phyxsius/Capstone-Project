package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Models a Board Game Geek boardgame name elements
 */

@Root(name = "name")
public class BoardgameName {
    @Attribute(name = "primary", required = false)
    private boolean primary;

    @Attribute(name = "sortindex", required = false)
    private int sortindex;

    @Text
    private String title;

    public String getTitle() {
        return title;
    }

    public boolean isPrimary() {
        return primary;
    }
}
