package com.amine.mealmanager;

public class UserChangeInfo {

    private String username, currentPassword, newPassword,
            lastActivity, realPhone, tempPhone, countryName;

    public UserChangeInfo(){}

    public UserChangeInfo(String username, String currentPassword, String newPassword, String lastActivity,
                          String realPhone, String tempPhone, String countryName) {
        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.lastActivity = lastActivity;
        this.realPhone = realPhone;
        this.tempPhone = tempPhone;
        this.countryName = countryName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getRealPhone() {
        return realPhone;
    }

    public void setRealPhone(String realPhone) {
        this.realPhone = realPhone;
    }

    public String getTempPhone() {
        return tempPhone;
    }

    public void setTempPhone(String tempPhone) {
        this.tempPhone = tempPhone;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
