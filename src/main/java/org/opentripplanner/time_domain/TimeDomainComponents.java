package org.opentripplanner.time_domain;

public class TimeDomainComponents {
    public String startString;
    public String durationString;
    
    public TimeDomainComponents(String start, String duration) {
        this.startString = start;
        this.durationString = duration;
    }
    
    @Override
    public String toString() {
        return "(start: "+startString+", duration: "+durationString+")";
    }
}