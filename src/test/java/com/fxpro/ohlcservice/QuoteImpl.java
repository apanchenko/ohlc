package com.fxpro.ohlcservice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Implement Quote for testing purposes
 * @author Anton Panchenko
 */
@Getter
@RequiredArgsConstructor
public class QuoteImpl implements Quote {

    private final double price;
    private final long instrumentId;
    private final long utcTimestamp;
}
