package com.fxpro.ohlcservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Candlestick in OHLC format
 * @author Anton Panchenko
 */
@Getter
@Setter
@ToString
public class Ohlc {
    private final double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private final OhlcPeriod period;
    /**
     * UTC timestamp in milliseconds when this OHLC has been started
     */
    private final long periodStart;

    public Ohlc(double price, OhlcPeriod period, long periodStart) {
        this.openPrice = price;
        this.highPrice = price;
        this.lowPrice = price;
        this.closePrice = price;
        this.period = period;
        this.periodStart = periodStart;
    }

    public void update(double price) {
        highPrice = Math.max(highPrice, price);
        lowPrice = Math.min(lowPrice, price);
        closePrice = price;
    }
}
