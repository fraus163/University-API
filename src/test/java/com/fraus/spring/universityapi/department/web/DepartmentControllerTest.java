package com.fraus.spring.universityapi.department.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.department.domain.DepartmentService;
import com.fraus.spring.universityapi.department.web.dto.DepartmentRequest;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(DepartmentController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование DepartmentController")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private DepartmentRequest createValidRequest() {
        return new DepartmentRequest((short) 10, "Кафедра ПОИТ");
    }

    private DepartmentResponse createTestResponse(Short id) {
        return new DepartmentResponse(id, "Кафедра ПОИТ", (short) 10);
    }

    @Nested
    @DisplayName("POST /api/v1/departments - Создание кафедры")
    class CreateDepartment {

        @Test
        @DisplayName("Должен вернуть 201 Created при вызове с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateDepartmentAndReturn201ForAdmin() throws Exception {
            DepartmentRequest request = createValidRequest();
            DepartmentResponse response = createTestResponse((short) 1);

            when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/departments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/departments/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Кафедра ПОИТ"))
                    .andExpect(jsonPath("$.facultyId").value(10));

            verify(departmentService, times(1)).createDepartment(any(DepartmentRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания с ролью STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenCreatedByStudent() throws Exception {
            DepartmentRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/departments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(departmentService, never()).createDepartment(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/departments - Получение кафедр")
    class GetDepartments {

        @Test
        @DisplayName("Должен вернуть 200 OK и список кафедр для роли STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnListOfDepartmentsForStudent() throws Exception {
            Short facultyId = 10;
            List<DepartmentResponse> responses = List.of(
                    createTestResponse((short) 1),
                    new DepartmentResponse((short) 2, "Кафедра ИСиТ", facultyId)
            );

            when(departmentService.getDepartmentsByFilter(facultyId)).thenReturn(responses);

            mockMvc.perform(get("/api/v1/departments")
                            .param("facultyId", facultyId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Кафедра ПОИТ"))
                    .andExpect(jsonPath("$[1].name").value("Кафедра ИСиТ"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и кафедру по ID")
        @WithMockUser(roles = "TEACHER")
        void shouldReturnDepartmentById() throws Exception {
            Short id = 1;
            DepartmentResponse response = createTestResponse(id);

            when(departmentService.getDepartmentById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/departments/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Кафедра ПОИТ"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если кафедра не найдена")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            Short id = 99;
            when(departmentService.getDepartmentById(id))
                    .thenThrow(new ResourceNotFoundException("Кафедра не найдена", "Department not found: id=" + id));

            mockMvc.perform(get("/api/v1/departments/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Кафедра не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/departments/{id} - Обновление кафедры")
    class UpdateDepartment {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateDepartmentSuccessfully() throws Exception {
            Short id = 1;
            DepartmentRequest request = createValidRequest();
            DepartmentResponse response = createTestResponse(id);

            when(departmentService.updateDepartmentById(eq(id), any(DepartmentRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/departments/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/departments/{id} - Удаление кафедры")
    class DeleteDepartment {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteDepartmentAndReturn204ForAdmin() throws Exception {
            Short id = 1;
            doNothing().when(departmentService).deleteDepartmentById(id);

            mockMvc.perform(delete("/api/v1/departments/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(departmentService, times(1)).deleteDepartmentById(id);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления ролями TEACHER или STUDENT")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403WhenDeletingByTeacher() throws Exception {
            Short id = 1;

            mockMvc.perform(delete("/api/v1/departments/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(departmentService, never()).deleteDepartmentById(any());
        }
    }
}