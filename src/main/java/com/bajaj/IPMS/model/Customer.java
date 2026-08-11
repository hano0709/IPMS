package com.bajaj.IPMS.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String customerCode;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String fullName;
    private Date dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String kycStatus;

    public long getId() {
        return id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getKycStatus() {
        return kycStatus;
    }
}
