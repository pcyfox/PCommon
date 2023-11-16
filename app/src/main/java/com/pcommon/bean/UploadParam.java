package com.pcommon.bean;

public class UploadParam {
    private int column;
    private int row;
    private int deviceType;
    private String ip;
    private int labId;
    private int logType;
    private String version;

    public UploadParam(int column, int row, int deviceType, String ip, int labId, int logType, String version) {
        this.column = column;
        this.row = row;
        this.deviceType = deviceType;
        this.ip = ip;
        this.labId = labId;
        this.logType = logType;
        this.version = version;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getLabId() {
        return labId;
    }

    public void setLabId(int labId) {
        this.labId = labId;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

