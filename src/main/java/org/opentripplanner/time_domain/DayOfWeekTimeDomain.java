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

public class DayOfWeekTimeDomain extends TimeDomain {
    
    private static final Logger LOG = LoggerFactory.getLogger(DayOfWeekTimeDomain.class);
    
    private Set<DayOfWeek> activeDays;
    private OffsetTime startTime; // milliseconds past midnight
    private Duration duration; // milliseconds
    private ZoneOffset zoneOffset;

    public DayOfWeekTimeDomain(Set<DayOfWeek> activeDays, int startHour, int startMinute, int durationHour, int durationMinute, int zoneOffsetMinutes) {
        this.activeDays = activeDays;
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

    public static DayOfWeekTimeDomain fromComponents(TimeDomainComponents c, int zoneOffsetMinutes) throws TimeDomainParseErrorException {
        String startString = "t2t3t4t5t6h11m30";
        String durationString = "h2m30";
        
        Pattern pDOW = Pattern.compile("t\\d");
        Pattern pH = Pattern.compile("h\\d{1,2}+");
        Pattern pM = Pattern.compile("m\\d{1,2}+");
        
        Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
        String match;
        int matches;
        int rawDOW;
        int adjustedDOW;
        int startHour = 0;
        int startMinute = 0;
        int durationHour = 0;
        int durationMinute = 0;
        
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
        if (matches != 1) {
            throw new TimeDomainParseErrorException("Multiple or missing hour components in time domain: " + c.toString());
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
        if (matches != 1) {
            throw new TimeDomainParseErrorException("Multiple or missing minute components in time domain: " + c.toString());
        }
        
        
        // Parse duration component (only hour and/or minute supported) 
        //      Parse hours
        mH = pH.matcher(durationString);
        matches = 0;
        while (mH.find()) {
            matches++;
            match = startString.substring(mH.start(), mH.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            durationHour = Integer.parseInt(match);
        }
        if (matches != 1) {
            throw new TimeDomainParseErrorException("Multiple or missing hour components in time domain: " + c.toString());
        }
        
        //      Parse minutes
        mM = pM.matcher(durationString);
        matches = 0;
        while (mM.find()) {
            matches++;
            match = startString.substring(mM.start(), mM.end());
            match = match.substring(1, match.length()); // strip leading character to leave only digits
            durationMinute = Integer.parseInt(match);
        }
        if (matches != 1) {
            throw new TimeDomainParseErrorException("Multiple or missing minute components in time domain: " + c.toString());
        }
        
        return new DayOfWeekTimeDomain(daysOfWeek, startHour, startMinute, durationHour, durationMinute, zoneOffsetMinutes);
        
    }

}
