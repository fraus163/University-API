package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.web.dto.PositionRequest;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
import com.fraus.spring.universityapi.position.web.mapper.PositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        var positionToSave = positionMapper.toEntity(request);
        var createdPosition = positionRepository.save(positionToSave);
        return positionMapper.toResponse(createdPosition);
    }

    public Page<PositionResponse> getAllPositions(Pageable pageable) {
        var foundedPositions = positionRepository.findAll(pageable);
        return positionMapper.toResponsePage(foundedPositions);
    }

    @Transactional
    public PositionResponse updatePositionById(
            Short id,
            PositionRequest request
    ) {
        var foundedPosition = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Должность не найдена",
                        "Position not found: id=" + id
                ));
        foundedPosition.setName(request.name());
        return positionMapper.toResponse(foundedPosition);
    }

    @Transactional
    public void deletePositionById(Short id) {
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Должность не найдена",
                    "Position not found: id=" + id
            );
        }
        positionRepository.deleteById(id);
    }

    public PositionResponse getPositionById(Short id) {
        return positionRepository.findById(id)
                .map(positionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Должность не найдена",
                        "Position not found: id=" + id
                ));
    }
}
