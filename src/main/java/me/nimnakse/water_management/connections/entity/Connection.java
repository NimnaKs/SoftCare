package me.nimnakse.water_management.connections.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "connections")
public class Connection extends BaseEntity {
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "premises_id", nullable = false)
    private Long premisesId;

    @Column(name = "billing_zone_id", nullable = false)
    private Long billingZoneId;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status;

    @Column(name = "line1_id")
    private Long line1Id;

    @Column(name = "line2_id")
    private Long line2Id;

    @Column(name = "line3_id")
    private Long line3Id;

    @Column(name = "line4_id")
    private Long line4Id;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "house_name")
    private String houseName;

    @Column(name = "house_nickname")
    private String houseNickname;

    @Column(name = "gn_division_id")
    private Long gnDivisionId;

    @Column(name = "valve_id")
    private Long valveId;

    @Column(name = "society_id")
    private Long societyId;

    @Column(name = "cluster_id")
    private Long clusterId;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "secondary_number")
    private String secondaryNumber;

    @Column(name = "fixed_line_number")
    private String fixedLineNumber;

    @Column(name = "tariff_id", nullable = false)
    private Long tariffId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getPremisesId() {
        return premisesId;
    }

    public void setPremisesId(Long premisesId) {
        this.premisesId = premisesId;
    }

    public Long getBillingZoneId() {
        return billingZoneId;
    }

    public void setBillingZoneId(Long billingZoneId) {
        this.billingZoneId = billingZoneId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public Long getLine1Id() {
        return line1Id;
    }

    public void setLine1Id(Long line1Id) {
        this.line1Id = line1Id;
    }

    public Long getLine2Id() {
        return line2Id;
    }

    public void setLine2Id(Long line2Id) {
        this.line2Id = line2Id;
    }

    public Long getLine3Id() {
        return line3Id;
    }

    public void setLine3Id(Long line3Id) {
        this.line3Id = line3Id;
    }

    public Long getLine4Id() {
        return line4Id;
    }

    public void setLine4Id(Long line4Id) {
        this.line4Id = line4Id;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getHouseNickname() {
        return houseNickname;
    }

    public void setHouseNickname(String houseNickname) {
        this.houseNickname = houseNickname;
    }

    public Long getGnDivisionId() {
        return gnDivisionId;
    }

    public void setGnDivisionId(Long gnDivisionId) {
        this.gnDivisionId = gnDivisionId;
    }

    public Long getValveId() {
        return valveId;
    }

    public void setValveId(Long valveId) {
        this.valveId = valveId;
    }

    public Long getSocietyId() {
        return societyId;
    }

    public void setSocietyId(Long societyId) {
        this.societyId = societyId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getSecondaryNumber() {
        return secondaryNumber;
    }

    public void setSecondaryNumber(String secondaryNumber) {
        this.secondaryNumber = secondaryNumber;
    }

    public String getFixedLineNumber() {
        return fixedLineNumber;
    }

    public void setFixedLineNumber(String fixedLineNumber) {
        this.fixedLineNumber = fixedLineNumber;
    }

    public Long getTariffId() {
        return tariffId;
    }

    public void setTariffId(Long tariffId) {
        this.tariffId = tariffId;
    }
}
