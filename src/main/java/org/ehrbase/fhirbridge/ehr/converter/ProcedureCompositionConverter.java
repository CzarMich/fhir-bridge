package org.ehrbase.fhirbridge.ehr.converter;

import com.nedap.archie.rm.datavalues.DvIdentifier;
import com.nedap.archie.rm.generic.PartyIdentified;
import com.nedap.archie.rm.generic.PartySelf;
import org.ehrbase.client.classgenerator.shareddefinition.Language;
import org.ehrbase.fhirbridge.ehr.opt.prozedurcomposition.ProzedurComposition;
import org.ehrbase.fhirbridge.ehr.opt.prozedurcomposition.definition.CareflowStepDefiningCode;
import org.ehrbase.fhirbridge.ehr.opt.prozedurcomposition.definition.CurrentStateDefiningCode;
import org.ehrbase.fhirbridge.ehr.opt.prozedurcomposition.definition.DetailsZurKoerperstelleCluster;
import org.ehrbase.fhirbridge.ehr.opt.prozedurcomposition.definition.ProzedurAction;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ProcedureCompositionConverter extends AbstractCompositionConverter<Procedure, ProzedurComposition> {

    @Override
    public ProzedurComposition convert(@NonNull Procedure procedure) {
        ProzedurComposition result = new ProzedurComposition();

        Coding code = procedure.getCode().getCoding().get(0);

        DateTimeType performed = procedure.getPerformedDateTimeType();

        Coding bodySite = null;
        List<CodeableConcept> bodySites = procedure.getBodySite();
        if (!bodySites.isEmpty()) {
            CodeableConcept bodySiteCodes = bodySites.get(0); // could be empty
            if (bodySiteCodes != null) {
                bodySite = bodySiteCodes.getCoding().get(0);
            }
        }

        Annotation note = null;
        List<Annotation> notes = procedure.getNote();
        if (!notes.isEmpty()) {
            note = procedure.getNote().get(0); // could be empty
        }

        ProzedurAction action = new ProzedurAction();
        action.setTimeValue(performed.getValueAsCalendar().toZonedDateTime());
        action.setNameDerProzedurValue(code.getDisplay());

        if (note != null) {
            action.setFreitextbeschreibungValue(note.getText());
        }

        // anatomical location
        if (bodySite != null) {
            DetailsZurKoerperstelleCluster anatomicalLocationCluster = new DetailsZurKoerperstelleCluster();

            // mapping
            anatomicalLocationCluster.setNameDerKoerperstelleValue(bodySite.getDisplay());

            action.setDetailsZurKoerperstelle(new ArrayList<>());
            action.getDetailsZurKoerperstelle().add(anatomicalLocationCluster);
        }

        // mandatory ism_transition

        action.setCareflowStepDefiningCode(CareflowStepDefiningCode.PROZEDUR_BEENDET);
        action.setCurrentStateDefiningCode(CurrentStateDefiningCode.COMPLETED);

        // mandatory subject
        action.setSubject(new PartySelf());

        // mandatory langauge
        action.setLanguage(Language.DE);


        result.setProzedur(action);

        // ======================================================================================
        // Required fields by API

        result.setStartTimeValue(performed.getValueAsCalendar().toZonedDateTime());

        // https://github.com/ehrbase/ehrbase_client_library/issues/31
        PartyIdentified composer = new PartyIdentified();
        DvIdentifier identifier = new DvIdentifier();
        identifier.setId(procedure.getRecorder().getReference()); // TODO: if there is no recorder, try with the performer
        composer.addIdentifier(identifier);
        result.setComposer(composer);

        mapDefaultAttributes(procedure, result);

        return result;
    }
}
