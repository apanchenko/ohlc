package com.fxpro.ohlcservice.service;

import com.fxpro.ohlcservice.Ohlc;
import com.fxpro.ohlcservice.OhlcPeriod;

import java.util.List;

/** Already implemented by your co-workers */
interface OhlcDao {

    void store(Ohlc ohlc);

    /**
     * Loads OHLCs from DB selected by parameters and sorted by
     * periodStartUtcTimestamp in descending order
     **/
    List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period);
}