package se.kry.poller.domain;

public enum ServiceStatus {
    OK, // Used when a service is found to be up
    FAIL, // Used when a service is found to be down
    UNKNOWN // Used when a service has not yet been checked
}
