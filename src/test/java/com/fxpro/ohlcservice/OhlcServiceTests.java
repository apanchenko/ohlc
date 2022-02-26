package com.fxpro.ohlcservice;

import com.fxpro.ohlcservice.service.OhlcService;
import com.fxpro.ohlcservice.service.OhlcServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.fxpro.ohlcservice.OhlcPeriod.M1;
import static com.fxpro.ohlcservice.OhlcPeriod.H1;
import static com.fxpro.ohlcservice.OhlcPeriod.D1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

/**
 * OhlcService tests
 * @author Anton Panchenko
 */
@SpringBootTest
class OhlcServiceTests {

	@Autowired
	OhlcService ohlcService;

	@Test
	void contextLoads() {
	}

	@Test
	void testNoQuotesCurrentOhlcIsNull() {
		var ohlcService = new OhlcServiceImpl();
		// Given no Quotes passed
		// When
		var current = ohlcService.getCurrent(1L, M1);
		// Then
		assertThat(current)
			.as("without any quotes current OHLC is null")
			.isNull();
	}

	@ParameterizedTest
	@EnumSource(OhlcPeriod.class)
	void testCurrentOhlc(OhlcPeriod period) {
		// Given
		var quote = new QuoteImpl(0.0, 0L, 0L);
		// When
		ohlcService.onQuote(quote);
		// Then
		assertThat(ohlcService.getCurrent(0L, period))
			.as("correct latest non persisted OHLC on period %s", period)
			.isNotNull()
			.returns(0.0, from(Ohlc::getOpenPrice))
			.returns(0.0, from(Ohlc::getHighPrice))
			.returns(0.0, from(Ohlc::getLowPrice))
			.returns(0.0, from(Ohlc::getClosePrice));
	}

	@ParameterizedTest
	@EnumSource(OhlcPeriod.class)
	void testCurrentOhlcAnotherInstrument(OhlcPeriod period) {
		// Given
		var quote = new QuoteImpl(0.0, 1L, 0L);
		// When
		ohlcService.onQuote(quote);
		// Then
		assertThat(ohlcService.getCurrent(0L, period))
			.as("another instrument OHLC should be null on period %s", period)
			.isNull();
	}
}
