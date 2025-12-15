package com.company.orchestrator.api.controller;

import com.company.orchestrator.api.dto.TransferRequestDto;
import com.company.orchestrator.domain.model.TransferState;
import com.company.orchestrator.infrastructure.persistence.repository.TransferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransferRepository repository;

    @BeforeEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void fullTransferLifecycle() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto(
                "consumer1",
                "provider1",
                "DATA_TYPE"
        );

        String createJson = objectMapper.writeValueAsString(requestDto);

        String responseBody = mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = objectMapper.readTree(responseBody)
                .get("transferId").traverse(objectMapper).readValueAs(UUID.class);

        assertThat(transferId).isNotNull();

        String statusJson = mockMvc.perform(get("/api/v1/transfers/{id}", transferId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String state = objectMapper.readTree(statusJson)
                .get("state").asText();

        assertThat(state).isNotNull();

        String auditJson = mockMvc.perform(get("/api/v1/transfers/{id}/audit", transferId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<?> auditEvents = objectMapper.readValue(auditJson, List.class);
        assertThat(auditEvents).isNotNull();

        String listJson = mockMvc.perform(get("/api/v1/transfers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("consumer1", "provider1");

        String analyticsJson = mockMvc.perform(get("/api/v1/transfers/analytics"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(analyticsJson).contains("totalTransfers");

        mockMvc.perform(delete("/api/v1/transfers/{id}", transferId))
                .andExpect(status().isNoContent());

        var updatedEntity = repository.findById(transferId).orElseThrow();
        assertThat(updatedEntity.getState()).isEqualTo(TransferState.CANCELLED);
    }
}
