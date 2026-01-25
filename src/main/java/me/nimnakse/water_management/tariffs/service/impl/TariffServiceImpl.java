package me.nimnakse.water_management.tariffs.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.tariffs.dto.request.TariffCreateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffUpdateReq;
import me.nimnakse.water_management.tariffs.dto.response.TariffRes;
import me.nimnakse.water_management.tariffs.entity.Tariff;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.tariffs.service.TariffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TariffServiceImpl implements TariffService {
    private final TariffRepository tariffRepository;
    private final ConnectionRepository connectionRepository;

    public TariffServiceImpl(TariffRepository tariffRepository,
            ConnectionRepository connectionRepository) {
        this.tariffRepository = tariffRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public TariffRes create(TariffCreateReq request) {
        if (tariffRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Tariff already exists", "ගාස්තු ක්‍රමය දැනටමත් පවතී");
        }
        Tariff tariff = new Tariff();
        tariff.setName(request.name().trim());
        tariff.setDescription(trimToNull(request.description()));
        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional
    @Override
    public TariffRes update(Long id, TariffUpdateReq request) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (tariffRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BadRequestException("Tariff already exists", "ගාස්තු ක්‍රමය දැනටමත් පවතී");
        }
        tariff.setName(request.name().trim());
        tariff.setDescription(trimToNull(request.description()));
        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional(readOnly = true)
    @Override
    public TariffRes getById(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        return toResponse(tariff);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TariffRes> list() {
        return tariffRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (connectionRepository.existsByTariffId(id)) {
            throw new BadRequestException("Tariff is linked to connections and cannot be deleted",
                    "ගාස්තු ක්‍රමය සම්බන්ධතා සමඟ සම්බන්ධ වී ඇති බැවින් මකා දැමිය නොහැක");
        }
        tariffRepository.delete(tariff);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TariffRes toResponse(Tariff tariff) {
        return new TariffRes(
                tariff.getId(),
                tariff.getName(),
                tariff.getDescription(),
                tariff.getCreatedAt(),
                tariff.getUpdatedAt());
    }
}
