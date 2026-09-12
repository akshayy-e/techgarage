package com.techgarage.service;

import com.techgarage.dto.message.MessageRequest;
import com.techgarage.dto.message.MessageResponse;
import java.util.List;

public interface MessageService {
    MessageResponse send(Long jobId, MessageRequest request);
    List<MessageResponse> getForJob(Long jobId);
}
