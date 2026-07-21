package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.subject.web.dto.SubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.subject.web.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Transactional
    public SubjectResponse createSubject(
            SubjectRequest request
    ) {
        var subjectToSave = subjectMapper.toEntity(request);
        var createdSubject = subjectRepository.save(subjectToSave);
        return subjectMapper.toResponse(createdSubject);
    }

    public SubjectResponse getSubjectById(
            Integer id
    ) {
        return subjectRepository
                .findById(id)
                .map(subjectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина не найдена",
                        "Subject not found: subjectId=" + id
                ));
    }

    public Page<SubjectResponse> getSubjectsByFilter(
            Pageable pageable,
            String name
    ) {
        var foundedSubjects = subjectRepository.findSubjectsByFilter(
                pageable,
                name
        );
        return subjectMapper.toResponsePage(foundedSubjects);
    }

    @Transactional
    public SubjectResponse updateSubjectById(
            Integer id,
            SubjectRequest request
    ) {
        var foundedSubject = subjectRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина не найдена",
                        "Subject not found: subjectId=" + id
                ));
        foundedSubject.setName(request.name());
        foundedSubject.setDescription(request.description());
        return subjectMapper.toResponse(foundedSubject);
    }

    @Transactional
    public void deleteSubjectById(Integer id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Дисциплина не найдена",
                    "Subject not found: subjectId=" + id
            );
        }
        subjectRepository.deleteById(id);
    }
}
