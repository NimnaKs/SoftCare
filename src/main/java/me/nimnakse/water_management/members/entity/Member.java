package me.nimnakse.water_management.members.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "members")
public class Member extends BaseEntity {
    @Column(name = "membership_code", nullable = false, unique = true)
    private String membershipCode;

    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false)
    private MemberType membershipType;

    @Column(name = "salutation")
    private String salutation;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "corporate_name")
    private String corporateName;

    @Column(name = "nic_old")
    private String nicOld;

    @Column(name = "nic_new")
    private String nicNew;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "dp_nic_front_url")
    private String dpNicFrontUrl;

    @Column(name = "dp_nic_rear_url")
    private String dpNicRearUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "brc_document_url")
    private String brcDocumentUrl;

    public String getMembershipCode() {
        return membershipCode;
    }

    public void setMembershipCode(String membershipCode) {
        this.membershipCode = membershipCode;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public MemberType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MemberType membershipType) {
        this.membershipType = membershipType;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public String getNicOld() {
        return nicOld;
    }

    public void setNicOld(String nicOld) {
        this.nicOld = nicOld;
    }

    public String getNicNew() {
        return nicNew;
    }

    public void setNicNew(String nicNew) {
        this.nicNew = nicNew;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDpNicFrontUrl() {
        return dpNicFrontUrl;
    }

    public void setDpNicFrontUrl(String dpNicFrontUrl) {
        this.dpNicFrontUrl = dpNicFrontUrl;
    }

    public String getDpNicRearUrl() {
        return dpNicRearUrl;
    }

    public void setDpNicRearUrl(String dpNicRearUrl) {
        this.dpNicRearUrl = dpNicRearUrl;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getBrcDocumentUrl() {
        return brcDocumentUrl;
    }

    public void setBrcDocumentUrl(String brcDocumentUrl) {
        this.brcDocumentUrl = brcDocumentUrl;
    }
}
