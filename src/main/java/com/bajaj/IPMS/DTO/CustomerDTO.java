package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.Customer;
import jakarta.persistence.*;

import java.time.LocalDate;

public class CustomerDTO {

    private long id;
    private String customerCode;
    private Long user_id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String kycStatus;
    private Long createdBy;

    public CustomerDTO(Customer customer) {
        this.id = customer.getId();
        this.customerCode = customer.getCustomerCode();
        this.user_id = customer.getUser().getId();
        this.fullName = customer.getFullName();
        this.dateOfBirth = customer.getDateOfBirth();
        this.gender = customer.getGender();
        this.phone = customer.getPhone();
        this.address = customer.getAddress();
        this.kycStatus = customer.getKycStatus();
        this.createdBy = customer.getCreatedBy();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
