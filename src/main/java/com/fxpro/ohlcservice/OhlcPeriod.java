package com.fxpro.ohlcservice;

public enum OhlcPeriod {
    /**
     * One minute, starts at 0 second of every minute
     */
    M1,

    /**
     * One hour, starts at 0:00 of every hour
     */
    H1,

    /**
     * One day, starts at 0:00:00 of every day
     */
    D1;

    public long start(long timestamp) {
        return 0L;
    }
}
