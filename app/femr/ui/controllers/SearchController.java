package femr.ui.controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import femr.business.dtos.CurrentUser;
import femr.business.dtos.ServiceResponse;
import femr.business.services.IPharmacyService;
import femr.business.services.ISearchService;
import femr.business.services.ISessionService;
import femr.business.services.IMedicalService;
import femr.common.models.*;
import femr.ui.helpers.controller.MedicalHelper;
import femr.ui.models.search.CreateEncounterViewModel;
import femr.ui.models.search.CreateViewModel;
import femr.ui.views.html.pharmacies.index;
import femr.util.calculations.dateUtils;
import play.mvc.Controller;
import play.mvc.Result;
import femr.ui.views.html.search.show;
import femr.ui.views.html.search.showEncounter;
import femr.ui.views.html.search.showError;
import femr.util.stringhelpers.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// include the view model for medical
import femr.ui.models.medical.CreateViewModelGet;
import views.html.defaultpages.error;

// include the view model for pharmacy
//import femr.ui.models.pharmacy.CreateViewModelGet;

public class SearchController extends Controller {
    private ISessionService sessionService;
    private ISearchService searchService;
    private IMedicalService medicalService;
    private IPharmacyService pharmacyService;
    private Provider<IPatientPrescription> patientPrescriptionProvider;
    private MedicalHelper medicalHelper;

    @Inject
    public SearchController(ISessionService sessionService,
                            ISearchService searchService,
                            IMedicalService medicalService,
                            IPharmacyService pharmacyService,
                            Provider<IPatientPrescription> patientPrescriptionProvider,
                            MedicalHelper medicalHelper) {
        this.sessionService = sessionService;
        this.searchService = searchService;
        this.medicalService = medicalService;
        this.medicalHelper = medicalHelper;
        this.pharmacyService = pharmacyService;
        this.patientPrescriptionProvider = patientPrescriptionProvider;
    }

