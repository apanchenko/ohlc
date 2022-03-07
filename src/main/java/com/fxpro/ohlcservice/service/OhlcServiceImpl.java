package com.fxpro.ohlcservice.service;

import com.fxpro.ohlcservice.Ohlc;
import com.fxpro.ohlcservice.OhlcPeriod;
import com.fxpro.ohlcservice.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OhlcService implementation
 * @author Anton Panchenko
 */
@RequiredArgsConstructor
@Service
public class OhlcServiceImpl implements OhlcService {

    private final OhlcDao ohlcDao;

    /**
     * Map instrument to current OHLCs for each period
     */
    public final Map<Long, Ohlc[]> currentCandles = new ConcurrentHashMap<>();

    /**
     * Latest non persisted OHLC
     **/
    @Override
    public @Nullable Ohlc getCurrent(long instrumentId, OhlcPeriod period) {
        return Optional.ofNullable(currentCandles.get(instrumentId))
            .map(candles -> candles[period.ordinal()])
            .orElse(null);
    }

    /**
     * All OHLCs which are kept in a database
     **/
    @Override
    public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
        return ohlcDao.getHistorical(instrumentId, period);
    }

    /**
     * Latest non persisted OHLC and OHLCs which are kept in a database
     **/
    @Override
    public List<Ohlc> getHistoricalAndCurrent(long instrumentId, OhlcPeriod period) {
        var historical = getHistorical(instrumentId, period);
        return Optional.ofNullable(getCurrent(instrumentId, period))
            .map(current -> {
                List<Ohlc> all = new ArrayList<>(1 + historical.size());
                all.add(current);
                all.addAll(historical);
                return all;
            })
            .orElse(historical);
    }

    /**
     * Receive a new quote
     * Stores current OHLC and starts a new one if period changed
     * or updates current OHLC if period is the same
     */
    @Override
    public void onQuote(Quote quote) {
        currentCandles.compute(quote.getInstrumentId(), (instrumentId, candles) -> {
            // new instrument
            if (candles == null) {
                candles = new Ohlc[OhlcPeriod.values().length];
            }
            // create or update OHLCs for each period
            for (var period : OhlcPeriod.values()) {
                int index = period.ordinal();
                var ohlc = candles[index];
                long start = period.start(quote.getUtcTimestamp());

                // period changed - save current candle
                if (ohlc != null && ohlc.getPeriodStart() != start) {
                    synchronized (ohlcDao) {
                        ohlcDao.store(ohlc);
                    }
                    ohlc = null;
                }

                // start a new candle
                if (ohlc == null) {
                    candles[index] = new Ohlc(instrumentId, quote.getPrice(), period, start);
                }
                // update existing candle
                else {
                    ohlc.update(quote.getPrice());
                }
            }
            return candles;
        });
    }
}
