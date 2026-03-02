package me.nimnakse.water_management.cash_accounts.service;

import jakarta.annotation.PostConstruct;
import me.nimnakse.water_management.receipts.PaymentMethodCode;
import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Component
public class PaymentMethodSeedService {

    private static final Map<PaymentMethodCode, String> DEFAULT_LABELS = new EnumMap<>(PaymentMethodCode.class);

    static {
        DEFAULT_LABELS.put(PaymentMethodCode.CASH, "Cash");
        DEFAULT_LABELS.put(PaymentMethodCode.CHEQUE, "Cheque");
        DEFAULT_LABELS.put(PaymentMethodCode.CARD, "Card");
        DEFAULT_LABELS.put(PaymentMethodCode.E_TRANSFER, "E-Transfer");
    }

    private final PaymentMethodLookupRepository repository;

    public PaymentMethodSeedService(PaymentMethodLookupRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    @Transactional
    public void ensureDefaults() {
        for (Map.Entry<PaymentMethodCode, String> entry : DEFAULT_LABELS.entrySet()) {
            PaymentMethodCode code = entry.getKey();
            String label = entry.getValue();

            repository.findByCodeIgnoreCase(code.name())
                    .ifPresentOrElse(existing -> {
                        existing.setName(label);
                        existing.setIsActive(Boolean.TRUE);
                        // no need to repository.save(existing) because entity is managed in TX
                    }, () -> {
                        PaymentMethodLookup lookup = new PaymentMethodLookup();
                        lookup.setCode(code.name());
                        lookup.setName(label);
                        lookup.setIsActive(Boolean.TRUE);
                        repository.save(lookup);
                    });
        }
    }
}