    /*
    GET - specific encounter details based on encounter id.
    Not yet implemented.
     */
    public Result viewEncounter(int id) {
        //Get patientEncounter
        CreateEncounterViewModel viewModel = new CreateEncounterViewModel();
        CurrentUser currentUser = sessionService.getCurrentUserSession();
        ServiceResponse<IPatientEncounter> patientEncounterServiceResponse = searchService.findPatientEncounterById(id);
        IPatientEncounter patientEncounter = patientEncounterServiceResponse.getResponseObject();




        //findPatientEncounterVitalByVitalIdAndEncounterId() is in the process of being replaced
                  /*
        // Fetch Vitals
        //this can be cleaned up by providing a method 'getVitalOrNull' that takes care of both
        //ints and floats
        ServiceResponse<IPatientEncounterVital> patientEncounterVitalServiceResponse;
        List<IPatientEncounterVital> patientEncounterVitals = new ArrayList<>();

        for (int vital = 1; vital <= 10; vital++) {
            patientEncounterVitalServiceResponse = searchService.findPatientEncounterVitalByVitalIdAndEncounterId(vital, id);
            if (patientEncounterVitalServiceResponse.hasErrors()) {
                patientEncounterVitals.add(null);
            } else {
                patientEncounterVitals.add(patientEncounterVitalServiceResponse.getResponseObject());
            }
        }
        if (patientEncounterVitals.get(0) == null) {
            viewModel.setRespiratoryRate(null);
        } else {
            viewModel.setRespiratoryRate(getVitalOrNull(patientEncounterVitals.get(0)).intValue());
        }
        if (patientEncounterVitals.get(1) == null) {
            viewModel.setHeartRate(null);
        } else {
            viewModel.setHeartRate(getVitalOrNull(patientEncounterVitals.get(1)).intValue());
        }
        viewModel.setTemperature(getVitalOrNull(patientEncounterVitals.get(2)));
        viewModel.setOxygenSaturation(getVitalOrNull(patientEncounterVitals.get(3)));
        if (patientEncounterVitals.get(4) == null) {
            viewModel.setHeightFeet(null);
        } else {
            viewModel.setHeightFeet(getVitalOrNull(patientEncounterVitals.get(4)).intValue());
        }
        if (patientEncounterVitals.get(5) == null) {
            viewModel.setHeightInches(null);
        } else {
            viewModel.setHeightInches(getVitalOrNull(patientEncounterVitals.get(5)).intValue());
        }
        viewModel.setWeight(getVitalOrNull(patientEncounterVitals.get(6)));

        if (patientEncounterVitals.get(7) == null) {
            viewModel.setBloodPressureSystolic(null);
        } else {
            viewModel.setBloodPressureSystolic(getVitalOrNull(patientEncounterVitals.get(7)).intValue());
        }
        if (patientEncounterVitals.get(8) == null) {
            viewModel.setBloodPressureDiastolic(null);
        } else {
            viewModel.setBloodPressureDiastolic(getVitalOrNull(patientEncounterVitals.get(8)).intValue());
        }

        viewModel.setGlucose(getVitalOrNull(patientEncounterVitals.get(9)));
            */

        //Get Patient Name and other basic info
        ServiceResponse<IPatient> patientServiceResponseid = null;
        patientServiceResponseid = searchService.findPatientById(patientEncounter.getPatientId());
        IPatient patient = patientServiceResponseid.getResponseObject();
        if (!patientServiceResponseid.hasErrors()) {

            viewModel.setFirstName(patient.getFirstName());
            viewModel.setLastName(patient.getLastName());
            viewModel.setAddress(patient.getAddress());
            viewModel.setCity(patient.getCity());
            viewModel.setAge(dateUtils.calculateYears(patient.getAge()));
            viewModel.setSex(patient.getSex());

        }

        // Get medical


        // Add the Medical data
        //get a list of prescriptions
        ServiceResponse<List<? extends IPatientPrescription>> patientPrescriptionsServiceResponse = searchService.findPrescriptionsByEncounterId(patientEncounter.getId());
        List<? extends IPatientPrescription> patientPrescriptions = new ArrayList<>();
        if (patientPrescriptionsServiceResponse.hasErrors()) {
            //do nothing, there might not always be available prescriptions
        } else {
            patientPrescriptions = patientPrescriptionsServiceResponse.getResponseObject();
        }

        //region **Mapping of treatment fields, HPI, PMH, and vitals**

        //Create linked hash map of treatment fields
        //get a list of available treatment fields
        ServiceResponse<List<? extends ITreatmentField>> treatmentFieldsServiceResponse = searchService.findAllTreatmentFields();
        List<? extends ITreatmentField> treatmentFields = treatmentFieldsServiceResponse.getResponseObject();

        //initalize map to store treatment fields: Map<treatmentFieldName, List of values>
        //the list of values is stored in desecending order by time taken
        Map<String, List<? extends IPatientEncounterTreatmentField>> patientEncounterTreatmentMap = new LinkedHashMap<>();
        ServiceResponse<List<? extends IPatientEncounterTreatmentField>> patientTreatmentServiceResponse;
        String treatmentFieldName;
        //loop through each available treatment field and build the map
        for (int treatmentFieldIndex = 0; treatmentFieldIndex < treatmentFields.size(); treatmentFieldIndex++) {
            treatmentFieldName = treatmentFields.get(treatmentFieldIndex).getName().trim();
            patientTreatmentServiceResponse = searchService.findTreatmentFields(patientEncounter.getId(), treatmentFieldName);
            if (patientTreatmentServiceResponse.hasErrors()) {
                continue;
            } else {
                patientEncounterTreatmentMap.put(treatmentFieldName, patientTreatmentServiceResponse.getResponseObject());
            }
        }

        //Create linked hash map of history of present illness fields
        ServiceResponse<List<? extends IHpiField>> hpiFieldServiceResponse = searchService.findAllHpiFields();
        List<? extends IHpiField> hpiFields = hpiFieldServiceResponse.getResponseObject();

        Map<String, List<? extends IPatientEncounterHpiField>> patientEncounterHpiMap = new LinkedHashMap<>();
        ServiceResponse<List<? extends IPatientEncounterHpiField>> patientHpiServiceResponse;
        String hpiFieldName;
        for (int hpiFieldIndex = 0; hpiFieldIndex < hpiFields.size(); hpiFieldIndex++) {
            hpiFieldName = hpiFields.get(hpiFieldIndex).getName().trim();
            patientHpiServiceResponse = searchService.findHpiFields(patientEncounter.getId(), hpiFieldName);
            if (patientHpiServiceResponse.hasErrors()) {
                continue;
            } else {
                patientEncounterHpiMap.put(hpiFieldName, patientHpiServiceResponse.getResponseObject());
            }
        }

        //Create linked hash map of past medical history fields
        ServiceResponse<List<? extends IPmhField>> pmhFieldServiceResponse = searchService.findAllPmhFields();
        List<? extends IPmhField> pmhFields = pmhFieldServiceResponse.getResponseObject();

        Map<String, List<? extends IPatientEncounterPmhField>> patientEncounterPmhMap = new LinkedHashMap<>();
        ServiceResponse<List<? extends IPatientEncounterPmhField>> patientPmhServiceResponse;
        String pmhFieldName;
        for (int pmhFieldIndex = 0; pmhFieldIndex < pmhFields.size(); pmhFieldIndex++) {
            pmhFieldName = pmhFields.get(pmhFieldIndex).getName().trim();
            patientPmhServiceResponse = searchService.findPmhFields(patientEncounter.getId(), pmhFieldName);
            if (patientPmhServiceResponse.hasErrors()) {
                continue;
            } else {
                patientEncounterPmhMap.put(pmhFieldName, patientPmhServiceResponse.getResponseObject());
            }
        }

        //Create linked hash map of vitals
        ServiceResponse<List<? extends IVital>> vitalServiceResponse = searchService.findAllVitals();
        List<? extends IVital> vitals = vitalServiceResponse.getResponseObject();

        Map<String, List<? extends IPatientEncounterVital>> patientEncounterVitalMap = new LinkedHashMap<>();
        ServiceResponse<List<? extends IPatientEncounterVital>> patientVitalServiceResponse;
        String vitalFieldName;
        for (int vitalFieldIndex = 0; vitalFieldIndex < vitals.size(); vitalFieldIndex++) {
            vitalFieldName = vitals.get(vitalFieldIndex).getName().trim();
            patientVitalServiceResponse = searchService.findPatientEncounterVitals(patientEncounter.getId(), vitalFieldName);
            if (patientVitalServiceResponse.hasErrors()) {
                continue;
            } else {
                patientEncounterVitalMap.put(vitalFieldName, patientVitalServiceResponse.getResponseObject());
            }
        }

        //endregion

        //set up viewModelGet with everything except vitals
        femr.ui.models.medical.CreateViewModelGet viewMedicalModel = medicalHelper.populateViewModelGet(patient, patientEncounter, patientPrescriptions, patientEncounterVitalMap, patientEncounterTreatmentMap, patientEncounterHpiMap, patientEncounterPmhMap);

        // ******* End of medical data

        // Get the medical Responce
        // Store it in the medical component

        viewModel.setMedicalView(viewMedicalModel);

        // ***************** Get the pharmacy data
        //find patient prescriptions
        femr.ui.models.pharmacy.CreateViewModelGet viewPharmacyModel = new femr.ui.models.pharmacy.CreateViewModelGet();

       // List<? extends IPatientPrescription> patientPrescriptions = patientPrescriptionsServiceResponse.getResponseObject();
        List<String> dynamicViewMedications = new ArrayList<>();

        for (int filledPrescription = 0; filledPrescription < patientPrescriptions.size(); filledPrescription++) {
            if (patientPrescriptions.get(filledPrescription).getReplaced() != true) {
                dynamicViewMedications.add(patientPrescriptions.get(filledPrescription).getMedicationName());
            }
        }
        //this should probably be left as a List or ArrayList
        String[] viewMedications = new String[dynamicViewMedications.size()];
        viewMedications = dynamicViewMedications.toArray(viewMedications);
        viewPharmacyModel.setMedications(viewMedications);

        // *********************** End of Pharmacy



        viewModel.setPharmacyView(viewPharmacyModel);

        /*                findAllTreatmentByEncounterId is in the process of being replaced
        //Get treatment info
        List<String> problemList = new ArrayList<String>();

        ServiceResponse<List<? extends IPatientEncounterTreatmentField>> patientEncounterProblemsServiceResponse = searchService.findAllTreatmentByEncounterId(id);
        if (patientEncounterProblemsServiceResponse.getResponseObject() != null) {
            for (int i = 0; i < patientEncounterProblemsServiceResponse.getResponseObject().size(); i++) {
                if (patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldId() == 1) {
                    viewModel.setAssessment(patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldValue());
                }
                if (patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldId() == 2) {
                    problemList.add(patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldValue());
                }
                if (patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldId() == 3) {
                    viewModel.setTreatment(patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldValue());
                }
                if (patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldId() == 4) {
                    viewModel.setFamilyHist(patientEncounterProblemsServiceResponse.getResponseObject().get(i).getTreatmentFieldValue());
                }
            }
            viewModel.setProblemList(problemList);
        }
                                */
        //Get patient prescriptions
        List<String> prescriptionsList = new ArrayList<String>();
        //ServiceResponse<List<? extends IPatientPrescription>> patientPrescriptionsServiceResponse = searchService.findPrescriptionsByEncounterId(patientEncounter.getId());
        if (!patientPrescriptionsServiceResponse.hasErrors()) {
            for (int i = 0; i < patientPrescriptionsServiceResponse.getResponseObject().size(); i++) {
                prescriptionsList.add(patientPrescriptionsServiceResponse.getResponseObject().get(i).getMedicationName());
            }
            viewModel.setPerscribList(prescriptionsList);
        }


        return ok(showEncounter.render(currentUser, patientEncounter, viewModel));   // this is where the responce is returned to the encounter page
    }

