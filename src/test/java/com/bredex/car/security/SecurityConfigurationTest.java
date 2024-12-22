package com.bredex.car.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/auth/signup"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/ad/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/ad"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testProtectedEndpointsReturnUnauthorizedWhenNoAuth() throws Exception {
        mockMvc.perform(delete("/ad/1"))
                .andExpect(status().isUnauthorized());
    }
}
