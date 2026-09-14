package com.techgarage.service;

import com.techgarage.dto.proposal.ProposalRequest;
import com.techgarage.dto.proposal.ProposalResponse;
import java.util.List;

public interface ProposalService {
    ProposalResponse submit(Long problemId, ProposalRequest request);
    List<ProposalResponse> getForProblem(Long problemId);
    List<ProposalResponse> getMyProposals();
    ProposalResponse accept(Long proposalId);
    ProposalResponse reject(Long proposalId);
}
