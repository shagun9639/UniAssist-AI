package com.uniassist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniassist.dto.AuthDtos.LoginRequest;
import com.uniassist.dto.AuthDtos.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStudentRegistrationAndLogin() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setFullName("Priya Patel");
        registerReq.setStudentId("STU-2024-CSE999");
        registerReq.setEmail("priya.patel@uniassist.edu");
        registerReq.setMobileNumber("+91 9988776655");
        registerReq.setDepartment("Computer Science & Engineering");
        registerReq.setCourse("B.Tech");
        registerReq.setSemester(2);
        registerReq.setPassword("Password@123");

        // Test Registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("priya.patel@uniassist.edu"))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"));

        // Test Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("priya.patel@uniassist.edu");
        loginReq.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Priya Patel"));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("nonexistent@uniassist.edu");
        loginReq.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest());
    }
}
