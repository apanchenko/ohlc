package com.fxpro.ohlcservice;

import lombok.Getter;

public enum OhlcPeriod {
    /**
     * One minute, starts at 0 second of every minute
     */
    M1(60_000),

    /**
     * One hour, starts at 0:00 of every hour
     */
    H1(3600_000),

    /**
     * One day, starts at 0:00:00 of every day
     */
    D1(24 * 3600_000);

    OhlcPeriod(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Getter
    private final long milliseconds;

    public long start(long timestamp) {
        return timestamp / milliseconds * milliseconds;
    }
}
