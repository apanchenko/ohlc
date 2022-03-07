package com.fxpro.ohlcservice.service;

import com.fxpro.ohlcservice.Quote;

interface QuoteListener {
    void onQuote(Quote quote);
}