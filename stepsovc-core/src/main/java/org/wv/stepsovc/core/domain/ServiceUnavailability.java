package org.wv.stepsovc.core.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class ServiceUnavailability implements Comparable<ServiceUnavailability>{
    @JsonProperty
    private String unavailableReason;
    @JsonProperty
    private String fromDate;
    @JsonProperty
    private String toDate;

    public ServiceUnavailability() {
    }

    public ServiceUnavailability(String unavailableReason, String fromDate, String toDate) {
        this.unavailableReason = unavailableReason;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }

    public void setUnavailableReason(String unavailableReason) {
        this.unavailableReason = unavailableReason;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    @Override
    public int compareTo(ServiceUnavailability serviceUnavailability) {
        return fromDate.compareTo(serviceUnavailability.fromDate) == 0 ?
            serviceUnavailability.toDate.compareTo(toDate) : fromDate.compareTo(serviceUnavailability.fromDate);
    }
}
