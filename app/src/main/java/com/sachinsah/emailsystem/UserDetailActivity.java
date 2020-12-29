package com.sachinsah.emailsystem;

public class UserDetailActivity {

    String name;
    String address;
    String phoneNo;
    String aaddharNo;
    String serviceType;
    String gender;
    String imgUrl;
    String emailId;

    UserDetailActivity(String imgUrl, String name, String address, String phoneNo, String aaddharNo, String serviceType, String gender, String emailId){
        this.imgUrl = imgUrl;
        this.name = name;
        this.aaddharNo = aaddharNo;
        this.serviceType = serviceType;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.address = address;
        this.emailId = emailId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAaddharNo() {
        return aaddharNo;
    }

    public void setAaddharNo(String aaddharNo) {
        this.aaddharNo = aaddharNo;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
