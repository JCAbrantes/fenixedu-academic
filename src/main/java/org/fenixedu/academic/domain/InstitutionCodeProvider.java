package org.fenixedu.academic.domain;

public interface InstitutionCodeProvider {

    public String getEstablishmentCode();

    public String getOrganicUnitCode(DegreeCurricularPlan degreeCurricularPlan);

}
