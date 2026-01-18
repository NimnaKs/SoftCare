package me.nimnakse.water_management.suppliers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {
    @Column(name = "org_unit_id")
    private Long orgUnitId;

    @Column(name = "supplier_code", unique = true)
    private String supplierCode;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(name = "brc_number", length = 100)
    private String brcNumber;

    @Column(length = 20)
    private String nic;

    @Column(name = "mobile_number_1", length = 20)
    private String mobileNumber1;

    @Column(name = "mobile_number_2", length = 20)
    private String mobileNumber2;

    @Column(name = "telephone_number", length = 20)
    private String telephoneNumber;

    @Column(length = 255)
    private String email;

    @Column(name = "liability_account_id")
    private Long liabilityAccountId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
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

    public String getBrcNumber() {
        return brcNumber;
    }

    public void setBrcNumber(String brcNumber) {
        this.brcNumber = brcNumber;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getMobileNumber1() {
        return mobileNumber1;
    }

    public void setMobileNumber1(String mobileNumber1) {
        this.mobileNumber1 = mobileNumber1;
    }

    public String getMobileNumber2() {
        return mobileNumber2;
    }

    public void setMobileNumber2(String mobileNumber2) {
        this.mobileNumber2 = mobileNumber2;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getLiabilityAccountId() {
        return liabilityAccountId;
    }

    public void setLiabilityAccountId(Long liabilityAccountId) {
        this.liabilityAccountId = liabilityAccountId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
