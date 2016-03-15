/**
 * 
 */
package org.opentripplanner.time_domain;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author owenx148
 *
 */
public abstract class TimeDomain implements Serializable{

    private static final long serialVersionUID = -2907528691412409360L;
    private static final Logger LOG = LoggerFactory.getLogger(TimeDomain.class);

    /**
     * @param t The time (in epoch milliseconds) at which to evaluate the time domain
     * @return True if the time domain includes the given time, false otherwise
     */

    public abstract boolean isActiveAtTime(long t);
    
    static TimeDomain parseTimeDomainString(String td) {        
        try {
            TimeDomainComponents components = splitComponents(td);
        } catch (UnsupportedTimeDomainTypeException e) {
            LOG.warn(e.getMessage());
            return null;
        }
        
       return null;
    }
    
    protected static TimeDomainComponents splitComponents(String td) throws UnsupportedTimeDomainTypeException {
        td = td.replaceAll("\\s+", ""); // strip whitespace
        td = td.substring(1, td.length() - 1); // remove square braces
        
        String first, second;
        Pattern p = Pattern.compile("\\(([thm[0-9]]+)\\)\\{([thm[0-9]]+)\\}");
        Matcher m = p.matcher(td);
        if (!m.find()) {
            throw new UnsupportedTimeDomainTypeException("Only day-of-week time domains are currently supported (" + td + ")");
        }
        first = m.group(1);
        second = m.group(2);
        
        TimeDomainComponents splitComponents = new TimeDomainComponents(first, second);
        return splitComponents;
    }
    
}
