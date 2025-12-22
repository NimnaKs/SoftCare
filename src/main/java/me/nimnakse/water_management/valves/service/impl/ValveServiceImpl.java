package me.nimnakse.water_management.valves.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.valves.dto.request.ValveCreateReq;
import me.nimnakse.water_management.valves.dto.request.ValveUpdateReq;
import me.nimnakse.water_management.valves.dto.response.ValveRes;
import me.nimnakse.water_management.valves.entity.Valve;
import me.nimnakse.water_management.valves.repository.ValveRepository;
import me.nimnakse.water_management.valves.service.ValveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValveServiceImpl implements ValveService {
    private final ValveRepository valveRepository;
    private final ConnectionRepository connectionRepository;

    public ValveServiceImpl(ValveRepository valveRepository,
                            ConnectionRepository connectionRepository) {
        this.valveRepository = valveRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public ValveRes create(ValveCreateReq request) {
        if (valveRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Valve already exists");
        }
        Valve valve = new Valve();
        valve.setName(request.name().trim());
        return toResponse(valveRepository.save(valve));
    }

    @Transactional
    @Override
    public ValveRes update(Long id, ValveUpdateReq request) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", ErrorCode.NOT_FOUND));
        if (valveRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BadRequestException("Valve already exists");
        }
        valve.setName(request.name().trim());
        return toResponse(valveRepository.save(valve));
    }

    @Transactional(readOnly = true)
    @Override
    public ValveRes getById(Long id) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", ErrorCode.NOT_FOUND));
        return toResponse(valve);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ValveRes> list() {
        return valveRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", ErrorCode.NOT_FOUND));
        if (connectionRepository.existsByValveId(id)) {
            throw new BadRequestException("Valve is linked to connections and cannot be deleted");
        }
        valveRepository.delete(valve);
    }

    private ValveRes toResponse(Valve valve) {
        return new ValveRes(
                valve.getId(),
                valve.getName(),
                valve.getCreatedAt(),
                valve.getUpdatedAt()
        );
    }
}
