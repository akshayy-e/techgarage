package com.techgarage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgarage.dto.auth.LoginRequest;
import com.techgarage.dto.auth.RegisterRequest;
import com.techgarage.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLogin_shouldSucceed() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Test Client");
        register.setEmail("test.client@example.com");
        register.setPassword("Password@123");
        register.setRole(Role.CLIENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CLIENT"));

        LoginRequest login = new LoginRequest();
        login.setEmail("test.client@example.com");
        login.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void register_withDuplicateEmail_shouldFail() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Dup User");
        register.setEmail("dup@example.com");
        register.setPassword("Password@123");
        register.setRole(Role.CLIENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_withInvalidEmail_shouldReturnValidationError() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Bad Email");
        register.setEmail("not-an-email");
        register.setPassword("Password@123");
        register.setRole(Role.CLIENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withWrongPassword_shouldReturnUnauthorized() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Wrong Pass");
        register.setEmail("wrongpass@example.com");
        register.setPassword("Correct@123");
        register.setRole(Role.CLIENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("wrongpass@example.com");
        login.setPassword("WrongOne@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
