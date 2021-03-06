/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.domain.candidacyProcess.secondCycle;

import org.fenixedu.academic.domain.AcademicProgram;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicAccessRule;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicOperationType;
import org.fenixedu.academic.domain.accounting.events.candidacy.SecondCycleIndividualCandidacyEvent;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.candidacyProcess.CandidacyProcess;
import org.fenixedu.academic.domain.candidacyProcess.CandidacyProcessDocumentUploadBean;
import org.fenixedu.academic.domain.candidacyProcess.DegreeOfficePublicCandidacyHashCode;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyDocumentFile;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyDocumentFileType;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyProcessBean;
import org.fenixedu.academic.domain.candidacyProcess.IndividualCandidacyState;
import org.fenixedu.academic.domain.caseHandling.Activity;
import org.fenixedu.academic.domain.caseHandling.PreConditionNotValidException;
import org.fenixedu.academic.domain.caseHandling.StartActivity;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.domain.student.PrecedentDegreeInformation;
import org.fenixedu.academic.dto.person.PersonBean;
import org.fenixedu.bennu.core.domain.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SecondCycleIndividualCandidacyProcess extends SecondCycleIndividualCandidacyProcess_Base {

    static private List<Activity> activities = new ArrayList<>();
    static {
        activities.add(new CandidacyPayment());
        activities.add(new EditCandidacyPersonalInformation());
        activities.add(new EditCandidacyInformation());
        activities.add(new IntroduceCandidacyResult());
        activities.add(new ChangeIndividualCandidacyState());
        activities.add(new CancelCandidacy());
        activities.add(new CreateRegistration());
        activities.add(new EditPublicCandidacyPersonalInformation());
        activities.add(new EditPublicCandidacyDocumentFile());
        activities.add(new EditPublicCandidacyHabilitations());
        activities.add(new EditDocuments());
        activities.add(new BindPersonToCandidacy());
        activities.add(new ChangeProcessCheckedState());
        activities.add(new SendEmailForApplicationSubmission());
        activities.add(new RevokeDocumentFile());
        activities.add(new ChangePaymentCheckedState());
        activities.add(new RejectCandidacy());
        activities.add(new RevertApplicationToStandBy());
        activities.add(new CopyIndividualCandidacyToNextCandidacyProcess());
        activities.add(new MoveCandidacy());
    }

    private SecondCycleIndividualCandidacyProcess() {
        super();
    }

    private SecondCycleIndividualCandidacyProcess(final SecondCycleIndividualCandidacyProcessBean bean) {
        this();

        /*
         * 06/04/2009 - The checkParameters, IndividualCandidacy creation and
         * candidacy information are made in the init method
         */
        init(bean);

        /*
         * 27/04/2009 - New document files specific to SecondCycle candidacies
         */
        setSpecificIndividualCandidacyDocumentFiles(bean);

    }

    private void setSpecificIndividualCandidacyDocumentFiles(SecondCycleIndividualCandidacyProcessBean bean) {
        bindIndividualCandidacyDocumentFile(bean.getCurriculumVitaeDocument());

        for (CandidacyProcessDocumentUploadBean documentBean : bean.getHabilitationCertificateList()) {
            bindIndividualCandidacyDocumentFile(documentBean);
        }

        for (CandidacyProcessDocumentUploadBean documentBean : bean.getReportOrWorkDocumentList()) {
            bindIndividualCandidacyDocumentFile(documentBean);
        }
    }

    @Override
    protected void checkParameters(final CandidacyProcess process) {
        if (process == null || process.getCandidacyPeriod() == null) {
            throw new DomainException("error.SecondCycleIndividualCandidacyProcess.invalid.candidacy.process");
        }
    }

    @Override
    protected void createIndividualCandidacy(final IndividualCandidacyProcessBean bean) {
        new SecondCycleIndividualCandidacy(this, (SecondCycleIndividualCandidacyProcessBean) bean);
    }

    @Override
    public boolean canExecuteActivity(User userView) {
        return isAllowedToManageProcess(this, userView) || RoleType.SCIENTIFIC_COUNCIL.isMember(userView.getPerson().getUser())
                || RoleType.COORDINATOR.isMember(userView.getPerson().getUser());
    }

    @Override
    public List<Activity> getActivities() {
        return activities;
    }

    @Override
    public SecondCycleIndividualCandidacy getCandidacy() {
        return (SecondCycleIndividualCandidacy) super.getCandidacy();
    }

    private SecondCycleIndividualCandidacyProcess editCandidacyInformation(final SecondCycleIndividualCandidacyProcessBean bean) {
        getCandidacy().editCandidacyInformation(bean.getCandidacyDate(), bean.getSelectedDegreeList(),
                bean.getPrecedentDegreeInformation(), bean.getProfessionalStatus(), bean.getOtherEducation());

        editPrecedentDegreeInformation(bean);

        return this;
    }

    public Degree getCandidacySelectedDegree() {
        throw new DomainException("shouldnt be called");
        // return getCandidacy().getSelectedDegree();
    }

    public Collection<Degree> getSelectedDegrees() {
        return getCandidacy().getSelectedDegreesSet();
    }

    public boolean hasCandidacyForSelectedDegree(final Degree degree) {
        return getSelectedDegrees().contains(degree);
    }

    public String getCandidacyProfessionalStatus() {
        return getCandidacy().getProfessionalStatus();
    }

    public String getCandidacyOtherEducation() {
        return getCandidacy().getOtherEducation();
    }

    public PrecedentDegreeInformation getPrecedentDegreeInformation() {
        return getCandidacy().getRefactoredPrecedentDegreeInformation();
    }

    public Integer getCandidacyProfessionalExperience() {
        return getCandidacy().getProfessionalExperience();
    }

    public BigDecimal getCandidacyAffinity() {
        return getCandidacy().getAffinity();
    }

    public Integer getCandidacyDegreeNature() {
        return getCandidacy().getDegreeNature();
    }

    public BigDecimal getCandidacyGrade() {
        return getCandidacy().getCandidacyGrade();
    }

    public String getCandidacyInterviewGrade() {
        return getCandidacy().getInterviewGrade();
    }

    public BigDecimal getCandidacySeriesGrade() {
        return getCandidacy().getSeriesCandidacyGrade();
    }

    public String getCandidacyNotes() {
        return getCandidacy().getNotes();
    }

    @Override
    public ExecutionYear getCandidacyExecutionInterval() {
        return (ExecutionYear) super.getCandidacyExecutionInterval();
    }

    static private boolean isAllowedToManageProcess(SecondCycleIndividualCandidacyProcess process, User userView) {
        Set<AcademicProgram> programs =
                AcademicAccessRule.getProgramsAccessibleToFunction(AcademicOperationType.MANAGE_INDIVIDUAL_CANDIDACIES,
                        userView.getPerson().getUser()).collect(Collectors.toSet());

        if (process == null || process.getCandidacy() == null) {
            return false;
        }

        return !Collections.disjoint(programs, process.getCandidacy().getSelectedDegreesSet());

    }

    private void editFormerIstStudentNumber(SecondCycleIndividualCandidacyProcessBean bean) {
        this.getCandidacy().editFormerIstStudentNumber(bean);
    }

    @StartActivity
    static public class IndividualCandidacyInformation extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess dummy,
                User userView, Object object) {
            return new SecondCycleIndividualCandidacyProcess((SecondCycleIndividualCandidacyProcessBean) object);
        }
    }

    static private class CandidacyPayment extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            return process; // nothing to be done, for now payment is being
            // done by existing interface
        }
    }

    static private class EditCandidacyPersonalInformation extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            final SecondCycleIndividualCandidacyProcessBean bean = (SecondCycleIndividualCandidacyProcessBean) object;
            process.editPersonalCandidacyInformation(bean.getPersonBean());
            return process;
        }
    }

    static private class EditCandidacyInformation extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
            if (process.isCandidacyCancelled() || process.isCandidacyAccepted() || process.hasRegistrationForCandidacy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.editCandidacyHabilitations((SecondCycleIndividualCandidacyProcessBean) object);
            process.getCandidacy().editObservations((SecondCycleIndividualCandidacyProcessBean) object);
            process.editCandidacyInformation((SecondCycleIndividualCandidacyProcessBean) object);

            return process;
        }
    }

    static private class IntroduceCandidacyResult extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView) && !RoleType.COORDINATOR.isMember(userView.getPerson().getUser())) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }

            if (!process.isCandidacyDebtPayed()) {
                throw new PreConditionNotValidException();
            }

            if (!process.isSentToCoordinator()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            SecondCycleIndividualCandidacyResultBean bean = (SecondCycleIndividualCandidacyResultBean) object;
            SecondCycleIndividualCandidacySeriesGrade seriesGrade =
                    process.getCandidacy().getSecondCycleIndividualCandidacySeriesGradeForDegree(bean.getDegree());
            seriesGrade.setAffinity(bean.getAffinity());
            seriesGrade.setProfessionalExperience(bean.getProfessionalExperience());
            seriesGrade.setDegreeNature(bean.getDegreeNature());
            seriesGrade.setCandidacyGrade(bean.getGrade());
            seriesGrade.setInterviewGrade(bean.getInterviewGrade());
            seriesGrade.setSeriesCandidacyGrade(bean.getSeriesGrade());
            seriesGrade.setNotes(bean.getNotes());
            seriesGrade.setState(bean.getSeriesGradeState());
            return process;
        }
    }

    static private class CancelCandidacy extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
            if (!process.isCandidacyInStandBy() || process.hasAnyPaymentForCandidacy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.cancelCandidacy(userView.getPerson());
            return process;
        }
    }

    static private class CreateRegistration extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (!process.isCandidacyAccepted()) {
                throw new PreConditionNotValidException();
            }

            if (process.hasRegistrationForCandidacy()) {
                throw new PreConditionNotValidException();
            }

            // TODO: check if can create registration when first cycle is
            // not concluded
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            final SecondCycleIndividualCandidacyProcessBean bean = (SecondCycleIndividualCandidacyProcessBean) object;
            createRegistration(process, bean);
            return process;
        }

        private void createRegistration(final SecondCycleIndividualCandidacyProcess candidacyProcess,
                final SecondCycleIndividualCandidacyProcessBean bean) {
            candidacyProcess.getCandidacy().createRegistration(getDegreeCurricularPlan(bean), CycleType.SECOND_CYCLE,
                    IngressionType.findByPredicate(IngressionType::isInternal2ndCycleAccess).orElse(null));
        }

        private DegreeCurricularPlan getDegreeCurricularPlan(final SecondCycleIndividualCandidacyProcessBean bean) {
            return bean.getSelectedDegree().getLastActiveDegreeCurricularPlan();
        }
    }

    static private class ChangeIndividualCandidacyState extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }

            if (!process.isCandidacyDebtPayed()) {
                throw new PreConditionNotValidException();
            }

            if (!process.isSentToCoordinator() && !process.isSentToScientificCouncil()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            SecondCycleIndividualCandidacyResultBean bean = (SecondCycleIndividualCandidacyResultBean) object;
            process.getCandidacy().setState(bean.getState());
            return process;
        }

    }

    static private class EditPublicCandidacyPersonalInformation extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!process.isCandidacyInStandBy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.editPersonalCandidacyInformation(((SecondCycleIndividualCandidacyProcessBean) object).getPersonBean());
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    static private class EditPublicCandidacyDocumentFile extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!process.isCandidacyInStandBy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            CandidacyProcessDocumentUploadBean bean = (CandidacyProcessDocumentUploadBean) object;
            process.bindIndividualCandidacyDocumentFile(bean);
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    static private class EditPublicCandidacyHabilitations extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!process.isCandidacyInStandBy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            SecondCycleIndividualCandidacyProcessBean bean = (SecondCycleIndividualCandidacyProcessBean) object;
            process.editCandidacyHabilitations(bean);
            process.editFormerIstStudentNumber(bean);
            process.getCandidacy().editSelectedDegrees(bean.getSelectedDegreeList());
            process.getCandidacy().editObservations(bean);

            process.editPrecedentDegreeInformation(bean);

            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    static private class EditDocuments extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            CandidacyProcessDocumentUploadBean bean = (CandidacyProcessDocumentUploadBean) object;
            process.bindIndividualCandidacyDocumentFile(bean);
            return process;
        }
    }

    static private class BindPersonToCandidacy extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyInternal()) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }

        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            SecondCycleIndividualCandidacyProcessBean bean = (SecondCycleIndividualCandidacyProcessBean) object;

            // First edit personal information
            process.editPersonalCandidacyInformation(bean.getPersonBean());
            // Then bind to person
            process.bindPerson(bean.getChoosePersonBean());

            return process;

        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    static private class ChangeProcessCheckedState extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }

        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.setProcessChecked(((IndividualCandidacyProcessBean) object).getProcessChecked());
            return process;
        }
    }

    static private class ChangePaymentCheckedState extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (process.isCandidacyCancelled()) {
                throw new PreConditionNotValidException();
            }

        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.setPaymentChecked(((IndividualCandidacyProcessBean) object).getPaymentChecked());
            return process;
        }
    }

    @Override
    public Boolean isCandidacyProcessComplete() {
        // TODO Auto-generated method stub
        return null;
    }

    static private class SendEmailForApplicationSubmission extends Activity<SecondCycleIndividualCandidacyProcess> {
        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            DegreeOfficePublicCandidacyHashCode hashCode = (DegreeOfficePublicCandidacyHashCode) object;
            hashCode.sendEmailForApplicationSuccessfullySubmited();
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    @Override
    public List<IndividualCandidacyDocumentFileType> getMissingRequiredDocumentFiles() {
        List<IndividualCandidacyDocumentFileType> missingDocumentFiles = new ArrayList<>();

        if (getActiveFileForType(IndividualCandidacyDocumentFileType.PHOTO) == null) {
            missingDocumentFiles.add(IndividualCandidacyDocumentFileType.PHOTO);
        }

        if (getActiveFileForType(IndividualCandidacyDocumentFileType.CV_DOCUMENT) == null) {
            missingDocumentFiles.add(IndividualCandidacyDocumentFileType.CV_DOCUMENT);
        }

        if (getActiveFileForType(IndividualCandidacyDocumentFileType.HABILITATION_CERTIFICATE_DOCUMENT) == null) {
            missingDocumentFiles.add(IndividualCandidacyDocumentFileType.HABILITATION_CERTIFICATE_DOCUMENT);
        }

        if (getActiveFileForType(IndividualCandidacyDocumentFileType.DOCUMENT_IDENTIFICATION) == null) {
            missingDocumentFiles.add(IndividualCandidacyDocumentFileType.DOCUMENT_IDENTIFICATION);
        }

        if (getActiveFileForType(IndividualCandidacyDocumentFileType.PAYMENT_DOCUMENT) == null) {
            missingDocumentFiles.add(IndividualCandidacyDocumentFileType.PAYMENT_DOCUMENT);
        }

        return missingDocumentFiles;
    }

    static protected class RevokeDocumentFile extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            ((CandidacyProcessDocumentUploadBean) object).getDocumentFile().setCandidacyFileActive(Boolean.FALSE);
            return process;
        }

        @Override
        public Boolean isVisibleForAdminOffice() {
            return Boolean.FALSE;
        }

    }

    static private class RejectCandidacy extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
            if (process.isCandidacyCancelled() || !process.isCandidacyInStandBy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.rejectCandidacy(userView.getPerson());
            return process;
        }
    }

    static private class MoveCandidacy extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }
            if (process.isCandidacyCancelled() || !process.isCandidacyInStandBy()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            CandidacyProcess candidacyProcess = (CandidacyProcess) object;
            process.setCandidacyProcess(candidacyProcess);
            return process;
        }
    }

    static private class RevertApplicationToStandBy extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {
            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (!process.isCandidacyCancelled() && !process.isCandidacyRejected()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            process.getCandidacy().setState(IndividualCandidacyState.STAND_BY);

            return process;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }
    }

    @Override
    protected void executeOperationsBeforeDocumentFileBinding(IndividualCandidacyDocumentFile documentFile) {
        IndividualCandidacyDocumentFileType type = documentFile.getCandidacyFileType();

        IndividualCandidacyDocumentFile file = getActiveFileForType(type);
        if (file == null) {
            return;
        }

        if (IndividualCandidacyDocumentFileType.REPORT_OR_WORK_DOCUMENT.equals(type)) {
            return;
        }

        file.setCandidacyFileActive(false);
    }

    static private class CopyIndividualCandidacyToNextCandidacyProcess extends Activity<SecondCycleIndividualCandidacyProcess> {

        @Override
        public void checkPreConditions(SecondCycleIndividualCandidacyProcess process, User userView) {

            if (!isAllowedToManageProcess(process, userView)) {
                throw new PreConditionNotValidException();
            }

            if (!process.isCandidacyNotAccepted()) {
                throw new PreConditionNotValidException();
            }
        }

        @Override
        protected SecondCycleIndividualCandidacyProcess executeActivity(SecondCycleIndividualCandidacyProcess process,
                User userView, Object object) {
            SecondCycleIndividualCandidacyProcessBean bean = (SecondCycleIndividualCandidacyProcessBean) object;
            SecondCycleCandidacyProcess destinationCandidacyProcess = bean.getCopyDestinationProcess();

            SecondCycleIndividualCandidacyProcessBean newBean = new SecondCycleIndividualCandidacyProcessBean(process);
            newBean.setCandidacyProcess(destinationCandidacyProcess);
            newBean.setPublicCandidacyHashCode(DegreeOfficePublicCandidacyHashCode.getUnusedOrCreateNewHashCode(
                    SecondCycleIndividualCandidacyProcess.class, destinationCandidacyProcess, process.getCandidacyHashCode()
                            .getEmail()));
            newBean.setPersonBean(new PersonBean(process.getPersonalDetails()));
            newBean.setCandidacyDate(destinationCandidacyProcess.getCandidacyPeriod().getStart().toLocalDate());
            newBean.initializeDocumentUploadBeans();

            SecondCycleIndividualCandidacyProcess newProcess =
                    createNewProcess(userView, SecondCycleIndividualCandidacyProcess.class, newBean);

            newProcess.setOriginalIndividualCandidacyProcess(process);

            SecondCycleIndividualCandidacyEvent event =
                    (SecondCycleIndividualCandidacyEvent) newProcess.getCandidacy().getEvent();

            Collection<IndividualCandidacyDocumentFile> documents = process.getCandidacy().getDocumentsSet();

            for (IndividualCandidacyDocumentFile individualCandidacyDocumentFile : documents) {
                individualCandidacyDocumentFile.addIndividualCandidacy(newProcess.getCandidacy());
            }

            return newProcess;
        }

        @Override
        public Boolean isVisibleForGriOffice() {
            return false;
        }

        @Override
        public Boolean isVisibleForCoordinator() {
            return false;
        }

    }

}
