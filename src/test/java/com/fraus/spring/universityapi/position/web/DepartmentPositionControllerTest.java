package com.fraus.spring.universityapi.position.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.DepartmentPositionService;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionRequest;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
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

@WebMvcTest(DepartmentPositionController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование DepartmentPositionController")
class DepartmentPositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentPositionService departmentPositionService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private DepartmentPositionRequest createValidRequest() {
        return new DepartmentPositionRequest((short) 1, (short) 2);
    }

    private DepartmentPositionResponse createTestResponse(Integer id) {
        PositionResponse positionResp = new PositionResponse((short) 1, "Доцент");
        DepartmentResponse departmentResp = new DepartmentResponse((short) 2, "Кафедра ПОИТ", null);
        return new DepartmentPositionResponse(id, positionResp, departmentResp);
    }

    @Nested
    @DisplayName("POST /api/v1/department_positions - Назначение должности кафедре")
    class CreateDepartmentPosition {

        @Test
        @DisplayName("Должен вернуть 201 Created и заголовок Location")
        @WithMockUser
        void shouldCreateDepartmentPositionAndReturn201() throws Exception {
            DepartmentPositionRequest request = createValidRequest();
            DepartmentPositionResponse response = createTestResponse(10);

            when(departmentPositionService.createDepartmentPositions(any(DepartmentPositionRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/department_positions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/department_positions/10")))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.position.id").value(1))
                    .andExpect(jsonPath("$.position.name").value("Доцент"))
                    .andExpect(jsonPath("$.department.id").value(2))
                    .andExpect(jsonPath("$.department.name").value("Кафедра ПОИТ"));

            verify(departmentPositionService, times(1)).createDepartmentPositions(any(DepartmentPositionRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/department_positions - Получение должностей кафедры")
    class GetDepartmentPositions {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу с результатом фильтрации")
        @WithMockUser
        void shouldReturnPageOfDepartmentPositions() throws Exception {
            Short positionId = 1;
            Short departmentId = 2;
            DepartmentPositionResponse response = createTestResponse(10);
            Page<DepartmentPositionResponse> responsePage = new PageImpl<>(List.of(response));

            when(departmentPositionService.getDepartmentPositionByFilter(any(Pageable.class), eq(positionId), eq(departmentId)))
                    .thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/department_positions")
                            .param("positionId", positionId.toString())
                            .param("departmentId", departmentId.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].position.id").value(1))
                    .andExpect(jsonPath("$.content[0].department.id").value(2));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и запись по ID")
        @WithMockUser
        void shouldReturnDepartmentPositionById() throws Exception {
            Integer id = 10;
            DepartmentPositionResponse response = createTestResponse(id);

            when(departmentPositionService.getDepartmentPositionById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/department_positions/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.position.name").value("Доцент"))
                    .andExpect(jsonPath("$.department.name").value("Кафедра ПОИТ"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если запись не найдена")
        @WithMockUser
        void shouldReturn404WhenNotFound() throws Exception {
            Integer id = 999;
            when(departmentPositionService.getDepartmentPositionById(id))
                    .thenThrow(new ResourceNotFoundException("Должность кафедры не найдена", "Department position not found: id=" + id));

            mockMvc.perform(get("/api/v1/department_positions/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Должность кафедры не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/department_positions/{id} - Обновление должности кафедры")
    class UpdateDepartmentPosition {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении")
        @WithMockUser
        void shouldUpdateDepartmentPositionSuccessfully() throws Exception {
            Integer id = 10;
            DepartmentPositionRequest request = createValidRequest();
            DepartmentPositionResponse response = createTestResponse(id);

            when(departmentPositionService.updateDepartmentPositionById(eq(id), any(DepartmentPositionRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/department_positions/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/department_positions/{id} - Удаление должности кафедры")
    class DeleteDepartmentPosition {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении")
        @WithMockUser
        void shouldDeleteDepartmentPositionAndReturn204() throws Exception {
            Integer id = 10;
            doNothing().when(departmentPositionService).deleteDepartmentPositionById(id);

            mockMvc.perform(delete("/api/v1/department_positions/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(departmentPositionService, times(1)).deleteDepartmentPositionById(id);
        }
    }
}