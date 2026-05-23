package me.nimnakse.water_management.service_requests.entity;

public enum ServiceRequestEventType {
    CREATED,
    UPDATED,
    SUBMITTED,
    PAUSED,
    RESUMED,
    MOBILE_UPDATED,
    WORK_ORDER_SAVED,
    WORK_ORDER_COMPLETED,
    SOLUTION_SAVED,
    SOLUTION_APPLIED,
    SOLUTION_PENDING,
    MATERIAL_CONSUMPTION_SAVED,
    INVOICE_CREATED,
    FEEDBACK_SAVED,
    RESOLVED,
    CLOSED,
    CANCELLED
}
