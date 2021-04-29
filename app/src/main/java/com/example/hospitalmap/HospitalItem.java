package com.example.hospitalmap;

import java.io.Serializable;

public class HospitalItem implements Serializable {
    private String hospName, classCodeName, address, telNo, hospUrl, estbDate;
    private Integer doctorTotalCnt, specialistDoctorCnt, generalDoctorCnt, residentCnt, internCnt;
    private Double xPos, yPos;

    public String getHospName() {
        return hospName;
    }

    public void setHospName(String hospName) {
        this.hospName = hospName;
    }

    public String getClassCodeName() {
        return classCodeName;
    }

    public void setClassCodeName(String classCodeName) {
        this.classCodeName = classCodeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getHospUrl() {
        return hospUrl;
    }

    public void setHospUrl(String hospUrl) {
        this.hospUrl = hospUrl;
    }

    public String getEstbDate() {
        return estbDate;
    }

    public void setEstbDate(String estbDate) {
        this.estbDate = estbDate;
    }

    public Integer getDoctorTotalCnt() {
        return doctorTotalCnt;
    }

    public void setDoctorTotalCnt(Integer doctorTotalCnt) {
        this.doctorTotalCnt = doctorTotalCnt;
    }

    public Integer getSpecialistDoctorCnt() {
        return specialistDoctorCnt;
    }

    public void setSpecialistDoctorCnt(Integer specialDoctorCnt) {
        this.specialistDoctorCnt = specialDoctorCnt;
    }

    public Integer getGeneralDoctorCnt() {
        return generalDoctorCnt;
    }

    public void setGeneralDoctorCnt(Integer generalDoctorCnt) {
        this.generalDoctorCnt = generalDoctorCnt;
    }

    public Integer getResidentCnt() {
        return residentCnt;
    }

    public void setResidentCnt(Integer residentCnt) {
        this.residentCnt = residentCnt;
    }

    public Integer getInternCnt() {
        return internCnt;
    }

    public void setInternCnt(Integer internCnt) {
        this.internCnt = internCnt;
    }

    public Double getXPos() {
        return xPos;
    }

    public void setXPos(Double xPos) {
        this.xPos = xPos;
    }

    public Double getYPos() {
        return yPos;
    }

    public void setYPos(Double yPos) {
        this.yPos = yPos;
    }
}
