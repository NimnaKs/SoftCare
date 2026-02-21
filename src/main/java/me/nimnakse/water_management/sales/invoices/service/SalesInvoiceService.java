package me.nimnakse.water_management.sales.invoices.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.sales.invoices.dto.SalesInvoicePostAction;
import me.nimnakse.water_management.sales.invoices.dto.request.RecurringInvoiceToggleReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceConnectionPreviewReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceCreateReq;
import me.nimnakse.water_management.sales.invoices.dto.response.RecurringInvoiceRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceConnectionPreviewRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePostRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePrintableConnectionsRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceSummaryRes;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;

public interface SalesInvoiceService {
    SalesInvoiceConnectionPreviewRes previewConnections(SalesInvoiceConnectionPreviewReq request, String sort);

    SalesInvoiceRes createDraftInvoice(SalesInvoiceCreateReq request);

    SalesInvoiceRes getById(Long id);

    PageResponse<SalesInvoiceSummaryRes> listInvoices(SaleType saleType, String invoiceNo, String connectionAccountNo, java.time.LocalDate dateFrom, java.time.LocalDate dateTo, int page, int size);

    SalesInvoicePostRes postInvoice(Long id, SalesInvoicePostAction action);

    PageResponse<RecurringInvoiceRes> listRecurringInvoices(Long billingZoneId, int page, int size);

    RecurringInvoiceRes toggleRecurring(Long id, RecurringInvoiceToggleReq request);

    SalesInvoicePrintableConnectionsRes printConnections(Long id, int page, int size);

    SalesInvoiceRes settleNonCustomerInvoice(Long id);

    SalesInvoiceRes reverseNonCustomerInvoice(Long id);
}


