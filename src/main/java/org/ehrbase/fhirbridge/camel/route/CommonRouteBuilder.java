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

package org.ehrbase.fhirbridge.camel.route;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.ObjectHelper;
import org.ehrbase.client.classgenerator.interfaces.CompositionEntity;
import org.ehrbase.client.openehrclient.VersionUid;
import org.ehrbase.fhirbridge.camel.CamelConstants;
import org.ehrbase.fhirbridge.camel.processor.EhrLookupProcessor;
import org.ehrbase.fhirbridge.camel.processor.FhirProfileValidator;
import org.ehrbase.fhirbridge.camel.processor.FindPatientOpenEhrProcessor;
import org.ehrbase.fhirbridge.camel.processor.PatientReferenceProcessor;
import org.ehrbase.fhirbridge.camel.processor.ProvideResourceAuditHandler;
import org.ehrbase.fhirbridge.camel.processor.ResourcePersistenceProcessor;
import org.ehrbase.fhirbridge.config.FhirBridgeProperties;
import org.hl7.fhir.r4.model.ResourceType;
import org.openehealth.ipf.commons.ihe.fhir.Constants;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * {@link RouteBuilder} implementation that configures common routes applicable to all resources.
 *
 * @since 1.2.0
 */
@Component
public class CommonRouteBuilder extends RouteBuilder {

    private final FhirBridgeProperties properties;

    public CommonRouteBuilder(FhirBridgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off

        from("direct:provideResource")
            .routeId("provideResourceRoute")
            .onCompletion()
                .process(ProvideResourceAuditHandler.BEAN_ID)
            .end()
            .process(FhirProfileValidator.BEAN_ID)
            .process(PatientReferenceProcessor.BEAN_ID)
            .process(ResourcePersistenceProcessor.BEAN_ID)
            .process(EhrLookupProcessor.BEAN_ID)
            .doTry()
                .to("bean:fhirResourceConversionService?method=convert(${headers.CamelFhirBridgeProfile}, ${body})")
                .process(exchange -> {
                    if (ObjectHelper.isNotEmpty(exchange.getIn().getHeader(CamelConstants.COMPOSITION_ID))) {
                        String compositionId = exchange.getIn().getHeader(CamelConstants.COMPOSITION_ID, String.class);
                        exchange.getIn().getBody(CompositionEntity.class).setVersionUid(new VersionUid(compositionId));
                    }
                })
                .to("ehr-composition:compositionProducer?operation=mergeCompositionEntity")
            .doCatch(Exception.class)
                .throwException(UnprocessableEntityException.class, "${exception.message}")
            .end()
            .process("provideResourceResponseProcessor");

        from("direct:findResource")
            .routeId("findResourceRoute")
            .choice()
                .when(isDatabaseSearch())
                    .process(ResourcePersistenceProcessor.BEAN_ID)
                .when(isPatientOpenEhrSearch())
                    .process(FindPatientOpenEhrProcessor.BEAN_ID)
                .otherwise()
                    .throwException(new NotImplementedOperationException("Search using openEHR is only supported by Patient resource"));

        // @formatter:on
    }

    private Predicate isDatabaseSearch() {
        return exchange -> properties.isDatabaseSearch();
    }

    private Predicate isPatientOpenEhrSearch() {
        return exchange -> {
            RequestDetails requestDetails = exchange.getIn().getHeader(Constants.FHIR_REQUEST_DETAILS, RequestDetails.class);
            return !properties.isDatabaseSearch() && Objects.equals(requestDetails.getResourceName(), ResourceType.Patient.name());
        };
    }
}
