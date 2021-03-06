package org.wv.stepsovc.web.controllers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.appointments.api.service.contract.VisitsQuery;
import org.motechproject.scheduletracking.api.repository.AllEnrollments;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.wv.stepsovc.core.configuration.ScheduleNames;
import org.wv.stepsovc.core.domain.Beneficiary;
import org.wv.stepsovc.core.domain.Facility;
import org.wv.stepsovc.core.domain.Referral;
import org.wv.stepsovc.core.domain.ServiceUnavailability;
import org.wv.stepsovc.core.repository.AllAppointments;
import org.wv.stepsovc.core.repository.AllBeneficiaries;
import org.wv.stepsovc.core.repository.AllFacilities;
import org.wv.stepsovc.core.repository.AllReferrals;
import org.wv.stepsovc.core.request.StepsovcCase;

import static fixtures.StepsovcCaseFixture.*;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.wv.stepsovc.core.mapper.ReferralMapper.SERVICE_RECEIVED;
import static org.wv.stepsovc.core.request.CaseUpdateType.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:testWebApplicationContext.xml")
public class BeneficiaryCaseIT {

    @Autowired
    StepsovcCaseController stepsovcCaseController;
    @Autowired
    AllBeneficiaries allBeneficiaries;
    @Autowired
    AllReferrals allReferrals;
    @Autowired
    private AllFacilities allFacilities;
    @Autowired
    private ScheduleTrackingService scheduleTrackingService;
    @Autowired
    private AllEnrollments allEnrollments;
    @Autowired
    private AllAppointments allAppointments;

    private Facility facility;
    private String beneficiaryCode = "8888";
    private Beneficiary beneficiary;
    private StepsovcCase stepsovcCase;

