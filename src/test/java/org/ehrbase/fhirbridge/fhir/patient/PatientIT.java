package org.ehrbase.fhirbridge.fhir.patient;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.nedap.archie.rm.archetyped.FeederAudit;
import org.apache.commons.io.IOUtils;
import org.ehrbase.fhirbridge.comparators.CustomTemporalAcessorComparator;
import org.ehrbase.fhirbridge.ehr.converter.PatientCompositionConverter;
import org.ehrbase.fhirbridge.ehr.opt.geccopersonendatencomposition.GECCOPersonendatenComposition;
import org.ehrbase.fhirbridge.ehr.opt.geccopersonendatencomposition.definition.*;
import org.ehrbase.fhirbridge.fhir.AbstractMappingTestSetupIT;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.clazz.ValueObjectDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link org.hl7.fhir.r4.model.Patient Patient} resource.
 */
class PatientIT extends AbstractMappingTestSetupIT {

    public PatientIT() {
        super("Patient/", Patient.class);
    }

    @Test
    void createPatient() throws IOException {
        create("create-patient.json");
    }

    @Test
    void mappingPatient() throws IOException {
        Patient patient = (Patient) super.testFileLoader.loadResource("create-patient.json");
        PatientCompositionConverter patientCompositionConverterConverter = new PatientCompositionConverter();
        GECCOPersonendatenComposition mappedGeccoPersonendatenComposition = patientCompositionConverterConverter.toComposition(patient);
        Diff diff = compareCompositions(getJavers(), "paragon-GECCO-patient-mapping-output.json", mappedGeccoPersonendatenComposition);

        assertEquals(0, diff.getChanges().size());
    }

    @Test
    void createInvalid() throws IOException {
        String resource = super.testFileLoader.loadResourceToString("create-patient-invalid.json");
        ICreateTyped createTyped = client.create().resource(resource.replaceAll(PATIENT_ID_TOKEN, PATIENT_ID));
        Exception exception = Assertions.assertThrows(UnprocessableEntityException.class, createTyped::execute);
        assertEquals("HTTP 422 : https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age: mindestens erforderlich = Extension.extension, aber nur gefunden Extension.extension:dateTimeOfDocumentation", exception.getMessage());
        //NOTE old message was more informative, but the one above addresses the correct issue
        //assertEquals("HTTP 422 : Extension.extension:dateTimeOfDocumentation: minimum required = 1, but only found 0 (from https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age)", exception.getMessage());
    }

    @Test
    void createWithDefaultProfile() throws IOException {
        String resource = super.testFileLoader.loadResourceToString("create-patient-with-default-profile.json");
        ICreateTyped createTyped = client.create().resource(resource.replaceAll(PATIENT_ID_TOKEN, PATIENT_ID));
        Exception exception = Assertions.assertThrows(UnprocessableEntityException.class, createTyped::execute);

        assertEquals("HTTP 422 : Default profile is not supported for Patient. One of the following profiles is expected: " +
                "[https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient]", exception.getMessage());
    }

    @Override
    public Exception executeMappingException(String path) throws IOException {
        Patient patient = (Patient) testFileLoader.loadResource(path);
        return assertThrows(UnprocessableEntityException.class, () ->
            new PatientCompositionConverter().toComposition((patient))
        );
    }

    @Override
    public Javers getJavers() {
        return JaversBuilder.javers()
                .registerValue(TemporalAccessor.class, new CustomTemporalAcessorComparator())
                .registerValueObject(new ValueObjectDefinition(GECCOPersonendatenComposition.class, List.of("location")))
                .registerValueObject((PersonendatenAdminEntry.class))
                .registerValueObject((AlterObservation.class))
                .registerValueObject((EthnischerHintergrundCluster.class))
                .registerValueObject((EthnischerHintergrundDefiningCode.class))
                .registerValueObject((DatenZurGeburtCluster.class))
                .registerValueObject((GeschlechtEvaluation.class))
                .registerValueObject((AngabenZumTodCluster.class))
                .registerValueObject((Period.class))
                .build();
    }
}