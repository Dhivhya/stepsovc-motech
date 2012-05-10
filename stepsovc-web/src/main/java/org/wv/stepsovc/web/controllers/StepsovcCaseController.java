package org.wv.stepsovc.web.controllers;

import org.apache.log4j.Logger;
import org.motechproject.casexml.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wv.stepsovc.web.request.BeneficiaryCaseUpdateType;
import org.wv.stepsovc.web.request.CaseType;
import org.wv.stepsovc.web.request.StepsovcCase;
import org.wv.stepsovc.web.services.BeneficiaryService;
import org.wv.stepsovc.web.services.ReferralService;

import java.io.IOException;

@Controller
@RequestMapping("/case/**")
public class StepsovcCaseController extends CaseService<StepsovcCase> {

    private static Logger logger = Logger.getLogger(StepsovcCaseController.class);

    private BeneficiaryService beneficiaryService;
    private ReferralService referralService;

    @Autowired
    public StepsovcCaseController(BeneficiaryService beneficiaryService, ReferralService referralService) {
        super(StepsovcCase.class);
        this.beneficiaryService = beneficiaryService;
        this.referralService = referralService;
    }

    @Override
    public ResponseEntity<String> processCase(HttpEntity<String> requestEntity) throws IOException {

        ResponseEntity<String> stringResponseEntity = null;
        try {
            stringResponseEntity = super.processCase(requestEntity);
        } catch (Exception e) {
            //TODO: decide about when to throwing it back to commcare-hq
            logger.error("Caught Exception while processing care : " + e);
        }
        //Hardcoded to return success response always, should be changed when we want to throw exception for retry
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @Override
    public void closeCase(StepsovcCase stepsovcCase) {
        logger.info("Inside close case");
    }

    @Override
    public void updateCase(StepsovcCase stepsovcCase) {
        logger.info("Inside update case");
    }

    @Override
    public void createCase(StepsovcCase stepsovcCase) {
        logger.info("Inside create case \"" + stepsovcCase.getForm_type() + "\"");

        if(CaseType.BENEFICIARY_CASE.getType().equals(stepsovcCase.getCase_type())) {
            if(BeneficiaryCaseUpdateType.BENEFICIARY_REGISTRATION.getType().equals(stepsovcCase.getForm_type()))
                beneficiaryService.createBeneficiary(stepsovcCase);
            else if(BeneficiaryCaseUpdateType.NEW_REFERRAL.getType().equals(stepsovcCase.getForm_type()))
                referralService.addNewReferral(stepsovcCase);
            else if(BeneficiaryCaseUpdateType.UPDATE_REFERRAL.getType().equals(stepsovcCase.getForm_type()))
                referralService.updateNotAvailedReasons(stepsovcCase);
            else if(BeneficiaryCaseUpdateType.UPDATE_SERVICE.getType().equals(stepsovcCase.getForm_type()))
                referralService.updateAvailedServices(stepsovcCase);
        }
    }
}

