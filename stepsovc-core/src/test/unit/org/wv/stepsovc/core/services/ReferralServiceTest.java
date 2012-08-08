package org.wv.stepsovc.core.services;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.wv.stepsovc.commcare.gateway.CommcareGateway;
import org.wv.stepsovc.commcare.vo.CaseOwnershipInformation;
import org.wv.stepsovc.core.domain.Referral;
import org.wv.stepsovc.core.fixtures.StepsovcCaseFixture;
import org.wv.stepsovc.core.mapper.ReferralMapper;
import org.wv.stepsovc.core.repository.AllReferrals;
import org.wv.stepsovc.core.request.StepsovcCase;
import org.wv.stepsovc.core.vo.FacilityAvailability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.util.DateUtil.today;

public class ReferralServiceTest {
    @Mock
    private AllReferrals mockAllReferrals;
    @Mock
    private FacilityService mockFacilityService;
    @Mock
    private CommcareGateway mockCommcareGateway;
    @Mock
    private StepsovcScheduleService mockStepsovcScheduleService;
    @Mock
    private StepsovcAlertService mockStepsovcAlertService;
    @Mock
    private BeneficiaryService mockBeneficiaryService;
    private ReferralService spyReferralService;
    private Integer exportWindowInWeeks = 3;

    @Before
    public void setup() {
        initMocks(this);
        spyReferralService = spy(new ReferralService());
        ReflectionTestUtils.setField(spyReferralService, "allReferrals", mockAllReferrals);
        ReflectionTestUtils.setField(spyReferralService, "commcareGateway", mockCommcareGateway);
        ReflectionTestUtils.setField(spyReferralService, "facilityService", mockFacilityService);
        ReflectionTestUtils.setField(spyReferralService, "stepsovcScheduleService", mockStepsovcScheduleService);
        ReflectionTestUtils.setField(spyReferralService, "stepsovcAlertService", mockStepsovcAlertService);
        ReflectionTestUtils.setField(spyReferralService, "exportWindowInWeeks", exportWindowInWeeks);
        ReflectionTestUtils.setField(spyReferralService, "beneficiaryService", mockBeneficiaryService);
    }

    @Test
    public void shouldSendSMSToFacility_CreateSchedulesAndUpdateReferralOwnerWhileCreatingNewReferral() {

        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);
        ArgumentCaptor<CaseOwnershipInformation> caseOwnershipInformationArgumentCaptor = ArgumentCaptor.forClass(CaseOwnershipInformation.class);

        String code = "ben001";
        String facilityCode = "FAC001";

        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForReferral(code, "2012-05-30", "FAC001");

        doReturn(null).when(mockAllReferrals).findActiveReferral(code);
        doReturn(new FacilityAvailability(true)).when(mockFacilityService).getFacilityAvailability(stepsovcCase.getFacility_code(), stepsovcCase.getService_date());
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(code);

        spyReferralService.addNewReferral(stepsovcCase);

        verify(mockAllReferrals).add(referralArgumentCaptor.capture());
        verify(mockStepsovcScheduleService).scheduleNewReferral(referralArgumentCaptor.getValue());

        doNothing().when(mockAllReferrals).add(referralArgumentCaptor.getValue());

