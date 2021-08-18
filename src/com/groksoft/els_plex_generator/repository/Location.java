package com.groksoft.els_plex_generator.repository;

public class Location
{
    /**
     * The location, drive:[\path] or mount point.
     */
    public String location;

    /**
     * The Minimum space available limit, scaled value.
     * <p>
     * See: Utils.getScaledValue and Utils.formatLong
     */
    public String minimum;

}
