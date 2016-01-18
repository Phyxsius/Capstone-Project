package us.phyxsi.gameshelf.data.api.bgg.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Models a Board Game Geek boardgame rating elements
 */
public class CollectionRating {

    @Attribute(name = "value", required = false)
    private String value;

    @Element(name = "usersrated", required = false)
    private long usersrated;

    @Element(name = "average", required = false)
    private float average;

    @Element(name = "bayesaverage", required = false)
    private float bayesaverage;

    @Element(name = "stddev", required = false)
    private float stddev;

    @Element(name = "median", required = false)
    private float median;
}
