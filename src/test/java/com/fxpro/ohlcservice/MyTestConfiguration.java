package com.fxpro.ohlcservice;

import com.fxpro.ohlcservice.service.OhlcDao;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class MyTestConfiguration {
    /**
     * OhlcDao implementation for testing purposes
     */
    @Bean
    OhlcDao createOhlcDao(){
        return new OhlcDao() {

            /**
             * Composite key to store OHLC
             */
            @AllArgsConstructor
            @EqualsAndHashCode
            @Getter
            class Key {
                private final long instrumentId;
                private final OhlcPeriod period;
            }

            /**
             * Map instrument and period to list of OHLCs
             */
            private Map<Key, List<Ohlc>> storage = new HashMap<>();

            @Override
            public void store(Ohlc ohlc) {
                storage.compute(
                    new Key(ohlc.getInstrumentId(), ohlc.getPeriod()),
                    (key, ohlcList) -> {
                        if (ohlcList == null) {
                            ohlcList = new ArrayList<>();
                        }
                        ohlcList.add(ohlc);
                        return ohlcList;
                    }
                );
            }

            @Override
            public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
                var key = new Key(instrumentId, period);
                return storage.getOrDefault(key, List.of());
            }
        };
    }
}