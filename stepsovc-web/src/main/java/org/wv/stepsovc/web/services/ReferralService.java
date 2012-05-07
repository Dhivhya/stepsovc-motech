package org.wv.stepsovc.web.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.wv.stepsovc.commcare.gateway.CommcareGateway;
import org.wv.stepsovc.vo.BeneficiaryFormRequest;
import org.wv.stepsovc.web.domain.Referral;
import org.wv.stepsovc.web.mapper.BeneficiaryMapper;
import org.wv.stepsovc.web.mapper.ReferralMapper;
import org.wv.stepsovc.web.repository.AllReferrals;
import org.wv.stepsovc.web.request.BeneficiaryCase;

public class ReferralService {

    private static Logger logger = Logger.getLogger(BeneficiaryService.class);

    @Autowired
    private AllReferrals allReferrals;

    @Autowired
    private CommcareGateway commcareGateway;

    private String COMMCARE_URL;

    public ReferralService(String COMMCARE_URL) {
        this.COMMCARE_URL = COMMCARE_URL;
    }

    public void addNewReferral(BeneficiaryCase beneficiaryCase) {
        logger.info("Handling new referral");
        Referral oldActiveReferral = allReferrals.findActiveReferral(beneficiaryCase.getBeneficiary_code());
        if(oldActiveReferral != null) {
            oldActiveReferral.setActive(false);
            allReferrals.update(oldActiveReferral);
        }

        Referral newReferral = new ReferralMapper().map(beneficiaryCase);
        newReferral.setActive(true);

        allReferrals.add(newReferral);

        assignToFacility(beneficiaryCase);
    }

    public void updateNotAvailedReasons(BeneficiaryCase beneficiaryCase) {
        logger.info("Handling update referral");
        Referral existingReferral = allReferrals.findActiveReferral(beneficiaryCase.getBeneficiary_code());

        allReferrals.update(new ReferralMapper().updateReferral(existingReferral, beneficiaryCase));
    }

    public void updateAvailedServices(BeneficiaryCase beneficiaryCase) {
        logger.info("Handling update service");
        Referral existingReferral = allReferrals.findActiveReferral(beneficiaryCase.getBeneficiary_code());

        allReferrals.update(new ReferralMapper().updateServices(existingReferral, beneficiaryCase));

        if(beneficiaryCase.getService_provider() != null && !"".equals(beneficiaryCase.getService_provider().trim())) {
            assignToFacility(beneficiaryCase);
        } else {
            removeFromCurrentFacility(beneficiaryCase);
        }
    }

    void removeFromCurrentFacility(BeneficiaryCase beneficiaryCase) {
        beneficiaryCase.setOwner_id(beneficiaryCase.getUser_id());
        updateReferralOwner(beneficiaryCase);
    }

    void assignToFacility(BeneficiaryCase beneficiaryCase) {
        String groupId = commcareGateway.getGroupId(beneficiaryCase.getService_provider());
        String ownerId = beneficiaryCase.getUser_id() + "," + groupId;
        beneficiaryCase.setOwner_id(ownerId);
        updateReferralOwner(beneficiaryCase);
    }

    void updateReferralOwner(BeneficiaryCase beneficiaryCase) {
        BeneficiaryFormRequest beneficiaryFormRequest = new BeneficiaryMapper().createFormRequest(beneficiaryCase);
        commcareGateway.updateReferralOwner(COMMCARE_URL, beneficiaryFormRequest);
    }
}