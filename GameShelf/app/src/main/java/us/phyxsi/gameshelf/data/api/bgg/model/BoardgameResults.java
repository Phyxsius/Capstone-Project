package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Models a Board Game Geek boardgame results elements
 */

@Root(name = "results", strict = false)
public class BoardgameResults {

    @Attribute(name = "numplayers", required = false)
    private String numplayers;

    @ElementList(name = "result", inline = true)
    private List<BoardgameResult> resultList;

    public String getNumplayers() {
        return numplayers;
    }

    public List<BoardgameResult> getResultList() {
        return resultList;
    }

}