        ArgumentCaptor<String> facilityCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockCommcareGateway).addGroupOwnership(caseOwnershipInformationArgumentCaptor.capture(), facilityCodeCaptor.capture());

        assertThat(facilityCodeCaptor.getValue(), is(facilityCode));
        assertThat(caseOwnershipInformationArgumentCaptor.getValue().getUserId(), is(stepsovcCase.getUser_id()));

    }

    @Test
    public void shouldMoveReferralToAvailableDateIfFacilityIsUnavailable() {

        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);

        String code = "ben001";

        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForReferral(code, "2012-05-30", "FAC001");

        doReturn(null).when(mockAllReferrals).findActiveReferral(code);
        doReturn(new FacilityAvailability(false, "2012-06-01", "2012-05-30", "2012-05-30")).when(mockFacilityService).getFacilityAvailability(stepsovcCase.getFacility_code(), stepsovcCase.getService_date());
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(code);

        spyReferralService.addNewReferral(stepsovcCase);

        verify(mockAllReferrals).add(referralArgumentCaptor.capture());
        verify(mockStepsovcScheduleService).scheduleNewReferral(referralArgumentCaptor.getValue());

        doNothing().when(mockAllReferrals).add(referralArgumentCaptor.getValue());

        assertThat(referralArgumentCaptor.getValue().getServiceDate(), is("2012-06-01"));

    }

    @Test
    public void shouldAlertCareGiverIfNewReferralIsAddedOnServiceUnavailableDate() {
        String caregiverId = "CG1";
        String facilityCode = "FAC001";
        String unavailableFrom = "2012-12-12";
        String unavailableTo = "2012-12-13";
        String nextAvailableDate = "2012-12-14";

        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForServiceUnavailable(caregiverId, facilityCode, unavailableFrom, unavailableFrom, unavailableTo);
        FacilityAvailability facilityAvailability = new FacilityAvailability(false, nextAvailableDate, unavailableFrom, unavailableTo);
        facilityAvailability.setUnavailableFromDate(unavailableFrom);
        facilityAvailability.setUnavailableToDate(unavailableTo);
        doReturn(facilityAvailability).when(mockFacilityService).getFacilityAvailability(facilityCode, unavailableFrom);
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(stepsovcCase.getBeneficiary_code());

        spyReferralService.addNewReferral(stepsovcCase);

        verify(mockStepsovcAlertService).sendInstantServiceUnavailabilityMsgToCareGiverOfReferral(caregiverId,
                facilityCode, unavailableFrom, unavailableTo, nextAvailableDate);
    }

    @Test
    public void shouldNotAlertCareGiverIfNewReferralIsAddedOnServiceAvailableDate() {
        String facilityCode = "FAC001";
        String serviceDate = "2012-12-11";
        String unavailableFrom = "2012-12-12";
        String unavailableTo = "2012-12-13";
        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForServiceUnavailable("CG1", facilityCode, serviceDate, unavailableFrom, unavailableTo);
        FacilityAvailability facilityAvailability = new FacilityAvailability(true);
        doReturn(facilityAvailability).when(mockFacilityService).getFacilityAvailability(facilityCode, serviceDate);
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(stepsovcCase.getBeneficiary_code());

        spyReferralService.addNewReferral(stepsovcCase);

        verifyZeroInteractions(mockStepsovcAlertService);
    }


    @Test
    public void shouldRemoveFacilityFromOwnersAndUnScheduleAppointmentsWhileUpdatingReferralServices() {

        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);

        String code = "ben001";
        String groupId = "group001";

        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForUpdateService(code, "2012-05-31");
        String ownerId = "userid" + "," + groupId;
        stepsovcCase.setOwner_id(ownerId);
        stepsovcCase.setFacility_code(null);

        Referral referral = new ReferralMapper().map(stepsovcCase);

        doReturn(referral).when(mockAllReferrals).findActiveReferral(code);

        spyReferralService.updateAvailedServices(stepsovcCase);

        verify(mockAllReferrals).update(referralArgumentCaptor.capture());

        doNothing().when(mockAllReferrals).update(referralArgumentCaptor.getValue());

        verify(mockStepsovcScheduleService).unscheduleReferral(referral.getOvcId());
    }

    @Test
    public void shouldAssignToNewFacilityAndRescheduleAppointmentWhileUpdatingReferralServicesWithServiceProvider() {

        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);
        ArgumentCaptor<CaseOwnershipInformation> caseOwnershipInformationArgumentCaptor = ArgumentCaptor.forClass(CaseOwnershipInformation.class);

        String code = "ben001";
        String groupId1 = "group001";

        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForUpdateService(code, "2012-05-31");
        String ownerId = "userid" + "," + groupId1;
        stepsovcCase.setOwner_id(ownerId);

        String groupName = "groupName";
        stepsovcCase.setFacility_code(groupName);
        Referral referral = new ReferralMapper().map(stepsovcCase);

        doReturn(referral).when(mockAllReferrals).findActiveReferral(code);
        doReturn(new FacilityAvailability(true)).when(mockFacilityService).getFacilityAvailability(referral.getFacilityCode(), referral.getServiceDate());
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(code);

        spyReferralService.updateAvailedServices(stepsovcCase);

        verify(mockAllReferrals).update(referralArgumentCaptor.capture());
        verify(mockStepsovcScheduleService).unscheduleReferral(referral.getOvcId());
        verify(mockStepsovcScheduleService).scheduleNewReferral(referralArgumentCaptor.getValue());

        doNothing().when(mockAllReferrals).update(referralArgumentCaptor.getValue());

        ArgumentCaptor<String> facilityCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockCommcareGateway).addGroupOwnership(caseOwnershipInformationArgumentCaptor.capture(), facilityCodeCaptor.capture());

        assertThat(facilityCodeCaptor.getValue(), is(groupName));
        assertThat(caseOwnershipInformationArgumentCaptor.getValue().getUserId(), is(stepsovcCase.getUser_id()));
    }

    @Test
    public void shouldDeactivatePreviousReferralWhileCreatingNewAndRemoveOldSchedules() {
        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);

        String code = "ben001";
        String oldOvcId = "oooVcId";
        StepsovcCase stepsovcCase = StepsovcCaseFixture.createCaseForReferral(code, "2012-05-31", "FAC001");

        doReturn(new Referral().setOvcId(oldOvcId)).when(mockAllReferrals).findActiveReferral(code);
        doNothing().when(mockAllReferrals).add(Matchers.<Referral>any());
        doNothing().when(mockAllReferrals).update(Matchers.<Referral>any());
        doReturn(new FacilityAvailability(true)).when(mockFacilityService).getFacilityAvailability(stepsovcCase.getFacility_code(), stepsovcCase.getService_date());
        doReturn(true).when(mockBeneficiaryService).beneficiaryExists(code);

        spyReferralService.addNewReferral(stepsovcCase);

        verify(mockAllReferrals).update(referralArgumentCaptor.capture());
        verify(mockStepsovcScheduleService).unscheduleReferral(referralArgumentCaptor.getValue().getOvcId());
        verify(mockStepsovcScheduleService).unscheduleFollowUpVisit(referralArgumentCaptor.getValue().getOvcId());

        assertFalse(referralArgumentCaptor.getValue().isActive());
    }

    @Test
    public void shouldUpdateServiceDateForReferralsWhichFallOnServiceUnavailableDates() {
        String facilityId = "123";
        String fromDateStr = "2012-05-01";
        String toDateStr = "2012-05-04";
        String nextAvailDate = "2012-05-05";

        ArgumentCaptor<Referral> referralArgumentCaptor = ArgumentCaptor.forClass(Referral.class);
        doReturn(Arrays.asList(new Referral().setOvcId("ovc1").setServiceDate(fromDateStr), new Referral().setOvcId("ovc2").setServiceDate(fromDateStr))).when(mockAllReferrals).findActiveReferrals(facilityId, fromDateStr);
        doReturn(Arrays.asList(new Referral().setOvcId("ovc3").setServiceDate(fromDateStr))).when(mockAllReferrals).findActiveReferrals(facilityId, "2012-05-02");
        doReturn(new ArrayList<Referral>()).when(mockAllReferrals).findActiveReferrals(facilityId, "2012-05-03");
        doReturn(Arrays.asList(new Referral().setOvcId("ovc4").setServiceDate(fromDateStr))).when(mockAllReferrals).findActiveReferrals(facilityId, toDateStr);

        List<Referral> updatedReferrals = spyReferralService.updateReferralsServiceDate(facilityId, fromDateStr, toDateStr, nextAvailDate);

        assertThat(updatedReferrals.size(), is(4));
        verify(mockAllReferrals, times(4)).update(referralArgumentCaptor.capture());

        for (Referral referral : referralArgumentCaptor.getAllValues()) {
            verify(mockStepsovcScheduleService).unscheduleReferral(referral.getOvcId());
            assertThat(referral.getServiceDate(), is(nextAvailDate));
        }

    }

    @Test
    public void shouldUpdateUnAvailedReasons() {
        String benCode = "ABC";
        StepsovcCase stepsovcCase = StepsovcCaseFixture.createNewCase(benCode);
        Referral toBeReturned = new ReferralMapper().map(stepsovcCase);
        stepsovcCase = StepsovcCaseFixture.createCaseForUpdateReferral(benCode);
        stepsovcCase.setBeneficiary_code(benCode);
        String ovcId = "1234";
        toBeReturned.setOvcId(ovcId);
        doReturn(toBeReturned).when(mockAllReferrals).findActiveReferral(benCode);
        spyReferralService.updateNotAvailedReasons(stepsovcCase);
        verify(mockStepsovcScheduleService).unscheduleReferral(toBeReturned.getOvcId());
        verify(mockAllReferrals).update(toBeReturned);
    }

    @Test
    public void shouldGetReferralsForExport() {
        List<Referral> expectedReferrals = Arrays.asList(new Referral());
        LocalDate onOrAfterDate = today().minusWeeks(exportWindowInWeeks);
        doReturn(expectedReferrals).when(mockAllReferrals).getAllModifiedBetween(onOrAfterDate, today());
        List<Referral> actualReferrals = spyReferralService.getReferralDataForExport();
        assertEquals(expectedReferrals, actualReferrals);
    }
}
