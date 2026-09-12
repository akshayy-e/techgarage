package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.message.MessageRequest;
import com.techgarage.dto.message.MessageResponse;
import com.techgarage.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> send(@PathVariable Long jobId, @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(messageService.send(jobId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getForJob(jobId)));
    }
}
