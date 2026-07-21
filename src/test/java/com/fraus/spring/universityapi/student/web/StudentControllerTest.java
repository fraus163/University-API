package com.fraus.spring.universityapi.student.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.student.domain.StudentService;
import com.fraus.spring.universityapi.student.web.dto.StudentCreateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.student.web.dto.StudentUpdateRequest;
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

@WebMvcTest(StudentController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование StudentController")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private StudentResponse createTestResponse(Long id) {
        GroupResponse groupResponse = new GroupResponse(1, "ПО-11", (short) 1, null);
        return new StudentResponse(
                id,
                "Иванов",
                "Иван",
                "Иванович",
                "+79291234567",
                "student@university.com",
                groupResponse
        );
    }

    @Nested
    @DisplayName("POST /api/v1/students - Создание студента")
    class CreateStudent {

        @Test
        @DisplayName("Должен вернуть 201 Created при успешном создании админом")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateStudentForAdmin() throws Exception {
            StudentCreateRequest request = new StudentCreateRequest(10L, 1);
            StudentResponse response = createTestResponse(100L);

            when(studentService.createStudent(any(StudentCreateRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/students/100")))
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.lastName").value("Иванов"))
                    .andExpect(jsonPath("$.group.id").value(1));

            verify(studentService, times(1)).createStudent(any(StudentCreateRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания преподавателем")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403ForTeacherOnCreate() throws Exception {
            StudentCreateRequest request = new StudentCreateRequest(10L, 1);

            mockMvc.perform(post("/api/v1/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(studentService, never()).createStudent(any());
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request при некорректных данных (например, userId = null)")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400OnInvalidRequestBody() throws Exception {
            StudentCreateRequest request = new StudentCreateRequest(null, 1);

            mockMvc.perform(post("/api/v1/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(studentService, never()).createStudent(any());
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request, если пользователь уже является студентом")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenUserAlreadyStudent() throws Exception {
            StudentCreateRequest request = new StudentCreateRequest(10L, 1);

            when(studentService.createStudent(any(StudentCreateRequest.class)))
                    .thenThrow(new InvalidValueException("Пользователь уже является студентом", "User with id=10 is already a student"));

            mockMvc.perform(post("/api/v1/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Пользователь уже является студентом"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/students - Получение студентов")
    class GetStudents {

        @Test
        @DisplayName("Должен вернуть 200 OK и данные студента по ID")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnStudentById() throws Exception {
            Long id = 100L;
            StudentResponse response = createTestResponse(id);

            when(studentService.getStudentById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/students/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.lastName").value("Иванов"))
                    .andExpect(jsonPath("$.email").value("student@university.com"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если студент не найден")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn404WhenNotFound() throws Exception {
            Long id = 999L;
            when(studentService.getStudentById(id))
                    .thenThrow(new ResourceNotFoundException("Студент не найден", "Student not found: id=" + id));

            mockMvc.perform(get("/api/v1/students/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Студент не найден"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу студентов по фильтру")
        @WithMockUser(roles = "TEACHER")
        void shouldReturnPageByFilter() throws Exception {
            StudentResponse response = createTestResponse(100L);
            Page<StudentResponse> page = new PageImpl<>(List.of(response));

            when(studentService.getStudentsByFilter(any(Pageable.class), eq("Иванов"), eq(null), eq(null), eq(1)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/students")
                            .param("lastName", "Иванов")
                            .param("groupId", "1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(100))
                    .andExpect(jsonPath("$.content[0].lastName").value("Иванов"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/students/{id} - Обновление данных студента")
    class UpdateStudent {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном переводе студента админом")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateStudentForAdmin() throws Exception {
            Long id = 100L;
            StudentUpdateRequest request = new StudentUpdateRequest(2); // Перевод в группу #2
            StudentResponse response = createTestResponse(id);

            when(studentService.updateStudentById(eq(id), any(StudentUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/students/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке обновления студентом")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403ForStudentOnUpdate() throws Exception {
            StudentUpdateRequest request = new StudentUpdateRequest(2);

            mockMvc.perform(put("/api/v1/students/{id}", 100L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(studentService, never()).updateStudentById(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/students/{id} - Удаление студента")
    class DeleteStudent {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении админом")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteStudentForAdmin() throws Exception {
            Long id = 100L;
            doNothing().when(studentService).deleteStudentById(id);

            mockMvc.perform(delete("/api/v1/students/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(studentService, times(1)).deleteStudentById(id);
        }
    }
}