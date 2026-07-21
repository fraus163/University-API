package com.fraus.spring.universityapi.subject.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.subject.domain.GroupSubjectService;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
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

@WebMvcTest(GroupSubjectController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование GroupSubjectController")
class GroupSubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupSubjectService groupSubjectService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private GroupSubjectRequest createValidRequest() {
        return new GroupSubjectRequest(1, 2, 5L, (short) 1, (short) 120, ControlType.EXAM);
    }

    private GroupSubjectResponse createTestResponse(Integer id) {
        GroupResponse groupResp = new GroupResponse(1, "ПО-11", (short) 1, null);
        SubjectResponse subjectResp = new SubjectResponse(2, "Высшая математика", "Описание");
        TeacherResponse teacherResp = new TeacherResponse(
                5L,
                "Иванов",
                "Иван",
                "Иванович",
                "+375291234567",
                "teacher@university.com",
                (short) 5,
                null,
                null,
                List.of()
        );
        return new GroupSubjectResponse(id, groupResp, subjectResp, teacherResp, (short) 1, (short) 120, ControlType.EXAM);
    }

    @Nested
    @DisplayName("POST /api/v1/group-subjects - Назначение дисциплины группе")
    class CreateGroupSubject {

        @Test
        @DisplayName("Должен вернуть 201 Created при вызове с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateGroupSubjectAndReturn201ForAdmin() throws Exception {
            GroupSubjectRequest request = createValidRequest();
            GroupSubjectResponse response = createTestResponse(10);

            when(groupSubjectService.createGroupSubject(any(GroupSubjectRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/group-subjects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/group-subjects/10")))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.group.id").value(1))
                    .andExpect(jsonPath("$.group.name").value("ПО-11"))
                    .andExpect(jsonPath("$.subject.id").value(2))
                    .andExpect(jsonPath("$.subject.name").value("Высшая математика"))
                    .andExpect(jsonPath("$.teacher.id").value(5))
                    .andExpect(jsonPath("$.teacher.lastName").value("Иванов"))
                    .andExpect(jsonPath("$.term").value(1))
                    .andExpect(jsonPath("$.hours").value(120))
                    .andExpect(jsonPath("$.typeOfControl").value(ControlType.EXAM.name()));

            verify(groupSubjectService, times(1)).createGroupSubject(any(GroupSubjectRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания ролями STUDENT или TEACHER")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenCreatedByStudent() throws Exception {
            GroupSubjectRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/group-subjects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(groupSubjectService, never()).createGroupSubject(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/group-subjects - Получение назначений дисциплин")
    class GetGroupSubjects {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу назначений для роли STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageOfGroupSubjectsForStudent() throws Exception {
            Integer groupId = 1;
            Integer subjectId = 2;
            Long teacherId = 5L;
            Short term = 1;

            GroupSubjectResponse response = createTestResponse(10);
            Page<GroupSubjectResponse> responsePage = new PageImpl<>(List.of(response));

            when(groupSubjectService.getGroupSubjectsByFilter(any(Pageable.class), eq(groupId), eq(subjectId), eq(teacherId), eq(term)))
                    .thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/group-subjects")
                            .param("groupId", groupId.toString())
                            .param("subjectId", subjectId.toString())
                            .param("teacherId", teacherId.toString())
                            .param("term", term.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].group.name").value("ПО-11"))
                    .andExpect(jsonPath("$.content[0].subject.name").value("Высшая математика"))
                    .andExpect(jsonPath("$.content[0].teacher.id").value(5));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и назначение по ID для роли TEACHER")
        @WithMockUser(roles = "TEACHER")
        void shouldReturnGroupSubjectByIdForTeacher() throws Exception {
            Integer id = 10;
            GroupSubjectResponse response = createTestResponse(id);

            when(groupSubjectService.getGroupSubjectById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/group-subjects/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.group.id").value(1))
                    .andExpect(jsonPath("$.subject.id").value(2))
                    .andExpect(jsonPath("$.teacher.id").value(5));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если назначение не найдено")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenGroupSubjectNotFound() throws Exception {
            Integer id = 999;
            when(groupSubjectService.getGroupSubjectById(id))
                    .thenThrow(new ResourceNotFoundException("Дисциплина группы не найдена", "Group subject not found: groupSubjectId=" + id));

            mockMvc.perform(get("/api/v1/group-subjects/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Дисциплина группы не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/group-subjects/{id} - Обновление назначения дисциплины")
    class UpdateGroupSubject {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateGroupSubjectSuccessfully() throws Exception {
            Integer id = 10;
            GroupSubjectRequest request = createValidRequest();
            GroupSubjectResponse response = createTestResponse(id);

            when(groupSubjectService.updateGroupSubjectById(eq(id), any(GroupSubjectRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/group-subjects/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.group.id").value(1))
                    .andExpect(jsonPath("$.teacher.id").value(5));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/group-subjects/{id} - Удаление назначения дисциплины")
    class DeleteGroupSubject {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteGroupSubjectAndReturn204ForAdmin() throws Exception {
            Integer id = 10;
            doNothing().when(groupSubjectService).deleteGroupSubjectById(id);

            mockMvc.perform(delete("/api/v1/group-subjects/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(groupSubjectService, times(1)).deleteGroupSubjectById(id);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления ролями TEACHER или STUDENT")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403WhenDeletingByTeacher() throws Exception {
            Integer id = 10;

            mockMvc.perform(delete("/api/v1/group-subjects/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(groupSubjectService, never()).deleteGroupSubjectById(any());
        }
    }
}