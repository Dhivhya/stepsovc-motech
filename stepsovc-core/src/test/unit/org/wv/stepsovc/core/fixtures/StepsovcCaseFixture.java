package org.wv.stepsovc.core.fixtures;

import org.wv.stepsovc.commcare.domain.CaseType;
import org.wv.stepsovc.core.request.StepsovcCase;

import static org.wv.stepsovc.core.mapper.ReferralMapper.SERVICE_RECEIVED;

public class StepsovcCaseFixture {
    public static StepsovcCase createCaseForReferral(String benCode, String serviceDate, String facilityId) {
        StepsovcCase stepsovcCase = createNewCase(benCode);
        stepsovcCase.setCondoms("Referred");
        stepsovcCase.setArt_adherence_counseling("Referred");
        stepsovcCase.setEnd_of_life_hospice("Not Referred");
        stepsovcCase.setFamily_planning("Referred");
        stepsovcCase.setFollowup_date("2012-12-12");
        stepsovcCase.setFollowup_required("Yes");
        stepsovcCase.setHiv_counseling("Referred");
        stepsovcCase.setPmtct("Referred");
        stepsovcCase.setNew_diagnosis("Referred");
        stepsovcCase.setSexually_transmitted_infection("Not Referred");
        stepsovcCase.setPain_management("Referred");
        stepsovcCase.setOther_health_services("Referred");
        stepsovcCase.setVisit_date("1988-12-23");
        stepsovcCase.setService_date(serviceDate);
        stepsovcCase.setService_details("new service details");
        stepsovcCase.setFacility_code(facilityId);
        stepsovcCase.setUser_id("Userid001");

        return stepsovcCase;
    }

    public static StepsovcCase createNewCase(String benCode) {
        StepsovcCase stepsovcCase = new StepsovcCase();

        stepsovcCase.setCase_type(CaseType.BENEFICIARY_CASE.getType());
        stepsovcCase.setBeneficiary_code(benCode);
        stepsovcCase.setBeneficiary_dob("10-10-1967");
        stepsovcCase.setBeneficiary_name("Ben_Name");
        stepsovcCase.setCaregiver_code("CareGiverID");
        stepsovcCase.setCaregiver_name("CareGiverName");
        stepsovcCase.setFacility_code("FAC");
        return stepsovcCase;
    }

    public static StepsovcCase createCaseForUpdateReferral(String beneficiaryCode) {
        StepsovcCase stepsovcCase = createNewCase(beneficiaryCode);
        stepsovcCase.setArt_adherence_na_reason("SERVICE_UNAVAILABLE");
        stepsovcCase.setCondoms_na_reason("FACILITY_CLOSED");
        stepsovcCase.setEnd_of_life_hospice_na_reason("BEN_UNABLE_TO_TRAVEL");
        stepsovcCase.setFamily_planning_na_reason("BEN_FORGOT");
        stepsovcCase.setHiv_counseling_na_reason("BEN_UNWILLING");
        stepsovcCase.setNew_diagnosis_na_reason("AVAILED_NOT_RECORED");
        stepsovcCase.setSexually_transmitted_na_reason("OTHER");
        stepsovcCase.setPain_management_na_reason("");
        stepsovcCase.setPmtct_na_reason("");
        stepsovcCase.setOther_health_service_na_reason("BEN_UNABLE_TO_TRAVEL");
        return stepsovcCase;

    }

    public static StepsovcCase createCaseForUpdateService(String code, String serviceDate) {
        StepsovcCase stepsovcCase = createCaseForReferral(code, null, "FAC001");
        stepsovcCase.setCondoms("Not Availed");
        stepsovcCase.setEnd_of_life_hospice("Not Availed");
        stepsovcCase.setFamily_planning("Not Availed");
        stepsovcCase.setFollowup_date("2012-12-12");
        stepsovcCase.setFollowup_required("Yes");
        stepsovcCase.setHiv_counseling("Not Availed");
        stepsovcCase.setPmtct("Not Availed");
        stepsovcCase.setNew_diagnosis("Not Availed");
        stepsovcCase.setSexually_transmitted_infection("Not Availed");
        stepsovcCase.setPain_management("Not Availed");
        stepsovcCase.setOther_health_services("Not Availed");
        stepsovcCase.setArt_adherence_counseling("Not Availed");
        stepsovcCase.setFacility_code("ABC");
        stepsovcCase.setService_date(null);
        stepsovcCase.setService_date(serviceDate);

        return stepsovcCase;
    }

    public static StepsovcCase createCaseForUpdateServiceWithServicesFullfilled(String code, String serviceDate) {
        StepsovcCase stepsovcCase = createCaseForReferral(code, null, "FAC001");
        stepsovcCase.setCondoms(SERVICE_RECEIVED);
        stepsovcCase.setEnd_of_life_hospice(SERVICE_RECEIVED);
        stepsovcCase.setFamily_planning(SERVICE_RECEIVED);
        stepsovcCase.setFollowup_date("2012-12-12");
        stepsovcCase.setFollowup_required("Yes");
        stepsovcCase.setHiv_counseling(SERVICE_RECEIVED);
        stepsovcCase.setPmtct(SERVICE_RECEIVED);
        stepsovcCase.setNew_diagnosis(SERVICE_RECEIVED);
        stepsovcCase.setSexually_transmitted_infection(SERVICE_RECEIVED);
        stepsovcCase.setPain_management(SERVICE_RECEIVED);
        stepsovcCase.setOther_health_services(SERVICE_RECEIVED);
        stepsovcCase.setArt_adherence_counseling(SERVICE_RECEIVED);
        stepsovcCase.setFacility_code("ABC");
        stepsovcCase.setService_date(null);
        stepsovcCase.setService_date(serviceDate);
        return stepsovcCase;
    }

    public static StepsovcCase createCaseForServiceUnavailable(String caregiverId, String facility_code, String serviceDate, String unavailableFrom, String unavailableTo) {
        StepsovcCase stepsovcCase = new StepsovcCase();
        stepsovcCase.setUser_id(caregiverId);
        stepsovcCase.setFacility_code(facility_code);
        stepsovcCase.setService_date(serviceDate);
        stepsovcCase.setService_unavailable_from(unavailableFrom);
        stepsovcCase.setService_unavailable_to(unavailableTo);
        return stepsovcCase;
    }

    public static StepsovcCase createNewCaseWithFacilityCode(String facilityCode, String unavailableDateFrom, String unavailableDateTo) {
        StepsovcCase stepsovcCase = new StepsovcCase();
        stepsovcCase.setFacility_code(facilityCode);
        stepsovcCase.setService_unavailable_from(unavailableDateFrom);
        stepsovcCase.setService_unavailable_to(unavailableDateTo);
        return stepsovcCase;

    }
}
