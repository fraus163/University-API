package com.fraus.spring.universityapi.position.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.PositionService;
import com.fraus.spring.universityapi.position.web.dto.PositionRequest;
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

@WebMvcTest(PositionController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование PositionController")
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PositionService positionService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private PositionRequest createValidRequest() {
        return new PositionRequest("Доцент");
    }

    private PositionResponse createTestResponse(Short id) {
        return new PositionResponse(id, "Доцент");
    }

    @Nested
    @DisplayName("POST /api/v1/positions - Создание должности")
    class CreatePosition {

        @Test
        @DisplayName("Должен вернуть 201 Created и заголовок Location при создании должности")
        @WithMockUser
        void shouldCreatePositionAndReturn201() throws Exception {
            PositionRequest request = createValidRequest();
            PositionResponse response = createTestResponse((short) 1);

            when(positionService.createPosition(any(PositionRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/positions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/positions/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Доцент"));

            verify(positionService, times(1)).createPosition(any(PositionRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/positions - Получение должностей")
    class GetPositions {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу всех должностей")
        @WithMockUser
        void shouldReturnPageOfPositions() throws Exception {
            PositionResponse response = createTestResponse((short) 1);
            Page<PositionResponse> responsePage = new PageImpl<>(List.of(response));

            when(positionService.getAllPositions(any(Pageable.class))).thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/positions")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Доцент"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и должность по ID")
        @WithMockUser
        void shouldReturnPositionById() throws Exception {
            Short id = 1;
            PositionResponse response = createTestResponse(id);

            when(positionService.getPositionById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/positions/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Доцент"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если должность не найдена")
        @WithMockUser
        void shouldReturn404WhenPositionNotFound() throws Exception {
            Short id = 99;
            when(positionService.getPositionById(id))
                    .thenThrow(new ResourceNotFoundException("Должность не найдена", "Position not found: id=" + id));

            mockMvc.perform(get("/api/v1/positions/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Должность не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/positions/{id} - Обновление должности")
    class UpdatePosition {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении должности")
        @WithMockUser
        void shouldUpdatePositionSuccessfully() throws Exception {
            Short id = 1;
            PositionRequest request = new PositionRequest("Профессор");
            PositionResponse response = new PositionResponse(id, "Профессор");

            when(positionService.updatePositionById(eq(id), any(PositionRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/positions/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Профессор"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/positions/{id} - Удаление должности")
    class DeletePosition {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении")
        @WithMockUser
        void shouldDeletePositionAndReturn204() throws Exception {
            Short id = 1;
            doNothing().when(positionService).deletePositionById(id);

            mockMvc.perform(delete("/api/v1/positions/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(positionService, times(1)).deletePositionById(id);
        }
    }
}