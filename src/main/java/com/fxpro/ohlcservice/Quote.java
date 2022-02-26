package com.fxpro.ohlcservice;

public interface Quote {
    double getPrice();
    long getInstrumentId();
    long getUtcTimestamp();
}
