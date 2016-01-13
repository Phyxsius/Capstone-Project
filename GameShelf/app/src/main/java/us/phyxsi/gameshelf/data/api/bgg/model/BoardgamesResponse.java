package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Models a response from the Board Game Geek API that returns a collection of boardgames
 */
@Root(name = "boardgames")
public class BoardgamesResponse {

    @Attribute(name = "termsofuse")
    private String termsofuse;

    @ElementList(inline = true)
    public List<Boardgame> boardgames;

    public BoardgamesResponse() {
        super();
    }

    public BoardgamesResponse(String termsofuse) {
        this.termsofuse = termsofuse;
    }

}