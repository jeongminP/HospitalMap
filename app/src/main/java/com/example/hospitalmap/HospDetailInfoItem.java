package com.example.hospitalmap;

import java.io.Serializable;

public class HospDetailInfoItem implements Serializable {
    private String place, parkXpnsYn, parkEtc, rcvWeek, lunchWeek, rcvSat, lunchSat, noTrmtHoli, noTrmtSun, emyDayYn, emyNgtYn;
    private Integer parkQty;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getParkXpnsYn() {
        return parkXpnsYn;
    }

    public void setParkXpnsYn(String parkXpnsYn) {
        this.parkXpnsYn = parkXpnsYn;
    }

    public String getParkEtc() {
        return parkEtc;
    }

    public void setParkEtc(String parkEtc) {
        this.parkEtc = parkEtc;
    }

    public String getRcvWeek() {
        return rcvWeek;
    }

    public void setRcvWeek(String rcvWeek) {
        this.rcvWeek = rcvWeek;
    }

    public String getLunchWeek() {
        return lunchWeek;
    }

    public void setLunchWeek(String lunchWeek) {
        this.lunchWeek = lunchWeek;
    }

    public String getRcvSat() {
        return rcvSat;
    }

    public void setRcvSat(String rcvSat) {
        this.rcvSat = rcvSat;
    }

    public String getLunchSat() {
        return lunchSat;
    }

    public void setLunchSat(String lunchSat) {
        this.lunchSat = lunchSat;
    }

    public String getNoTrmtHoli() {
        return noTrmtHoli;
    }

    public void setNoTrmtHoli(String noTrmtHoli) {
        this.noTrmtHoli = noTrmtHoli;
    }

    public String getNoTrmtSun() {
        return noTrmtSun;
    }

    public void setNoTrmtSun(String noTrmtSun) {
        this.noTrmtSun = noTrmtSun;
    }

    public String getEmyDayYn() {
        return emyDayYn;
    }

    public void setEmyDayYn(String emyDayYn) {
        this.emyDayYn = emyDayYn;
    }

    public String getEmyNgtYn() {
        return emyNgtYn;
    }

    public void setEmyNgtYn(String emyNgtYn) {
        this.emyNgtYn = emyNgtYn;
    }

    public Integer getParkQty() {
        return parkQty;
    }

    public void setParkQty(Integer parkQty) {
        this.parkQty = parkQty;
    }
}
