package com.example.hospitalmap;

public enum DepartmentCode {
    IM("01", "내과"),
    NP("03", "정신건강의학과"),
    OS("05", "정형외과"),
    NS("06", "신경외과"),
    PS("08", "성형외과"),
    OBGY("10", "산부인과"),
    PED("11", "소아청소년과"),
    EY("12", "안과"),
    ENT("13", "이비인후과"),
    DER("14", "피부과"),
    UR("15", "비뇨기과"),
    DEN("49", "치과"),
    OMC("90", "한의원");

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
