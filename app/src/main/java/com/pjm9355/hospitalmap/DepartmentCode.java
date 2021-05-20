package com.pjm9355.hospitalmap;

public enum DepartmentCode {
    IM("01", "내과"),
    NR("02", "신경과"),
    NP("03", "정신건강의학과"),
    GS("04", "외과"),
    OS("05", "정형외과"),
    NS("06", "신경외과"),
    CS("07", "흉부외과"),
    PS("08", "성형외과"),
    ANE("09", "마취통증의학과"),
    OBGY("10", "산부인과"),
    PED("11", "소아청소년과"),
    EY("12", "안과"),
    ENT("13", "이비인후과"),
    DER("14", "피부과"),
    UR("15", "비뇨기과"),
    RAD("16", "영상의학과"),
    RO("17", "방사선종양학과"),
    LM("19", "진단검사의학과"),
    RM("21", "재활의학과"),
    FM("23", "가정의학과"),
    DEN("49", "치과"),
    OMC("80", "한의원");

    final private String code;
    final private String departmentName;

    private DepartmentCode(String code, String departmentName) {
        this.code = code;
        this.departmentName = departmentName;
    }

    public String getCode() {
        return code;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}
