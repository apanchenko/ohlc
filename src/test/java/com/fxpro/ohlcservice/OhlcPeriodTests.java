package com.fxpro.ohlcservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Anton Panchenko
 */
@SpringBootTest
class OhlcPeriodTests {

	@Test
	void contextLoads() {
	}

	@ParameterizedTest
	@CsvSource({
		"M1, 0, 0",
		"M1, 500, 0",
		"M1, 59999, 0",
		"M1, 60000, 60000",
		"M1, 1646178723123, 1646178720000",
		"H1, 3599999, 0",
		"H1, 3600000, 3600000",
		"H1, 1646478013456, 1646478000000",
		"D1, 1646478013456, 1646438400000"
	})
	void testCurrentOhlcNoQuotes(String periodName, long timestamp, long start) {
		var period = OhlcPeriod.valueOf(periodName);
		assertThat(period.start(timestamp))
			.isEqualTo(start);
	}

}
