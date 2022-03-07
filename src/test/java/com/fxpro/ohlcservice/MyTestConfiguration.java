package com.fxpro.ohlcservice;

import com.fxpro.ohlcservice.service.OhlcDao;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
             * Map instrument and period to list of OHLCs.
             * Use linked list for fast adding first element
             */
            private final Map<Key, LinkedList<Ohlc>> storage = new HashMap<>();

            @Override
            public void store(Ohlc ohlc) {
                storage.compute(
                    new Key(ohlc.getInstrumentId(), ohlc.getPeriod()),
                    (key, ohlcList) -> {
                        if (ohlcList == null) {
                            ohlcList = new LinkedList<>();
                        }
                        ohlcList.addFirst(ohlc);
                        return ohlcList;
                    }
                );
            }

            @Override
            public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
                var key = new Key(instrumentId, period);
                return Optional.ofNullable(storage.get(key))
                    .map(Collections::unmodifiableList)
                    .orElseGet(List::of);
            }
        };
    }
}