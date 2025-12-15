package com.company.orchestrator.api.controller;

import com.company.orchestrator.api.dto.*;
import com.company.orchestrator.domain.model.*;
import com.company.orchestrator.domain.service.TransferOrchestrator;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.policy.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferOrchestrator orchestrator;

    public TransferController(TransferOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Operation(summary = "Initiate a new data transfer")
    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(
            @Valid @RequestBody TransferRequestDto dto
    ) {

        UUID id = orchestrator.initiateTransfer(dto);
        log.info("got response: {}", id);

        return ResponseEntity.ok(new TransferResponseDto(id));
    }

    @Operation(summary = "Get transfer status")
    @GetMapping("/{id}")
    public ResponseEntity<TransferStatusDto> getStatus(
            @PathVariable(name = "id") UUID id
    ) {
        var status = orchestrator.getTransferStatus(id);

        return ResponseEntity.ok(
                new TransferStatusDto(
                        status.transferId(),
                        status.state(),
                        status.lastUpdated()
                )
        );
    }

    @Operation(summary = "Cancel transfer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable(name = "id") UUID id) {
        orchestrator.cancelTransfer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get audit trail for a transfer")
    @GetMapping("/{id}/audit")
    public List<AuditEventEntity> getAuditLog(
            @PathVariable(name = "id") UUID transferId
    ) {
        return orchestrator.getTransferAuditLog(transferId);
    }

    @GetMapping
    @Operation(summary = "List transfers (paginated)")
    public Page<TransferSummaryResponse> listTransfers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction =
                sortParams.length > 1
                        ? Sort.Direction.fromString(sortParams[1])
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direction, sortParams[0])
                );

        return orchestrator.listTransfers(pageable);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Transfer analytics")
    public TransferAnalyticsResponse getTransferAnalytics() {
        return orchestrator.getAnalytics();
    }
}
