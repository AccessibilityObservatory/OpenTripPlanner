package org.opentripplanner.time_domain;

@SuppressWarnings("serial")
public class UnsupportedTimeDomainTypeException extends Exception {
    public UnsupportedTimeDomainTypeException(String message) {
        super(message);
    }
}