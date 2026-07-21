package com.fraus.spring.universityapi.faculty.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.faculty.domain.FacultyService;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyRequest;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

@WebMvcTest(FacultyController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("WebMvc-тестирование FacultyController")
@WithMockUser(roles = {"ADMIN"})
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FacultyService facultyService;

    private FacultyRequest createValidRequest() {
        return new FacultyRequest((short) 101, "Информационные технологии");
    }

    private FacultyResponse createTestResponse(Short id) {
        return new FacultyResponse(id, (short) 101, "Информационные технологии");
    }

    @Nested
    @DisplayName("POST /api/v1/faculties - Создание факультета")
    class CreateFaculty {

        @Test
        @DisplayName("Должен вернуть 201 Created и заголовок Location при валидных данных")
        void shouldCreateFacultyAndReturn201() throws Exception {
            FacultyRequest request = createValidRequest();
            FacultyResponse response = createTestResponse((short) 1);

            when(facultyService.createFaculty(any(FacultyRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/faculties")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/faculties/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.number").value(101))
                    .andExpect(jsonPath("$.name").value("Информационные технологии"));

            verify(facultyService, times(1)).createFaculty(any(FacultyRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request при заполнении запроса с невалидными данными")
        void shouldReturn400WhenRequestIsInvalid() throws Exception {
            FacultyRequest invalidRequest = new FacultyRequest(null, "");

            mockMvc.perform(post("/api/v1/faculties")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(facultyService, never()).createFaculty(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/faculties - Получение факультетов")
    class GetFaculties {

        @Test
        @DisplayName("Должен вернуть 200 OK и список факультетов")
        void shouldReturnListOfFaculties() throws Exception {
            List<FacultyResponse> responses = List.of(
                    createTestResponse((short) 1),
                    new FacultyResponse((short) 2, (short) 102, "Экономический")
            );

            when(facultyService.getAllFaculties()).thenReturn(responses);

            mockMvc.perform(get("/api/v1/faculties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].name").value("Экономический"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и объект факультета по существующему ID")
        void shouldReturnFacultyById() throws Exception {
            Short id = 1;
            FacultyResponse response = createTestResponse(id);

            when(facultyService.getFacultyById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/faculties/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Информационные технологии"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если факультет не существует")
        void shouldReturn404WhenFacultyNotFound() throws Exception {
            Short id = 99;
            when(facultyService.getFacultyById(id))
                    .thenThrow(new ResourceNotFoundException("Факультет не найден", "Faculty not found: id=" + id));

            mockMvc.perform(get("/api/v1/faculties/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Факультет не найден"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/faculties/{id} - Обновление факультета")
    class UpdateFaculty {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении")
        void shouldUpdateFacultyAndReturn200() throws Exception {
            Short id = 1;
            FacultyRequest request = createValidRequest();
            FacultyResponse response = createTestResponse(id);

            when(facultyService.updateFacultyById(eq(id), any(FacultyRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/faculties/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/faculties/{id} - Удаление факультета")
    class DeleteFaculty {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении")
        void shouldDeleteFacultyAndReturn204() throws Exception {
            Short id = 1;
            doNothing().when(facultyService).deleteFacultyById(id);

            mockMvc.perform(delete("/api/v1/faculties/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(facultyService, times(1)).deleteFacultyById(id);
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found при попытке удалить несуществующий факультет")
        void shouldReturn404WhenDeletingNonExistentFaculty() throws Exception {
            Short id = 99;
            doThrow(new ResourceNotFoundException("Факультет не найден", "Faculty not found: id=" + id))
                    .when(facultyService).deleteFacultyById(id);

            mockMvc.perform(delete("/api/v1/faculties/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Факультет не найден"));
        }
    }
}
