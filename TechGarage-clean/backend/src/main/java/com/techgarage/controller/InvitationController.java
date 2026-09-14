package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.invitation.InvitationRequest;
import com.techgarage.dto.invitation.InvitationResponse;
import com.techgarage.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping("/freelancers/{freelancerId}")
    public ResponseEntity<ApiResponse<InvitationResponse>> invite(@PathVariable Long freelancerId, @Valid @RequestBody InvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation sent", invitationService.invite(freelancerId, request)));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> mine() {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.getMyInvitations()));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> sent() {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.getSentInvitations()));
    }

    @PutMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<InvitationResponse>> respond(@PathVariable Long id, @RequestParam boolean accept) {
        return ResponseEntity.ok(ApiResponse.ok(accept ? "Invitation accepted" : "Invitation declined", invitationService.respond(id, accept)));
    }
}
