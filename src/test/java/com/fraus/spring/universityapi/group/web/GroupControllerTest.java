package com.fraus.spring.universityapi.group.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupService;
import com.fraus.spring.universityapi.group.web.dto.GroupRequest;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
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

@WebMvcTest(GroupController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование GroupController")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private SpecialtyResponse createTestSpecialtyResponse(Short id) {
        return new SpecialtyResponse(id, "ПОИТ", DegreeType.BACHELOR, null);
    }

    private GroupRequest createValidRequest() {
        return new GroupRequest("ПО-11", (short) 1, (short) 10);
    }

    private GroupResponse createTestResponse(Integer id) {
        return new GroupResponse(id, "ПО-11", (short) 1, createTestSpecialtyResponse((short) 10));
    }

    @Nested
    @DisplayName("POST /api/v1/groups - Создание группы")
    class CreateGroup {

        @Test
        @DisplayName("Должен вернуть 201 Created при вызове с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateGroupAndReturn201ForAdmin() throws Exception {
            GroupRequest request = createValidRequest();
            GroupResponse response = createTestResponse(1);

            when(groupService.createGroup(any(GroupRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/groups")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/groups/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ПО-11"))
                    .andExpect(jsonPath("$.course").value(1))
                    .andExpect(jsonPath("$.specialty.id").value(10))
                    .andExpect(jsonPath("$.specialty.name").value("ПОИТ"));

            verify(groupService, times(1)).createGroup(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания ролями STUDENT или TEACHER")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenCreatedByStudent() throws Exception {
            GroupRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/groups")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(groupService, never()).createGroup(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/groups - Получение групп")
    class GetGroups {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу групп для роли STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageOfGroupsForStudent() throws Exception {
            Short specialtyId = 10;
            Short facultyId = 5;
            Short course = 1;

            GroupResponse response = createTestResponse(1);
            Page<GroupResponse> responsePage = new PageImpl<>(List.of(response));

            when(groupService.getGroupsByFilter(any(Pageable.class), eq(specialtyId), eq(facultyId), eq(course)))
                    .thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/groups")
                            .param("specialtyId", specialtyId.toString())
                            .param("facultyId", facultyId.toString())
                            .param("course", course.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("ПО-11"))
                    .andExpect(jsonPath("$.content[0].specialty.id").value(10));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и группу по ID для роли TEACHER")
        @WithMockUser(roles = "TEACHER")
        void shouldReturnGroupByIdForTeacher() throws Exception {
            Integer id = 1;
            GroupResponse response = createTestResponse(id);

            when(groupService.getGroupById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/groups/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ПО-11"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если группа не найдена")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenGroupNotFound() throws Exception {
            Integer id = 999;
            when(groupService.getGroupById(id))
                    .thenThrow(new ResourceNotFoundException("Группа не найдена", "Group not found: id=" + id));

            mockMvc.perform(get("/api/v1/groups/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Группа не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/groups/{id} - Обновление группы")
    class UpdateGroup {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateGroupSuccessfully() throws Exception {
            Integer id = 1;
            GroupRequest request = createValidRequest();
            GroupResponse response = createTestResponse(id);

            when(groupService.updateGroupById(eq(id), any(GroupRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/groups/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ПО-11"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/groups/{id} - Удаление группы")
    class DeleteGroup {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteGroupAndReturn204ForAdmin() throws Exception {
            Integer id = 1;
            doNothing().when(groupService).deleteGroupById(id);

            mockMvc.perform(delete("/api/v1/groups/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(groupService, times(1)).deleteGroupById(id);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления ролями TEACHER или STUDENT")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403WhenDeletingByTeacher() throws Exception {
            Integer id = 1;

            mockMvc.perform(delete("/api/v1/groups/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(groupService, never()).deleteGroupById(any());
        }
    }
}