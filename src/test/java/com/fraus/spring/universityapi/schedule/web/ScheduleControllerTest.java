package com.fraus.spring.universityapi.schedule.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.schedule.domain.ScheduleService;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleRequest;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleResponse;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование ScheduleController")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private ScheduleRequest createValidRequest() {
        return new ScheduleRequest(
                5L,
                2,
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                LocalDate.now().plusDays(1),
                "301-1",
                List.of(1)
        );
    }

    private ScheduleResponse createTestResponse(Long id) {
        return new ScheduleResponse(
                id,
                null,
                null,
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                LocalDate.now().plusDays(1),
                "301-1",
                List.of()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/schedule - Создание расписания")
    class CreateSchedule {

        @Test
        @DisplayName("Должен вернуть 201 Created при успешном создании админом")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateScheduleForAdmin() throws Exception {
            ScheduleRequest request = createValidRequest();
            ScheduleResponse response = createTestResponse(100L);

            when(scheduleService.createSchedule(any(ScheduleRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/schedule")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/schedule/100")))
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.audience").value("301-1"));

            verify(scheduleService, times(1)).createSchedule(any(ScheduleRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания преподавателем")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403ForTeacherOnCreate() throws Exception {
            ScheduleRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/schedule")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(scheduleService, never()).createSchedule(any());
        }

        @Test
        @DisplayName("Должен вернуть 409 Conflict при коллизии аудитории")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409OnAudienceCollision() throws Exception {
            ScheduleRequest request = createValidRequest();
            when(scheduleService.createSchedule(any(ScheduleRequest.class)))
                    .thenThrow(new AlreadyExistsException("Аудитория занята на это время", "Collision info"));

            mockMvc.perform(post("/api/v1/schedule")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Аудитория занята на это время"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/schedule - Получение расписания")
    class GetSchedule {

        @Test
        @DisplayName("Должен вернуть 200 OK и объект расписания по ID")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnScheduleById() throws Exception {
            Long id = 100L;
            ScheduleResponse response = createTestResponse(id);

            when(scheduleService.getScheduleById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/schedule/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.audience").value("301-1"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если расписание не найдено")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn404WhenNotFound() throws Exception {
            Long id = 999L;
            when(scheduleService.getScheduleById(id))
                    .thenThrow(new ResourceNotFoundException("Расписание не найдено", "Not found id=" + id));

            mockMvc.perform(get("/api/v1/schedule/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Расписание не найдено"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу расписания по фильтру")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageByFilter() throws Exception {
            ScheduleResponse response = createTestResponse(100L);
            Page<ScheduleResponse> page = new PageImpl<>(List.of(response));

            when(scheduleService.getSchedulesByFilter(any(Pageable.class), eq(5L), eq(null), eq(null), eq(null), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/schedule")
                            .param("teacherId", "5")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(100));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/schedule/{id} - Обновление расписания")
    class UpdateSchedule {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении админом")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateScheduleForAdmin() throws Exception {
            Long id = 100L;
            ScheduleRequest request = createValidRequest();
            ScheduleResponse response = createTestResponse(id);

            when(scheduleService.updateScheduleById(eq(id), any(ScheduleRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/schedule/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/schedule/{id} - Удаление расписания")
    class DeleteSchedule {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении админом")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteScheduleForAdmin() throws Exception {
            Long id = 100L;
            doNothing().when(scheduleService).deleteScheduleById(id);

            mockMvc.perform(delete("/api/v1/schedule/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(scheduleService, times(1)).deleteScheduleById(id);
        }
    }
}