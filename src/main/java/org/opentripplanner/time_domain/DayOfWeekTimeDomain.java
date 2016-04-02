package org.opentripplanner.time_domain;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class DayOfWeekTimeDomain extends TimeDomain {
    
    private static final Logger LOG = LoggerFactory.getLogger(DayOfWeekTimeDomain.class);
    
    private Set<DayOfWeek> activeDays;
    private OffsetTime startTime; // milliseconds past midnight
    private Duration duration; // milliseconds
    private ZoneOffset zoneOffset;

    private int startHour;
    private int startMinute;
    private int durationHour;
    private int durationMinute;
    
    public DayOfWeekTimeDomain(Set<DayOfWeek> activeDays, int startHour, int startMinute, int durationHour, int durationMinute) {
        this(activeDays, startHour, startMinute, durationHour, durationMinute, 0);
    }
    
    public DayOfWeekTimeDomain(Set<DayOfWeek> activeDays, int startHour, int startMinute, int durationHour, int durationMinute, int zoneOffsetMinutes) {
        this.activeDays = activeDays;
        this.zoneOffset = ZoneOffset.ofTotalSeconds(zoneOffsetMinutes * 60);
        this.startTime = OffsetTime.of(startHour, startMinute, 0, 0, this.zoneOffset);
        this.duration = Duration.ofHours(durationHour).plusMinutes(durationMinute);
        
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.durationMinute = durationMinute;
        this.durationHour = durationHour;
    }
    
    public void setZoneOffsetMinutes(int zoneOffsetMinutes) {
        this.zoneOffset = ZoneOffset.ofTotalSeconds(zoneOffsetMinutes * 60);
        this.startTime = OffsetTime.of(startHour, startMinute, 0, 0, this.zoneOffset);
        this.duration = Duration.ofHours(durationHour).plusMinutes(durationMinute);
    }

    @Override
    public boolean isActiveAtTime(long t) {
        Instant queryTime = Instant.ofEpochMilli(t);
        OffsetDateTime queryDT = queryTime.atOffset(this.zoneOffset);
        DayOfWeek dow = queryDT.getDayOfWeek();
        LOG.info("Query datetime is {} ({})", queryDT, dow);
        
        if (this.activeDays.contains(dow)) {
            // localize the time domain to the query date
            OffsetDateTime startDT = startTime.atDate(queryDT.toLocalDate());
            OffsetDateTime endDT = startDT.plus(duration);
            
            if ((queryDT.isEqual(startDT) || queryDT.isAfter(startDT)) && 
                (queryDT.isEqual(endDT)   || queryDT.isBefore(endDT)) ) {
                return true;
            }
            
        }
        
        // If today's domain isn't active, check yesterday's in case it crosses midnight to now
        if (this.activeDays.contains(dow.minus(1))) {
            OffsetDateTime startDT = startTime.atDate(queryDT.toLocalDate().minusDays(1));
            OffsetDateTime endDT = startDT.plus(duration);
            
            if ((queryDT.isEqual(startDT) || queryDT.isAfter(startDT)) && 
                (queryDT.isEqual(endDT)   || queryDT.isBefore(endDT)) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public Set<DayOfWeek> getActiveDays() {
        return this.activeDays;
    }
    
    public static DayOfWeekTimeDomain fromComponents(TimeDomainComponents c) throws TimeDomainParseErrorException {
        return fromComponents(c, 0);
    }

    public static DayOfWeekTimeDomain fromComponents(TimeDomainComponents c, int zoneOffsetMinutes) throws TimeDomainParseErrorException {
        String startString = c.startString;
        String durationString = c.durationString;
        
        Pattern pDOW = Pattern.compile("t\\d");
        Pattern pH = Pattern.compile("h\\d{1,2}+");
        Pattern pM = Pattern.compile("m\\d{1,2}+");
        Pattern pD = Pattern.compile("d\\d{1,2}+");
        
        Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
        String match;
        int matches;
        int rawDOW;
        int adjustedDOW;
        int startHour = 0;
        int startMinute = 0;
        int durationHour = 0;
        int durationMinute = 0;
        int durationDay = 0;
        
        // Parse start component
        //      Parse days of week
        Matcher mDOW = pDOW.matcher(startString);
        while (mDOW.find()) {
            match = startString.substring(mDOW.start(), mDOW.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            rawDOW = Integer.parseInt(match);
            adjustedDOW = rawDOW - 1;
            if (adjustedDOW == 0) adjustedDOW = 7;
            daysOfWeek.add(DayOfWeek.of(adjustedDOW));
        }
        
        //      Parse hours
        Matcher mH = pH.matcher(startString);
        matches = 0;
        while (mH.find()) {
            matches++;
            match = startString.substring(mH.start(), mH.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            startHour = Integer.parseInt(match);
        }
        if (matches > 1) {
            throw new TimeDomainParseErrorException("Multiple hour components in time domain: " + c.toString());
        }
        
        //      Parse minutes
        Matcher mM = pM.matcher(startString);
        matches = 0;
        while (mM.find()) {
            matches++;
            match = startString.substring(mM.start(), mM.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            startMinute = Integer.parseInt(match);
        }
        if (matches > 1) {
            throw new TimeDomainParseErrorException("Multiple minute components in time domain: " + c.toString());
        }
        
        
        // Parse duration component (only hour and/or minute supported) 
        //      Parse hours
        mH = pH.matcher(durationString);
        matches = 0;
        while (mH.find()) {
            matches++;
            match = durationString.substring(mH.start(), mH.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            durationHour = Integer.parseInt(match);
        }
        if (matches > 1) {
            throw new TimeDomainParseErrorException("Multiple hour components in time domain: " + c.toString());
        }
        
        //      Parse minutes
        mM = pM.matcher(durationString);
        matches = 0;
        while (mM.find()) {
            matches++;
            match = durationString.substring(mM.start(), mM.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            durationMinute = Integer.parseInt(match);
        }
        if (matches > 1) {
            throw new TimeDomainParseErrorException("Multiple minute components in time domain: " + c.toString());
        }
        
        //      Parse minutes
        Matcher mD = pD.matcher(durationString);
        matches = 0;
        while (mD.find()) {
            matches++;
            match = durationString.substring(mD.start(), mD.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            durationDay = Integer.parseInt(match);
        }
        if (matches > 1) {
            throw new TimeDomainParseErrorException("Multiple day components in time domain: " + c.toString());
        }
        
        DayOfWeekTimeDomain ret;
        // Check duration
        // (start hour/min of 0 is OK because that means midnight)
        if ((durationMinute > 0 || durationHour > 0) && durationDay == 0) {
            ret = new DayOfWeekTimeDomain(daysOfWeek, startHour, startMinute, durationHour, durationMinute, zoneOffsetMinutes);
        } else if (durationMinute == 0 && durationHour == 0 && durationDay == 1 && startHour == 0 && startMinute == 0) {
            ret = new DayOfWeekTimeDomain(daysOfWeek, startHour, startMinute, 24, 0, zoneOffsetMinutes);
        } else {
            throw new TimeDomainParseErrorException("Unsupported time domain: " + c.toString());
        }
        
        return ret;
    }

}
