package org.ehrbase.fhirbridge.fhir.condition;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.io.IOUtils;
import org.ehrbase.fhirbridge.comparators.CustomTemporalAcessorComparator;
import org.ehrbase.fhirbridge.ehr.converter.GECCODiagnoseCompositionConverter;
import org.ehrbase.fhirbridge.ehr.opt.geccodiagnosecomposition.GECCODiagnoseComposition;
import org.ehrbase.fhirbridge.ehr.opt.geccodiagnosecomposition.definition.*;
import org.ehrbase.fhirbridge.fhir.AbstractMappingTestSetupIT;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.clazz.ValueObjectDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link org.hl7.fhir.r4.model.Condition Condition} resource.
 */
class GECCODiagnoseIT extends AbstractMappingTestSetupIT {

    public GECCODiagnoseIT() {
        super("Condition/GECCODiagnose/", Condition.class);
    }

    @Test
    void createDiagnoseChronicLiverDisease() throws IOException {
        create("create-chronic-liver-disease.json");
    }

    @Test
    void createDiagnoseChronicLungDisease() throws IOException {
        create("create-chronic-lung-disease.json");
    }

    @Test
    void createDiagnoseDiabetesMellitus() throws IOException {
        create("create-diabetes-mellitus.json");
    }

    @Test
    void createDiagnoseMalignantNeoplasticDisease() throws IOException {
        create("create-malignant-neoplastic-disease-absent.json");
    }


    @Test
    void createDiagnoseRheumatologicalImmunologicalDiseases() throws IOException {
        create("create-rheumatological-immunological-diseases-rheumatism.json");
    }

    @Test
    void createDiagnoseHIV() throws IOException {
        create("example-human-immunodeficiency-virus-infection1.json");
    }

    @Test
    void createDiagnoseCardiovascularDiseases() throws IOException {
        create("example-cardiovascular-diseases.json");

    }

    @Test
    void createDiagnoseChronicKidneyDisease() throws IOException {
        create("example-chronic-kidney-diseases2.json");
    }

    @Test
    void createDiagnoseChronicNeurologicalMentalDiseases() throws IOException {
        create("example-chronic-neurological-mental-diseases.json");
    }

    @Test
    void createDiagnoseComplicationsCovid19() throws IOException {
        create("example-complications-covid19-0.json");
    }

    @Test
    void createDiagnoseOrganRecipient() throws IOException {
        create("example-organ-recipient.json");
    }

    @Test
    void createDiagnoseGastrointerstinalUlcers() throws IOException {
        create("example-gastrointestinal-ulcers.json");
    }

    @Test
    void createDiagnoseDependenceOnVentilator() throws IOException {
        create("dependence-on-ventilator.json");
    }

    /*
    @Test
    void mapCompletedJavers() throws IOException {
        Condition resource = (Condition)  super.testFileLoader.loadResource("create-chronic-liver-disease.json");

        GECCODiagnoseCompositionConverter compositionConverter = new GECCODiagnoseCompositionConverter();
        GECCODiagnoseComposition composition = compositionConverter.toComposition(resource);

        Diff diff = compareCompositions(getJavers(), "create-chronic-liver-disease-result.json", composition);
        assertEquals(diff.getChanges().size(), 0);
    }
    */

    @Test
    void createDiagnoseInvalidVerificationStatus() throws IOException {
        Exception exception = executeMappingException("invalid/invalid-verification-status.json");
        assertEquals("Cant identify the verification status", exception.getMessage());
    }

    @Test
    void createDiagnoseInvalidKategorie() throws IOException {
        Exception exception = executeMappingException("invalid/invalid-kategorie.json");
        assertEquals("Category not present", exception.getMessage());
    }

    @Test
    void createDiagnoseInvalidBodySite() throws IOException {
        Exception exception = executeMappingException("invalid/invalid-body-site.json");
        assertEquals("Body site not processable.", exception.getMessage());
    }

    @Test
    void createDiagnoseInvalidSeverity() throws IOException {
        Exception exception = executeMappingException("invalid/invalid-severity.json");
        assertEquals("Severity not processable.", exception.getMessage());
    }

    @Override
    public Exception executeMappingException(String path) throws IOException {
        Condition condition = (Condition) testFileLoader.loadResource(path);
        return assertThrows(UnprocessableEntityException.class, () -> {
            (new GECCODiagnoseCompositionConverter()).toComposition(condition);
        });
    }

    @Override
    public Javers getJavers() {
        return JaversBuilder.javers()
                .registerValue(TemporalAccessor.class, new CustomTemporalAcessorComparator())
                .registerValueObject(new ValueObjectDefinition(GECCODiagnoseComposition.class, List.of("location")))
                .registerValueObject(AusgeschlosseneDiagnoseEvaluation.class)
                .registerValueObject(UnbekannteDiagnoseEvaluation.class)
                .registerValueObject(VorliegendeDiagnoseEvaluation.class)
                .build();
    }
}
