package com.techgarage.service.impl;

import com.techgarage.dto.message.MessageRequest;
import com.techgarage.dto.message.MessageResponse;
import com.techgarage.entity.Job;
import com.techgarage.entity.Message;
import com.techgarage.entity.Role;
import com.techgarage.entity.User;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.JobRepository;
import com.techgarage.repository.MessageRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.MessageService;
import com.techgarage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final JobRepository jobRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;

    @Override
    public MessageResponse send(Long jobId, MessageRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User sender = securityUtil.getCurrentUser();

        User receiver;
        if (job.getClient().getId().equals(sender.getId())) {
            receiver = job.getFreelancer();
        } else if (job.getFreelancer().getId().equals(sender.getId())) {
            receiver = job.getClient();
        } else {
            throw new ForbiddenException("Chat is only available between the client and the assigned freelancer");
        }

        Message message = Message.builder()
                .job(job)
                .sender(sender)
                .receiver(receiver)
                .message(request.getMessage())
                .build();
        message = messageRepository.save(message);

        notificationService.notify(receiver.getId(), sender.getName() + " sent you a message on Job #" + job.getId());

        return toResponse(message);
    }

    @Override
    public List<MessageResponse> getForJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User me = securityUtil.getCurrentUser();
        boolean participant = job.getClient().getId().equals(me.getId())
                || job.getFreelancer().getId().equals(me.getId())
                || me.getRole() == Role.ADMIN;
        if (!participant) {
            throw new ForbiddenException("You are not a participant in this job's chat");
        }
        return messageRepository.findByJobIdOrderByCreatedAtAsc(jobId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .jobId(m.getJob().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getName())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
