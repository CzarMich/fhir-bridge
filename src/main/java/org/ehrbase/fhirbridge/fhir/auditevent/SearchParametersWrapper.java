package org.ehrbase.fhirbridge.fhir.auditevent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.TokenParam;
import org.openehealth.ipf.commons.ihe.fhir.FhirSearchParameters;

import java.util.List;
import java.util.Set;

public class SearchParametersWrapper implements FhirSearchParameters {

    private final FhirContext fhirContext;

    private final SearchParameterMap searchParameters;

    public SearchParametersWrapper(FhirContext fhirContext, SearchParameterMap searchParameters) {
        this.fhirContext = fhirContext;
        this.searchParameters = searchParameters;
    }

    public SearchParameterMap getSearchParameters() {
        return searchParameters;
    }

    @Override
    public SortSpec getSortSpec() {
        return searchParameters.getSort();
    }

    @Override
    public Set<Include> getIncludeSpec() {
        return searchParameters.getIncludes();
    }

    @Override
    public List<TokenParam> getPatientIdParam() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FhirContext getFhirContext() {
        return fhirContext;
    }
}
