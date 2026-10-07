package org.example.model;

import java.time.LocalDate;

public class Member {

    private int memberId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate registrationDate;

    public Member() {
    }

    public Member(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Member(int memberId, String name, String email,
                  String phone, String address,
                  LocalDate registrationDate) {

        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}