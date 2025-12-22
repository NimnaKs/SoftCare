package me.nimnakse.water_management.societies.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.societies.dto.request.SocietyCreateReq;
import me.nimnakse.water_management.societies.dto.request.SocietyUpdateReq;
import me.nimnakse.water_management.societies.dto.response.SocietyRes;
import me.nimnakse.water_management.societies.entity.Society;
import me.nimnakse.water_management.societies.repository.SocietyRepository;
import me.nimnakse.water_management.societies.service.SocietyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocietyServiceImpl implements SocietyService {
    private final SocietyRepository societyRepository;
    private final ConnectionRepository connectionRepository;

    public SocietyServiceImpl(SocietyRepository societyRepository,
                              ConnectionRepository connectionRepository) {
        this.societyRepository = societyRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public SocietyRes create(SocietyCreateReq request) {
        if (societyRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Society already exists");
        }
        Society society = new Society();
        society.setName(request.name().trim());
        return toResponse(societyRepository.save(society));
    }

    @Transactional
    @Override
    public SocietyRes update(Long id, SocietyUpdateReq request) {
        Society society = societyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Society not found", ErrorCode.NOT_FOUND));
        if (societyRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BadRequestException("Society already exists");
        }
        society.setName(request.name().trim());
        return toResponse(societyRepository.save(society));
    }

    @Transactional(readOnly = true)
    @Override
    public SocietyRes getById(Long id) {
        Society society = societyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Society not found", ErrorCode.NOT_FOUND));
        return toResponse(society);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SocietyRes> list() {
        return societyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Society society = societyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Society not found", ErrorCode.NOT_FOUND));
        if (connectionRepository.existsBySocietyId(id)) {
            throw new BadRequestException("Society is linked to connections and cannot be deleted");
        }
        societyRepository.delete(society);
    }

    private SocietyRes toResponse(Society society) {
        return new SocietyRes(
                society.getId(),
                society.getName(),
                society.getCreatedAt(),
                society.getUpdatedAt()
        );
    }
}
