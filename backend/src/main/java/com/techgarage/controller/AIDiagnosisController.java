package com.techgarage.controller;
import com.techgarage.dto.ai.AIDiagnosisRequest;
import com.techgarage.dto.ai.AIDiagnosisResponse;
import com.techgarage.dto.common.ApiResponse;
import com.techgarage.service.AIDiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/ai") @RequiredArgsConstructor
public class AIDiagnosisController {
    private final AIDiagnosisService service;
    @PostMapping("/diagnose")
    public ResponseEntity<ApiResponse<AIDiagnosisResponse>> diagnose(@Valid @RequestBody AIDiagnosisRequest request){
        return ResponseEntity.ok(ApiResponse.ok(service.diagnose(request)));
    }
}
