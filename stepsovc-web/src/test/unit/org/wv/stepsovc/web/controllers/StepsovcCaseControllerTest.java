package org.wv.stepsovc.web.controllers;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.wv.stepsovc.commcare.domain.CaseType;
import org.wv.stepsovc.core.request.CaseUpdateType;
import org.wv.stepsovc.core.request.StepsovcCase;
import org.wv.stepsovc.core.services.BeneficiaryService;
import org.wv.stepsovc.core.services.FacilityService;
import org.wv.stepsovc.core.services.ReferralService;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;


public class StepsovcCaseControllerTest {

    @Mock
    BeneficiaryService mockBeneficiaryService;
    @Mock
    VelocityEngine velocityEngine;
    @Mock
    ReferralService mockReferralService;

    StepsovcCaseController stepsovcCaseController;
    private StepsovcCase stepsovcCase;
    @Mock
    private FacilityService facilityService;


    @Before
    public void setup() {
        initMocks(this);
        stepsovcCaseController = new StepsovcCaseController(mockBeneficiaryService, mockReferralService, facilityService);
        stepsovcCase = new StepsovcCase();
    }


    @Test
    public void ShouldCallBeneficiaryRegistrationHandler() throws Exception {
        stepsovcCase.setCase_type(CaseType.BENEFICIARY_CASE.getType());
        stepsovcCase.setForm_type(CaseUpdateType.BENEFICIARY_REGISTRATION.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        verify(mockBeneficiaryService).createBeneficiary(stepsovcCase);
    }

    @Test
    public void ShouldCallNewReferralHandler() throws Exception {
        stepsovcCase.setCase_type(CaseType.BENEFICIARY_CASE.getType());
        stepsovcCase.setForm_type(CaseUpdateType.NEW_REFERRAL.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        verify(mockReferralService).addNewReferral(stepsovcCase);
    }

    @Test
    public void ShouldCallUpdateReferralHandler() throws Exception {
        stepsovcCase.setCase_type(CaseType.BENEFICIARY_CASE.getType());
        stepsovcCase.setForm_type(CaseUpdateType.UPDATE_REFERRAL.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        verify(mockReferralService).updateNotAvailedReasons(stepsovcCase);
    }

    @Test
    public void ShouldCallUpdateServiceHandler() throws Exception {
        stepsovcCase.setCase_type(CaseType.BENEFICIARY_CASE.getType());
        stepsovcCase.setForm_type(CaseUpdateType.UPDATE_SERVICE.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        verify(mockReferralService).updateAvailedServices(stepsovcCase);
    }
}
