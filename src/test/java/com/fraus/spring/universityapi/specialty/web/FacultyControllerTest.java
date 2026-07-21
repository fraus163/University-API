package com.fraus.spring.universityapi.specialty.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyService;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyRequest;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecialtyController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование SpecialtyController")
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpecialtyService specialtyService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private FacultyResponse createFacultyResponse(Short id) {
        return new FacultyResponse(id, (short) 101, "ФИТУ");
    }

    private SpecialtyRequest createValidRequest() {
        return new SpecialtyRequest("Программная инженерия", DegreeType.BACHELOR, (short) 10);
    }

    private SpecialtyResponse createTestResponse(Short id) {
        return new SpecialtyResponse(id, "Программная инженерия", DegreeType.BACHELOR, createFacultyResponse((short) 10));
    }

    @Nested
    @DisplayName("POST /api/v1/specialties - Создание специальности")
    class CreateSpecialty {

        @Test
        @DisplayName("Должен вернуть 201 Created при вызове с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateSpecialtyAndReturn201ForAdmin() throws Exception {
            SpecialtyRequest request = createValidRequest();
            SpecialtyResponse response = createTestResponse((short) 1);

            when(specialtyService.createSpecialty(any(SpecialtyRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/specialties")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/specialties/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Программная инженерия"))
                    .andExpect(jsonPath("$.degree").value(DegreeType.BACHELOR.name()))
                    .andExpect(jsonPath("$.faculty.id").value(10))
                    .andExpect(jsonPath("$.faculty.name").value("ФИТУ"));

            verify(specialtyService, times(1)).createSpecialty(any(SpecialtyRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания ролями STUDENT или TEACHER")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenCreatedByStudent() throws Exception {
            SpecialtyRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/specialties")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(specialtyService, never()).createSpecialty(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/specialties - Получение специальностей")
    class GetSpecialties {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу специальностей (без авторизации/любая роль)")
        @WithMockUser
        void shouldReturnPageOfSpecialties() throws Exception {
            Short facultyId = 10;
            SpecialtyResponse response = createTestResponse((short) 1);
            Page<SpecialtyResponse> responsePage = new PageImpl<>(List.of(response));

            when(specialtyService.getSpecialtiesByFilter(any(Pageable.class), eq(facultyId))).thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/specialties")
                            .param("facultyId", facultyId.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].faculty.name").value("ФИТУ"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и специальность по ID")
        @WithMockUser
        void shouldReturnSpecialtyById() throws Exception {
            Short id = 1;
            SpecialtyResponse response = createTestResponse(id);

            when(specialtyService.getSpecialtyById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/specialties/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Программная инженерия"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если специальность не найдена")
        @WithMockUser
        void shouldReturn404WhenSpecialtyNotFound() throws Exception {
            Short id = 99;
            when(specialtyService.getSpecialtyById(id))
                    .thenThrow(new ResourceNotFoundException("Специальность не найдена", "Specialty not found: id=" + id));

            mockMvc.perform(get("/api/v1/specialties/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Специальность не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/specialties/{id} - Обновление специальности")
    class UpdateSpecialty {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateSpecialtySuccessfully() throws Exception {
            Short id = 1;
            SpecialtyRequest request = createValidRequest();
            SpecialtyResponse response = createTestResponse(id);

            when(specialtyService.updateSpecialtyById(eq(id), any(SpecialtyRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/specialties/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/specialties/{id} - Удаление специальности")
    class DeleteSpecialty {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteSpecialtyAndReturn204ForAdmin() throws Exception {
            Short id = 1;
            doNothing().when(specialtyService).deleteSpecialtyById(id);

            mockMvc.perform(delete("/api/v1/specialties/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(specialtyService, times(1)).deleteSpecialtyById(id);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления без роли ADMIN")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403WhenDeletingByTeacher() throws Exception {
            Short id = 1;

            mockMvc.perform(delete("/api/v1/specialties/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(specialtyService, never()).deleteSpecialtyById(any());
        }
    }
}