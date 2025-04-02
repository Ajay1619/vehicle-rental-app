package com.example.vehiclerentalapp.models;

import com.google.gson.annotations.SerializedName;

public class GuestRegistration {

    private PersonalInfo personalInfo;
    private AccountInfo accountInfo;

    // Default constructor (required for Retrofit)
    public GuestRegistration() {
        this.personalInfo = null;
        this.accountInfo = null;
    }

    // Constructor with all fields
    public GuestRegistration(PersonalInfo personalInfo, AccountInfo accountInfo) {
        this.personalInfo = personalInfo;
        this.accountInfo = accountInfo;
    }

    // Nested class for PersonalInfo
    public static class PersonalInfo {
        @SerializedName("guest_name")
        private String guestName;

        @SerializedName("guest_mobile_number")
        private String guestMobileNumber;

        @SerializedName("guest_email_id")
        private String guestEmailId;

        // Default constructor (required for Retrofit)
        public PersonalInfo() {}

        // Constructor with all fields
        public PersonalInfo(String guestName, String guestMobileNumber, String guestEmailId) {
            this.guestName = guestName;
            this.guestMobileNumber = guestMobileNumber;
            this.guestEmailId = guestEmailId;
        }

        // Getters and Setters
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getGuestMobileNumber() { return guestMobileNumber; }
        public void setGuestMobileNumber(String guestMobileNumber) { this.guestMobileNumber = guestMobileNumber; }
        public String getGuestEmailId() { return guestEmailId; }
        public void setGuestEmailId(String guestEmailId) { this.guestEmailId = guestEmailId; }
    }

    // Nested class for AccountInfo
    public static class AccountInfo {
        @SerializedName("account_username")
        private String accountUsername;

        @SerializedName("account_password")
        private String accountPassword;

        @SerializedName("account_portal_type")
        private int accountPortalType;

        @SerializedName("account_code")
        private String accountCode;

        @SerializedName("master_key")
        private String masterKey;

        @SerializedName("salt_value")
        private String saltValue;

        @SerializedName("iterations_value")
        private Integer iterationsValue; // Integer to allow null (optional field)

        @SerializedName("iv_value")
        private String ivValue;

        // Default constructor (required for Retrofit)
        public AccountInfo() {}

        // Constructor with all fields
        public AccountInfo(String accountUsername, String accountPassword, int accountPortalType,
                           String accountCode, String masterKey, String saltValue, Integer iterationsValue, String ivValue) {
            this.accountUsername = accountUsername;
            this.accountPassword = accountPassword;
            this.accountPortalType = accountPortalType;
            this.accountCode = accountCode;
            this.masterKey = masterKey;
            this.saltValue = saltValue;
            this.iterationsValue = iterationsValue;
            this.ivValue = ivValue;
        }

        // Getters and Setters
        public String getAccountUsername() { return accountUsername; }
        public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }
        public String getAccountPassword() { return accountPassword; }
        public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }
        public int getAccountPortalType() { return accountPortalType; }
        public void setAccountPortalType(int accountPortalType) { this.accountPortalType = accountPortalType; }
        public String getAccountCode() { return accountCode; }
        public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
        public String getMasterKey() { return masterKey; }
        public void setMasterKey(String masterKey) { this.masterKey = masterKey; }
        public String getSaltValue() { return saltValue; }
        public void setSaltValue(String saltValue) { this.saltValue = saltValue; }
        public Integer getIterationsValue() { return iterationsValue; }
        public void setIterationsValue(Integer iterationsValue) { this.iterationsValue = iterationsValue; }
        public String getIvValue() { return ivValue; }
        public void setIvValue(String ivValue) { this.ivValue = ivValue; }
    }

    // Getters and Setters for GuestRegistration
    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }
    public AccountInfo getAccountInfo() { return accountInfo; }
    public void setAccountInfo(AccountInfo accountInfo) { this.accountInfo = accountInfo; }
}