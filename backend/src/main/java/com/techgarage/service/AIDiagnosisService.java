package com.techgarage.service;
import com.techgarage.dto.ai.AIDiagnosisRequest;
import com.techgarage.dto.ai.AIDiagnosisResponse;
public interface AIDiagnosisService { AIDiagnosisResponse diagnose(AIDiagnosisRequest request); }
