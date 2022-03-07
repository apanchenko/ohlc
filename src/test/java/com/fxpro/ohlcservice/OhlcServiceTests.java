package com.fxpro.ohlcservice;

import com.fxpro.ohlcservice.service.OhlcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
	void testNextOhlcOpened() {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, 0));
		ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, M1.getMilliseconds())); // next minute
		// When
		var currentOhlcM1 = ohlcService.getCurrent(instrumentId, M1);
		// Then
		assertThat(currentOhlcM1.getOpenPrice())
			.as("next period started")
			.isEqualTo(2.0);
	}

	@Test
	void testCurrentH1Period() {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, 0));
		ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, M1.getMilliseconds())); // next minute
		// When
		var currentOhlcH1 = ohlcService.getCurrent(instrumentId, H1);
		// Then
		assertThat(currentOhlcH1)
			.as("current H1 period is correct while M1 period changed")
			.returns(0L, from(Ohlc::getPeriodStart))
			.returns(1.0, from(Ohlc::getLowPrice))
			.returns(2.0, from(Ohlc::getHighPrice));
	}

	@Test
	void testCurrentD1Period() {
		// Given
		var instrumentId = 0L;
		ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, 1646653866000L)); // Mon Mar 07 2022 11:51:06
		ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, 1646657466000L)); // Mon Mar 07 2022 12:51:06
		// When
		var currentOhlcD1 = ohlcService.getCurrent(instrumentId, D1);
		// Then
		assertThat(currentOhlcD1)
			.as("current D1 period is correct while H1 period changed")
			.returns(1646611200000L, from(Ohlc::getPeriodStart)) // Mon Mar 07 2022 00:00:00
			.returns(1.0, from(Ohlc::getLowPrice))
			.returns(2.0, from(Ohlc::getHighPrice));
	}

	@Test
	void testGetHistoricalOneInstrument() {
		// Given
		var instrumentId = 0L;
		LongStream.range(0, 200) // 200 minutes
			.map(t -> t * M1.getMilliseconds())
			.forEach(t -> {
				ohlcService.onQuote(new QuoteImpl(1.0, instrumentId, t));
				ohlcService.onQuote(new QuoteImpl(2.0, instrumentId, t));
			});
		// When
		var historicalM = ohlcService.getHistorical(instrumentId, M1);
		var historicalH = ohlcService.getHistorical(instrumentId, H1);
		var historicalD = ohlcService.getHistorical(instrumentId, D1);
		// Then
		assertThat(historicalM).hasSize(199); // 199 minutes saved + 1 current
		assertThat(historicalH).hasSize(3);   // 3 hours saved
		assertThat(historicalD).hasSize(0);   // 1 day current

		Stream.concat(historicalM.stream(), historicalH.stream())
			.forEach(ohlc ->
				assertThat(ohlc)
					.returns(instrumentId, from(Ohlc::getInstrumentId))
					.returns(1.0, from(Ohlc::getOpenPrice))
					.returns(2.0, from(Ohlc::getHighPrice))
			);
	}

	@Test
	void testGetHistoricalManyInstruments() {
		// Given
		int quotes = 200;
		int instruments = 10;
		LongStream.range(0, quotes)
			.mapToObj(t -> new QuoteImpl(1.0, t % instruments, t * M1.getMilliseconds()))
			.forEach(quote -> ohlcService.onQuote(quote));
		// When
		var historicalM = ohlcService.getHistorical(0, M1);
		// Then
		assertThat(historicalM)
			.hasSize(quotes / instruments - 1)
			.isUnmodifiable();
	}

	@Test
	void testGetAllOneInstrument() {
		// Given
		int quotes = 200;
		LongStream.range(0, quotes)
			.mapToObj(t -> new QuoteImpl(1, 0, t * M1.getMilliseconds()))
			.forEach(quote -> ohlcService.onQuote(quote));
		// When
		var allM1 = ohlcService.getHistoricalAndCurrent(0, M1);
		// Then
		assertThat(allM1)
			.as("total historical and current OHLC count is correct")
			.hasSize(quotes)
			.as("total OHLC sorted by periodStart descending")
			.isSortedAccordingTo(Comparator.comparing(Ohlc::getPeriodStart).reversed());
	}

	@Test
	void testSpeedSingleThread() {
		// Given
		long start = System.currentTimeMillis();
		int quotes = 1_000_000;
		int instruments = 1000;
		LongStream.range(0, quotes)
			.mapToObj(t -> new QuoteImpl(
				t % instruments,
				t % instruments,
				t * M1.getMilliseconds() / quotes
			))
			.forEach(quote -> ohlcService.onQuote(quote));
		// When
		var spent = System.currentTimeMillis() - start;
		System.out.printf("%d quotes processed in %d ms", quotes, spent);
		var all = ohlcService.getHistoricalAndCurrent(0, M1);
		// Then
		assertThat(spent)
			.as("processed sync %d quotes in less than a minute", quotes)
			.isLessThan(M1.getMilliseconds());
		assertThat(all)
			.as("all OHLCs by one instrument is one candle")
			.hasSize(1);
	}

	@Test
	void testMultiThreadSinglePeriod() {
		// Given
		int quoteCount = 10_000_000;
		int instrumentCount = 1000;
		int threadCount = 10;
		int quotesPerThread = quoteCount / threadCount;
		var threads = IntStream.range(0, threadCount)
			.mapToObj(i ->
				new Thread(() ->
					LongStream.range(0, quotesPerThread)
						.mapToObj(q -> new QuoteImpl(1, q % instrumentCount, M1.getMilliseconds() * q / quotesPerThread))
						.forEach(quote -> ohlcService.onQuote(quote))
				)
			)
			.collect(Collectors.toList());
		// When
		long start = System.currentTimeMillis();
		threads.forEach(Thread::start);
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		var spent = System.currentTimeMillis() - start;
		System.out.printf("%d quotes processed by %d threads in %d ms", quoteCount, threadCount, spent);
		// Then
		assertThat(spent)
			.isLessThan(M1.getMilliseconds());
	}

	/**
	 * By requirements, we assume:
	 *   "utcTimestamp always coming in correct order (from the past to the future) per instrument"
	 * so one instrument should come in the same thread
	 * or else this assumption will be violated.
	 */
	@Test
	void testMultiThreadManyPeriods() {
		// Given
		int quoteCount = 10_000_000;
		int instrumentCount = 100; // one thread per instrument
		var threads = IntStream.range(0, instrumentCount)
			.mapToObj(instrument ->
				new Thread(() ->
					LongStream.range(0, quoteCount / instrumentCount)
						.mapToObj(q -> new QuoteImpl(1, instrument, M1.getMilliseconds() * q))
						.forEach(quote -> ohlcService.onQuote(quote))
				)
			)
			.collect(Collectors.toList());
		// When
		long start = System.currentTimeMillis();
		threads.forEach(Thread::start);
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		var spent = System.currentTimeMillis() - start;
		var all = ohlcService.getHistoricalAndCurrent(0, M1);
		// Then
		System.out.printf("%d quotes processed by %d threads in %d ms", quoteCount, instrumentCount, spent);
		assertThat(all)
			.as("all OHLCs by one instrument")
			.hasSize(quoteCount / instrumentCount)
			.as("all OHLC sorted by periodStart descending")
			.isSortedAccordingTo(Comparator.comparing(Ohlc::getPeriodStart).reversed());
	}
}
