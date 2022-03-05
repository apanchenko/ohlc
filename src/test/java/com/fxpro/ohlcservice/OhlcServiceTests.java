package com.fxpro.ohlcservice;

import com.fxpro.ohlcservice.service.OhlcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import static com.fxpro.ohlcservice.OhlcPeriod.M1;
import static com.fxpro.ohlcservice.OhlcPeriod.H1;
import static com.fxpro.ohlcservice.OhlcPeriod.D1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

/**
 * OhlcService tests
 * @author Anton Panchenko
 */
@Import(MyTestConfiguration.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OhlcServiceTests {

	@Autowired
	OhlcService ohlcService;

	@Test
	void contextLoads() {
	}

	@Test
	void testCurrentOhlcNoQuotes() {
		// Given no Quotes passed
		// When
		var current = ohlcService.getCurrent(0L, M1);
		// Then
		assertThat(current)
			.as("without any quotes current OHLC is null")
			.isNull();
	}

	@ParameterizedTest
	@EnumSource(OhlcPeriod.class)
	void testCurrentOhlcSingleQuote(OhlcPeriod period) {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(0.0, instrumentId, 0L));
		// When
		var ohlc = ohlcService.getCurrent(instrumentId, period);
		// Then
		assertThat(ohlc)
			.as("correct latest non persisted OHLC on period %s", period)
			.isNotNull()
			.returns(0.0, from(Ohlc::getOpenPrice))
			.returns(0.0, from(Ohlc::getHighPrice))
			.returns(0.0, from(Ohlc::getLowPrice))
			.returns(0.0, from(Ohlc::getClosePrice));
	}

	@Test
	void testCurrentOhlcManyQuotes() {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, 0L));
		ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, 0L));
		ohlcService.onQuote(new QuoteImpl(4.0, instrumentId, 0L));
		ohlcService.onQuote(new QuoteImpl(3.0, instrumentId, 0L));
		// When
		var ohlc = ohlcService.getCurrent(instrumentId, M1);
		// Then
		assertThat(ohlc)
			.isNotNull()
			.returns(2.0, from(Ohlc::getOpenPrice))
			.returns(4.0, from(Ohlc::getHighPrice))
			.returns(1.0, from(Ohlc::getLowPrice))
			.returns(3.0, from(Ohlc::getClosePrice));
	}

	@ParameterizedTest
	@EnumSource(OhlcPeriod.class)
	void testCurrentOhlcAnotherInstrument(OhlcPeriod period) {
		// Given
		ohlcService.onQuote(new QuoteImpl(0.0, 0L, 0L));
		// When
		var ohlc = ohlcService.getCurrent(1L, period);
		// Then
		assertThat(ohlc)
			.as("another instrument OHLC should be null on period %s", period)
			.isNull();
	}

	@Test
	void testNextCandleOpened() {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, 0L));
		ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, 60_000L));
		// When
		var ohlc = ohlcService.getCurrent(instrumentId, M1);
		// Then
		assertThat(ohlc).returns(2.0, from(Ohlc::getOpenPrice));
	}
}
