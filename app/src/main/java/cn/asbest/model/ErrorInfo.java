package cn.asbest.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by chenyanlan on 2016/11/17.
 */

public class ErrorInfo {

    private String id;
    private String deviceInfo;
    private String softwareInfo;
    private String errorInfo;
    private Date createdAt;
    private Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getSoftwareInfo() {
        return softwareInfo;
    }

    public void setSoftwareInfo(String softwareInfo) {
        this.softwareInfo = softwareInfo;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
