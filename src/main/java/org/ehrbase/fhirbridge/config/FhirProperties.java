package org.ehrbase.fhirbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fhir-bridge.fhir")
public class FhirProperties {

    private final Pageable pageable = new Pageable();

    public Pageable getPageable() {
        return pageable;
    }

    public static class Pageable {

        /**
         * Default page size.
         */
        private int defaultPageSize = 20;

        /**
         * Maximum page size to be accepted.
         */
        private int maxPageSize = 1000;

        public int getDefaultPageSize() {
            return defaultPageSize;
        }

        public void setDefaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
    }
}