    @Before
    public void setUp() throws Exception {
        stepsovcCase = createNewBeneficiaryCase(beneficiaryCode);
        stepsovcCase.setForm_type(BENEFICIARY_REGISTRATION.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        beneficiary = allBeneficiaries.findBeneficiaryByCode(beneficiaryCode);
        facility = new Facility("FAC001", "FAC001-Name",
                asList(new ServiceUnavailability("reason1", "2012-06-20", "2012-06-20"),
                        new ServiceUnavailability("reason2", "2012-06-26", "2012-06-26")
                ),
                asList("9999999999", "88888888"));
        allFacilities.add(facility);
        stepsovcCase = createCaseForReferral(beneficiaryCode, "2013-4-12", "FAC001", "1988-12-23");
        stepsovcCase.setForm_type(NEW_REFERRAL.getType());
    }

    @Test
    public void testReferralFullfillmentScenario() {
        assertNotNull(beneficiary);
        stepsovcCaseController.createCase(stepsovcCase);

        Referral activeReferral = allReferrals.findActiveReferral(beneficiaryCode);
        assertNotNull(activeReferral);
        assertReferrals(stepsovcCase, activeReferral);
        String firstOvcId = activeReferral.getOvcId();
        Assert.assertNotNull(allEnrollments.getActiveEnrollment(firstOvcId, ScheduleNames.REFERRAL.getName()));
        Assert.assertNotNull(allEnrollments.getActiveEnrollment(firstOvcId, ScheduleNames.DEFAULTMENT.getName()));
        assertThat(allAppointments.find(new VisitsQuery().havingExternalId(firstOvcId)).size(), is(1));


        /* Test New Referral Creation - Replace existing */
        stepsovcCase.setService_date("2013-5-12");
        stepsovcCaseController.createCase(stepsovcCase);
        activeReferral = allReferrals.findActiveReferral(beneficiaryCode);
        String secondOvcId = activeReferral.getOvcId();
        assertNotNull(activeReferral);
        assertReferrals(stepsovcCase, allReferrals.findActiveReferral(beneficiaryCode));
        assertNull(allEnrollments.getActiveEnrollment(firstOvcId, ScheduleNames.REFERRAL.getName()));
        assertNull(allEnrollments.getActiveEnrollment(firstOvcId, ScheduleNames.DEFAULTMENT.getName()));
        assertThat(allAppointments.find(new VisitsQuery().havingExternalId(firstOvcId)).size(), is(0));
        assertNotNull(allEnrollments.getActiveEnrollment(secondOvcId, ScheduleNames.REFERRAL.getName()));
        assertNotNull(allEnrollments.getActiveEnrollment(secondOvcId, ScheduleNames.DEFAULTMENT.getName()));
        assertThat(allAppointments.find(new VisitsQuery().havingExternalId(secondOvcId)).size(), is(1));

        /* Test Availed Service - fulfil existing schedule with all schedules availed*/
        stepsovcCase = createCaseForUpdateServiceWithServicesFullfilled(beneficiaryCode, "2013-12-12", null);
        stepsovcCase.setForm_type(UPDATE_SERVICE.getType());
        stepsovcCase.setOwner_id("123");
        stepsovcCaseController.createCase(stepsovcCase);
        Assert.assertNotNull(allReferrals.findActiveReferral(beneficiaryCode));
        assertServices(stepsovcCase, allReferrals.findActiveReferral(beneficiaryCode));
        assertNull(allEnrollments.getActiveEnrollment(secondOvcId, ScheduleNames.DEFAULTMENT.getName()));
        assertNull(allEnrollments.getActiveEnrollment(secondOvcId, ScheduleNames.REFERRAL.getName()));
    }

    @Test
    public void testReferralPartialFullfillmentScenarioWithForwardToAnotherFacility() {
        stepsovcCase.setService_date("2013-5-12");
        stepsovcCaseController.createCase(stepsovcCase);
        Referral activeReferral = allReferrals.findActiveReferral(beneficiaryCode);
        String ovcId = activeReferral.getOvcId();
        assertNotNull(activeReferral);

        /* Test Availed Service - fulfil existing schedule and refer to new facility*/
        stepsovcCase = createCaseForUpdateService(beneficiaryCode, "2013-12-12", "FAC002");
        stepsovcCase.setForm_type(UPDATE_SERVICE.getType());
        stepsovcCase.setOwner_id("123");
        stepsovcCaseController.createCase(stepsovcCase);
        Assert.assertNotNull(allReferrals.findActiveReferral(beneficiaryCode));
        assertServices(stepsovcCase, allReferrals.findActiveReferral(beneficiaryCode));
        assertNotNull(allEnrollments.getActiveEnrollment(ovcId, ScheduleNames.REFERRAL.getName()));
        assertNotNull(allEnrollments.getActiveEnrollment(ovcId, ScheduleNames.DEFAULTMENT.getName()));
    }


    @Test
    public void testReferralPartialFullfillmentScenarioWithUpdateReasons() {
        stepsovcCase.setService_date("2013-5-12");
        stepsovcCaseController.createCase(stepsovcCase);
        Referral activeReferral = allReferrals.findActiveReferral(beneficiaryCode);
        String ovcId = activeReferral.getOvcId();
        assertNotNull(activeReferral);

        /* Test Availed Service - fulfil partial services*/
        stepsovcCase = createCaseForUpdateService(beneficiaryCode, "2013-12-12", null);
        stepsovcCase.setForm_type(UPDATE_SERVICE.getType());
        stepsovcCase.setOwner_id("123");
        stepsovcCase.setArt_adherence_counseling(SERVICE_RECEIVED);
        stepsovcCaseController.createCase(stepsovcCase);
        Assert.assertNotNull(allReferrals.findActiveReferral(beneficiaryCode));
        assertServices(stepsovcCase, allReferrals.findActiveReferral(beneficiaryCode));
        assertNull(allEnrollments.getActiveEnrollment(ovcId, ScheduleNames.REFERRAL.getName()));
        assertNotNull(allEnrollments.getActiveEnrollment(ovcId, ScheduleNames.DEFAULTMENT.getName()));

        /* Assert referral reasons with case */
        stepsovcCase = createCaseForUpdateReferral(beneficiaryCode);
        stepsovcCase.setForm_type(UPDATE_REFERRAL.getType());
        stepsovcCaseController.createCase(stepsovcCase);
        Assert.assertNotNull(allBeneficiaries.findBeneficiaryByCode(beneficiaryCode));
        assertReferralReasons(stepsovcCase, allReferrals.findActiveReferral(beneficiaryCode));
    }

    @After
    public void clearAll() throws SchedulerException {
        allFacilities.remove(facility);
        allBeneficiaries.removeAll();
        allReferrals.removeAllByBeneficiary(beneficiaryCode);
        allEnrollments.removeAll();
    }

}
