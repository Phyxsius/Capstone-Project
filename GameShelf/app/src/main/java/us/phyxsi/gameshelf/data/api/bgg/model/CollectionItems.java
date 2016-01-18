package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Models a Board Game Geek boardgame items elements
 */
@Root(name = "items", strict = false)
public class CollectionItems {

    @Attribute(name = "totalitems", required = false)
    private String totalitems;

    @ElementList(name = "item", inline = true)
    private List<CollectionItem> itemList;

    public String getTotalitems() {
        return totalitems;
    }

    public List<CollectionItem> getItemList() {
        return itemList;
    }


    public CollectionItems() { super(); }
//    public CollectionItems(String totalitems) { this.totalitems = totalitems; }
}
