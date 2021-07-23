package org.ehrbase.fhirbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {@link ConfigurationProperties ConfigurationProperties} to configure general FHIR Bridge properties.
 */
@Component
@ConfigurationProperties(prefix = "fhir-bridge")
public class FhirBridgeProperties {

    private final Debug debug = new Debug();

    private SearchMode searchMode = SearchMode.DATABASE;

    public Debug getDebug() {
        return debug;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public boolean isDatabaseSearch() {
        return searchMode == SearchMode.DATABASE;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public static class Debug {

        private boolean enabled = false;

        private String mappingOutputDirectory;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMappingOutputDirectory() {
            return mappingOutputDirectory;
        }

        public void setMappingOutputDirectory(String mappingOutputDirectory) {
            this.mappingOutputDirectory = mappingOutputDirectory;
        }
    }

    public enum SearchMode {

        DATABASE, OPENEHR
    }
}
