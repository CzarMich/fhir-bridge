/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehrbase.fhirbridge.camel.processor;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

/**
 * Camel {@link Processor} that converts back the persisted resource with contained resources into a Bundle.
 *
 * @since 1.0.0
 */
@Component
public class BundleResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        MethodOutcome methodOutcome = exchange.getIn().getMandatoryBody(MethodOutcome.class);

        Bundle responseBundle = new Bundle()
                .setType(Bundle.BundleType.TRANSACTIONRESPONSE)
                .addEntry(
                        new Bundle.BundleEntryComponent()
                                .setResource((Resource) methodOutcome.getResource()));

        exchange.getMessage().setBody(responseBundle);
    }
}
