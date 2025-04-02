package com.example.vehiclerentalapp.models;

import com.google.gson.annotations.SerializedName;

public class CustomerRegistration {
    private AccountInfo accountInfo;
    private ContactInfo contactInfo;
    private AddressInfo addressInfo;
    private BankInfo bankInfo;
    private BusinessInfo businessInfo;

    // Default constructor allowing partial initialization (required for Retrofit)
    public CustomerRegistration() {
        this.accountInfo = null;
        this.contactInfo = null;
        this.addressInfo = null;
        this.bankInfo = null;
        this.businessInfo = null;
    }

    // Nested classes with default constructors and getters/setters
    public static class AccountInfo {
        private String username;
        private String password;

        // Default constructor (required for Retrofit)
        public AccountInfo() {}

        public AccountInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ContactInfo {
        @SerializedName("contactName") // Matches your server's JSON key
        private String contactName;
        @SerializedName("mobileNumber") // Matches your server's JSON key
        private String mobileNumber;
        @SerializedName("emailId") // Matches your server's JSON key
        private String emailId;

        // Default constructor (required for Retrofit)
        public ContactInfo() {}

        public ContactInfo(String contactName, String mobileNumber, String emailId) {
            this.contactName = contactName;
            this.mobileNumber = mobileNumber;
            this.emailId = emailId;
        }

        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getEmailId() { return emailId; }
        public void setEmailId(String emailId) { this.emailId = emailId; }
    }

    public static class AddressInfo {
        private String doorNo;
        private String streetName;
        private String locality;
        private String city;
        private String district;
        private String state;
        private String country;
        private String postalCode;
        private String latitude;
        private String longitude;

        // Default constructor (required for Retrofit)
        public AddressInfo() {}

        public AddressInfo(String doorNo, String streetName, String locality, String city,
                           String district, String state, String country, String postalCode,
                           String latitude, String longitude) {
            this.doorNo = doorNo;
            this.streetName = streetName;
            this.locality = locality;
            this.city = city;
            this.district = district;
            this.state = state;
            this.country = country;
            this.postalCode = postalCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getDoorNo() { return doorNo; }
        public void setDoorNo(String doorNo) { this.doorNo = doorNo; }
        public String getStreetName() { return streetName; }
        public void setStreetName(String streetName) { this.streetName = streetName; }
        public String getLocality() { return locality; }
        public void setLocality(String locality) { this.locality = locality; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
    }

    public static class BankInfo {
        private String accountHolderName;
        private String bankName;
        private String branchName;
        private String ifscCode;
        private String upiNumber;
        private String accountNumber; // Added new field

        // Default constructor (required for Retrofit)
        public BankInfo() {}

        public BankInfo(String accountHolderName, String bankName, String branchName,
                        String ifscCode, String upiNumber, String accountNumber) {
            this.accountHolderName = accountHolderName;
            this.bankName = bankName;
            this.branchName = branchName;
            this.ifscCode = ifscCode;
            this.upiNumber = upiNumber;
            this.accountNumber = accountNumber;
        }

        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        public String getIfscCode() { return ifscCode; }
        public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
        public String getUpiNumber() { return upiNumber; }
        public void setUpiNumber(String upiNumber) { this.upiNumber = upiNumber; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    }

    public static class BusinessInfo {
        private String businessName;
        private String registerNumber;
        private String gstin;
        private String logoPath;
        private String licensePath;

        // Default constructor (required for Retrofit)
        public BusinessInfo() {}

        public BusinessInfo(String businessName, String registerNumber, String gstin,
                            String logoPath, String licensePath) {
            this.businessName = businessName;
            this.registerNumber = registerNumber;
            this.gstin = gstin;
            this.logoPath = logoPath;
            this.licensePath = licensePath;
        }

        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getRegisterNumber() { return registerNumber; }
        public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }
        public String getGstin() { return gstin; }
        public void setGstin(String gstin) { this.gstin = gstin; }
        public String getLogoPath() { return logoPath; }
        public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
        public String getLicensePath() { return licensePath; }
        public void setLicensePath(String licensePath) { this.licensePath = licensePath; }
    }

    // Getters
    public AccountInfo getAccountInfo() { return accountInfo; }
    public ContactInfo getContactInfo() { return contactInfo; }
    public AddressInfo getAddressInfo() { return addressInfo; }
    public BankInfo getBankInfo() { return bankInfo; }
    public BusinessInfo getBusinessInfo() { return businessInfo; }

    // Setters for incremental updates
    public void setAccountInfo(AccountInfo accountInfo) { this.accountInfo = accountInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }
    public void setAddressInfo(AddressInfo addressInfo) { this.addressInfo = addressInfo; }
    public void setBankInfo(BankInfo bankInfo) { this.bankInfo = bankInfo; }
    public void setBusinessInfo(BusinessInfo businessInfo) { this.businessInfo = businessInfo; }
}