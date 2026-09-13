package com.techgarage.service;

import com.techgarage.dto.invitation.InvitationRequest;
import com.techgarage.dto.invitation.InvitationResponse;
import java.util.List;

public interface InvitationService {
    InvitationResponse invite(Long freelancerId, InvitationRequest request);
    List<InvitationResponse> getMyInvitations();
    List<InvitationResponse> getSentInvitations();
    InvitationResponse respond(Long id, boolean accept);
}
