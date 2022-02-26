package com.fxpro.ohlcservice.service;

import com.fxpro.ohlcservice.Ohlc;
import com.fxpro.ohlcservice.OhlcPeriod;
import com.fxpro.ohlcservice.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OhlcService implementation
 * @author Anton Panchenko
 */
//@RequiredArgsConstructor//(onConstructor_ = @Autowired)
@Service
public class OhlcServiceImpl implements OhlcService {

    //private final OhlcDao ohlcDao;
    public final Map<Long, Ohlc[]> currentCandles = new HashMap<>();

    public OhlcServiceImpl() {
        System.out.println("created OHLC service");
    }

    @Override
    public @Nullable Ohlc getCurrent(long instrumentId, OhlcPeriod period) {
        return Optional.ofNullable(currentCandles.get(instrumentId))
            .map(candles -> candles[period.ordinal()])
            .orElse(null);
    }

    @Override
    public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
        return null;
    }

    @Override
    public List<Ohlc> getHistoricalAndCurrent(long instrumentId, OhlcPeriod period) {
        return null;
    }

    @Override
    public void onQuote(Quote quote) {
        var price = quote.getPrice();
        currentCandles.compute(quote.getInstrumentId(), (instrumentId, candles) -> {
            if (candles == null) {
                candles = new Ohlc[OhlcPeriod.values().length];
            }
            for (var period : OhlcPeriod.values()) {
                int index = period.ordinal();
                var ohlc = candles[index];
                long start = period.start(quote.getUtcTimestamp());

                // close and save candle
                if (ohlc != null && ohlc.getPeriodStart() != start) {
                    ohlc = null;
                }

                // start new candle
                if (ohlc == null) {
                    candles[index] = new Ohlc(price, period, start);
                }
                // update existing candle
                else {
                    ohlc.update(price);
                }
            }
            return candles;
        });
    }
}
