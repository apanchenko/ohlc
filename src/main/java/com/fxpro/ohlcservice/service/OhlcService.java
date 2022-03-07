package com.fxpro.ohlcservice.service;

import com.fxpro.ohlcservice.Ohlc;
import com.fxpro.ohlcservice.OhlcPeriod;

import java.util.List;

public interface OhlcService extends QuoteListener {
    /**
     * Latest non persisted OHLC
     **/
    Ohlc getCurrent(long instrumentId, OhlcPeriod period);

    /**
     * All OHLCs which are kept in a database
     **/
    List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period);

    /**
     * Latest non persisted OHLC and OHLCs which are kept in a database
     **/
    List<Ohlc> getHistoricalAndCurrent(long instrumentId, OhlcPeriod period);
}