    private Float getVitalOrNull(IPatientEncounterVital patientEncounterVital) {
        if (patientEncounterVital == null)
            return null;
        else
            return patientEncounterVital.getVitalValue();
    }

    /*
    GET - detailed patient information
        based on ID
     */
    public Result createGet() {
        CurrentUser currentUser = sessionService.getCurrentUserSession();
        boolean error = false;
        String firstName = request().getQueryString("searchFirstName");
        String lastName = request().getQueryString("searchLastName");
        String s_id = request().getQueryString("id");
        ServiceResponse<List<? extends IPatient>> patientServiceResponse = null;
        ServiceResponse<IPatient> patientServiceResponseid = null;

        Integer id;

        if (!StringUtils.isNullOrWhiteSpace(s_id)) {
            s_id = s_id.trim();
            id = Integer.parseInt(s_id);
            patientServiceResponseid = searchService.findPatientById(id);
        } else if (!StringUtils.isNullOrWhiteSpace(firstName) && StringUtils.isNullOrWhiteSpace(lastName) || !StringUtils.isNullOrWhiteSpace(lastName) && StringUtils.isNullOrWhiteSpace(firstName) || !StringUtils.isNullOrWhiteSpace(firstName) && !StringUtils.isNullOrWhiteSpace(lastName)) {
            firstName = firstName.trim();
            lastName = lastName.trim();
            patientServiceResponse = searchService.findPatientByName(firstName, lastName);
            if (patientServiceResponse.getResponseObject() != null) {
                id = patientServiceResponse.getResponseObject().get(0).getId();  //grab 1st index
            } else {
                id = 0;
            }
        } else {

            return ok(showError.render(currentUser));
        }
        if (patientServiceResponseid != null) {
            if (patientServiceResponseid.hasErrors()) {
                return ok(showError.render(currentUser));
            }
        }
        ServiceResponse<List<? extends IPatientEncounter>> patientEncountersServiceResponse = searchService.findAllEncountersByPatientId(id);
        if (patientEncountersServiceResponse.hasErrors()) {

            return ok(showError.render(currentUser));
        }


        List<? extends IPatientEncounter> patientEncounters = patientEncountersServiceResponse.getResponseObject();
        CreateViewModel viewModel = new CreateViewModel();
        if (patientServiceResponse != null) {
            if (!patientServiceResponse.hasErrors()) {
                IPatient patient = patientServiceResponse.getResponseObject().get(0);
                viewModel.setPatientNameResult(patientServiceResponse.getResponseObject());
                viewModel.setFirstName(patient.getFirstName());
                viewModel.setLastName(patient.getLastName());
                viewModel.setAddress(patient.getAddress());
                viewModel.setCity(patient.getCity());
                viewModel.setAge(dateUtils.calculateYears(patient.getAge()));
                viewModel.setSex(patient.getSex());
            } else {
                return ok(showError.render(currentUser));
            }

        } else {
            if (!patientServiceResponseid.hasErrors()) {
                IPatient patient = patientServiceResponseid.getResponseObject();
                viewModel.setFirstName(patient.getFirstName());
                viewModel.setLastName(patient.getLastName());
                viewModel.setAddress(patient.getAddress());
                viewModel.setCity(patient.getCity());
                viewModel.setAge(dateUtils.calculateYears(patient.getAge()));
                viewModel.setSex(patient.getSex());
                viewModel.setPatientID(patient.getId());
            } else {
                return ok(showError.render(currentUser));
            }
        }

        return ok(show.render(currentUser, error, viewModel, patientEncounters, id));
    }
}
