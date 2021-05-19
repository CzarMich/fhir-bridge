package org.ehrbase.fhirbridge.fhir.support;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.ehr.EhrStatus;
import com.nedap.archie.rm.generic.PartySelf;
import com.nedap.archie.rm.support.identification.GenericId;
import com.nedap.archie.rm.support.identification.PartyRef;
import org.ehrbase.client.aql.parameter.ParameterValue;
import org.ehrbase.client.aql.query.Query;
import org.ehrbase.client.aql.record.Record1;
import org.ehrbase.client.openehrclient.OpenEhrClient;
import org.ehrbase.fhirbridge.fhir.common.Profile;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Encounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Resources {

    public static final String RFC_4122_SYSTEM = "urn:ietf:rfc:4122";

    private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

    private Resources() {
    }

    public static void addIdentifier(Identifier identifier, Resource resource) {
        if (resource instanceof Condition) {
            ((Condition) resource).addIdentifier(identifier);
        } else if (resource instanceof DiagnosticReport) {
            ((DiagnosticReport) resource).addIdentifier(identifier);
        } else if (resource instanceof Observation) {
            ((Observation) resource).addIdentifier(identifier);
        } else if (resource instanceof Patient) {
            ((Patient) resource).addIdentifier(identifier);
        } else if (resource instanceof Procedure) {
            ((Procedure) resource).addIdentifier(identifier);
        }
    }

    public static List<String> getProfileUris(Resource resource) {
        return resource.getMeta().getProfile()
                .stream()
                .map(CanonicalType::getValue)
                .collect(Collectors.toUnmodifiableList());
    }

    public static Optional<Identifier> getSubjectIdentifier(Resource resourceopenEhrClient) {
        return getSubjectIdentifier(resourceopenEhrClient, Optional.empty(), Optional.empty());
    }


    public static Optional<Identifier> getSubjectIdentifier(Resource resource, Optional<OpenEhrClient> openEhrClient, Optional<PatientIdRepository> patientIdRepository) {
        Identifier subjectIdentifier = null;

        if (resource instanceof Condition) {
            subjectIdentifier = ((Condition) resource).getSubject().getIdentifier();
        } else if (resource instanceof Consent) {
            subjectIdentifier = ((Consent) resource).getPatient().getIdentifier();
        } else if (resource instanceof DiagnosticReport) {
            subjectIdentifier = ((DiagnosticReport) resource).getSubject().getIdentifier();
        } else if (resource instanceof MedicationStatement) {
            subjectIdentifier = ((MedicationStatement) resource).getSubject().getIdentifier();
        } else if (resource instanceof Observation) {
            subjectIdentifier = ((Observation) resource).getSubject().getIdentifier();
        } else if (resource instanceof Patient) {
            subjectIdentifier = ((Patient) resource).getIdentifier()
                    .stream()
                    .filter(identifier -> Objects.equals(identifier.getSystem(), RFC_4122_SYSTEM))
                    .findFirst()
                    .orElse(null);
        } else if (resource instanceof Procedure) {
            subjectIdentifier = ((Procedure) resource).getSubject().getIdentifier();
        } else if (resource instanceof QuestionnaireResponse) {
            subjectIdentifier = getQuestionnaireId((QuestionnaireResponse) resource, openEhrClient, patientIdRepository);
        } else if (resource instanceof Encounter) {
            subjectIdentifier = getEncounterIdentifier((Encounter) resource, openEhrClient);
        }

        return Optional.ofNullable(subjectIdentifier);
    }

    private static Identifier getEncounterIdentifier(Encounter resource, Optional<OpenEhrClient> openEhrClient) {

        if (openEhrClient.isEmpty()) {
            throw new InternalErrorException("getSubjectIdentifier by Encounter was called without a configured openEHRClient as parameter. Please add one.");
        }

        // @formatter:off
        Query<Record1<UUID>> query = Query.buildNativeQuery(
                "SELECT e/ehr_id/value " +
                        "FROM ehr e " +
                        "WHERE e/ehr_status/subject/external_ref/id/value = $subject", UUID.class);
        // @formatter:on

        List<Record1<UUID>> result = openEhrClient.get().aqlEndpoint().execute(query, new ParameterValue<>("subject", resource.getSubject().getIdentifier().getValue()));

        LOG.debug("Subject ID from Encounter: " + resource.getSubject().getIdentifier().getValue());

        // create ehr if patient not exist
        if (result.isEmpty()) {
            return createEHRWithSubjectID(openEhrClient.get(), resource.getSubject().getIdentifier().getValue());
        } else {
            return resource.getSubject().getIdentifier();
        }
    }

    private static Identifier createEHRWithSubjectID(OpenEhrClient openEhrClient, String patientID) {

        PartySelf subject = new PartySelf();
        PartyRef externalRef = new PartyRef();
        externalRef.setType("PERSON");
        externalRef.setNamespace("SmICSTests");
        GenericId genericId = new GenericId();
        genericId.setScheme("id_scheme");
        genericId.setValue(patientID);
        externalRef.setId(genericId);
        subject.setExternalRef(externalRef);
        DvText dvText = new DvText("any EHR status");
        EhrStatus ehrStatus = new EhrStatus("openEHR-EHR-ITEM_TREE.generic.v1", dvText, subject, true, true, null);
        UUID ehrId = openEhrClient.ehrEndpoint().createEhr(ehrStatus);
        Identifier identifier = new Identifier();
        identifier.setValue(genericId.getValue());
        return identifier;
    }

    public static Identifier getQuestionnaireId(QuestionnaireResponse resource, Optional<OpenEhrClient> openEhrClient, Optional<PatientIdRepository> patientIdRepository) {
        if (openEhrClient.isEmpty()) {
            throw new InternalErrorException("getSubjectIdentifier was called without a confugred openEHRClient as parameter. Please add one.");
        }
        if (patientIdRepository.isEmpty()) {
            throw new InternalErrorException("PatientIdRepository is required");
        }

        if (resource.getQuestionnaire().contains("http://fhir.data4life.care/covid-19/r4/Questionnaire/covid19-recommendation|")) {
            return createQuestionnaireEHRAndReturnPatientId(openEhrClient.get(), patientIdRepository.get());
        } else {
            return resource.getSubject().getIdentifier();
        }
    }


    private static Identifier createQuestionnaireEHRAndReturnPatientId(OpenEhrClient openEhrClient, PatientIdRepository patientIdRepository) {
        PatientId patientId = patientIdRepository.save(new PatientId());
        PartySelf subject = new PartySelf();
        PartyRef externalRef = new PartyRef();
        externalRef.setType("PARTY_REF");
        externalRef.setNamespace("patients");
        GenericId genericId = new GenericId();
        genericId.setScheme("id_scheme");
        genericId.setValue(patientId.getUuidAsString());
        externalRef.setId(genericId);
        subject.setExternalRef(externalRef);
        DvText dvText = new DvText("any EHR status");
        EhrStatus ehrStatus = new EhrStatus("openEHR-EHR-ITEM_TREE.generic.v1", dvText, subject, true, true, null);
        UUID ehrId = openEhrClient.ehrEndpoint().createEhr(ehrStatus);
        LOG.debug("EhrID: " + ehrId.toString());
        Identifier identifier = new Identifier();
        identifier.setValue(genericId.getValue());
        return identifier;
    }

    public static boolean hasProfile(Resource resource, Profile profile) {
        return hasAnyProfile(resource, profile);
    }

    public static boolean hasAnyProfile(Resource resource, Profile... profiles) {
        Set<String> c1 = resource.getMeta().getProfile()
                .stream()
                .map(PrimitiveType::getValue)
                .collect(Collectors.toSet());
        Set<String> c2 = Arrays.stream(profiles)
                .map(Profile::getUri)
                .collect(Collectors.toSet());

        return !Collections.disjoint(c1, c2);
    }
}
