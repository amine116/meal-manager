package com.amine.mealmanager;

public class MemberSuggestion {
    private String name, suggestedBy, date, pushId;

    public MemberSuggestion(){}

    public MemberSuggestion(String name, String suggestedBy, String date, String pushId) {
        this.name = name;
        this.suggestedBy = suggestedBy;
        this.date = date;
        this.pushId = pushId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuggestedBy() {
        return suggestedBy;
    }

    public void setSuggestedBy(String suggestedBy) {
        this.suggestedBy = suggestedBy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}
