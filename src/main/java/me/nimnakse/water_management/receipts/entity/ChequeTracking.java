package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "cheque_tracking")
public class ChequeTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cheque_no", nullable = false, unique = true)
    private String chequeNo;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChequeTrackingStatus status = ChequeTrackingStatus.RECEIVED;

    @Column(name = "status_date", nullable = false)
    private Instant statusDate;

    @Column(name = "note", length = 500)
    private String note;

    public Long getId() { return id; }
    public String getChequeNo() { return chequeNo; }
    public void setChequeNo(String chequeNo) { this.chequeNo = chequeNo; }
    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }
    public ChequeTrackingStatus getStatus() { return status; }
    public void setStatus(ChequeTrackingStatus status) { this.status = status; }
    public Instant getStatusDate() { return statusDate; }
    public void setStatusDate(Instant statusDate) { this.statusDate = statusDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
