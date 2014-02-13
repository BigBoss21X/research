package mock.femr.data.models;

import femr.common.models.IPatientEncounter;

public class MockPatientEncounter implements IPatientEncounter {

    private int id = 0;
    private int patientId = 0;
    private int userId = 0;
    private String dateOfVisit = "";
    private String chiefComplaint = "";
    private Integer weeksPregnant = 0;
    private Boolean isPregnant = false;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getPatientId() {
        return patientId;
    }

    @Override
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String getDateOfVisit() {
        return dateOfVisit;
    }

    @Override
    public void setDateOfVisit(String dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    @Override
    public String getChiefComplaint() {
        return chiefComplaint;
    }

    @Override
    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    @Override
    public Integer getWeeksPregnant() {
        return weeksPregnant;
    }

    @Override
    public void setWeeksPregnant(Integer weeksPregnant) {
        this.weeksPregnant = weeksPregnant;
    }

    @Override
    public Boolean getIsPregnant() {
        return isPregnant;
    }

    @Override
    public void setIsPregnant(Boolean isPregnant) {
        this.isPregnant = isPregnant;
    }
}

