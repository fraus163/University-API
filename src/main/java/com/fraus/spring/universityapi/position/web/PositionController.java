package com.fraus.spring.universityapi.position.web;

import com.fraus.spring.universityapi.position.domain.PositionService;
import com.fraus.spring.universityapi.position.web.dto.PositionRequest;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@Slf4j
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<PositionResponse> createPosition(
            @RequestBody @Valid PositionRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createPosition is called: request={}", request);
        PositionResponse createdPosition = positionService.createPosition(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/positions/{id}")
                .buildAndExpand(createdPosition.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdPosition);
    }

    @GetMapping
    public Page<PositionResponse> getAllPositions(
            @PageableDefault(page = 0, size = 10, sort = "name")
            Pageable pageable
    ) {
        log.info("Controller getPositionsByFilter is called");
        return positionService.getAllPositions(pageable);
    }

    @GetMapping("/{id}")
    public PositionResponse getPositionById(
            @PathVariable Short id
    ) {
        log.info("Controller getPositionById is called: id={}", id);
        return positionService.getPositionById(id);
    }

    @PutMapping("/{id}")
    public PositionResponse updatePositionById(
            @PathVariable Short id,
            @RequestBody @Valid PositionRequest request
    ) {
        log.info("Controller updatePositionById is called: id={}", id);
        return positionService.updatePositionById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePositionById(
            @PathVariable Short id
    ) {
        log.info("Controller deletePositionById is called: id={}", id);
        positionService.deletePositionById(id);
    }
}
