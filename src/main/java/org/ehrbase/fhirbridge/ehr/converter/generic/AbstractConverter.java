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

package org.ehrbase.fhirbridge.ehr.converter.generic;

import com.nedap.archie.rm.archetyped.FeederAudit;
import com.nedap.archie.rm.archetyped.FeederAuditDetails;
import com.nedap.archie.rm.datavalues.DvIdentifier;
import org.ehrbase.client.classgenerator.interfaces.RMEntity;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <S> source type
 * @param <T> target type
 * @since 1.0.0
 */
public abstract class AbstractConverter<S extends Resource, T extends RMEntity> implements RMEntityConverter<S, T> {

    public static final String DEFAULT_SYSTEM_ID = "FHIR-Bridge";

    protected FeederAudit buildFeederAudit(@NonNull S resource) {
        FeederAudit result = new FeederAudit();
        String systemId = resource.getMeta().hasSource() ? resource.getMeta().getSource() : DEFAULT_SYSTEM_ID;
        result.setOriginatingSystemAudit(new FeederAuditDetails(systemId, null, null, null, null, null, null));
        if (resource.hasId()) {
            DvIdentifier identifier = new DvIdentifier();
            identifier.setId(resource.getId());
            identifier.setType("fhir_logical_id");
            List<DvIdentifier> identifiers = new ArrayList<>();
            identifiers.add(identifier);
            result.setOriginatingSystemItemIds(identifiers);
        }
        return result;
    }
}
