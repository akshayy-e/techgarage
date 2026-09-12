package com.techgarage.service.impl;

import com.techgarage.dto.profile.FreelancerProfileRequest;
import com.techgarage.dto.profile.FreelancerProfileResponse;
import com.techgarage.entity.FreelancerProfile;
import com.techgarage.entity.User;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.FreelancerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreelancerProfileServiceImpl implements FreelancerProfileService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final SecurityUtil securityUtil;

    @Override
    public FreelancerProfileResponse getMyProfile() {
        User me = securityUtil.getCurrentUser();
        return getByUserId(me.getId());
    }

    @Override
    public FreelancerProfileResponse getByUserId(Long userId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
        return toResponse(profile);
    }

    @Override
    public FreelancerProfileResponse update(FreelancerProfileRequest request) {
        User me = securityUtil.getCurrentUser();
        if (me.getRole() != com.techgarage.entity.Role.FREELANCER) {
            throw new ForbiddenException("Only freelancers have a freelancer profile");
        }
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));

        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getExperienceYears() != null) profile.setExperienceYears(request.getExperienceYears());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getPortfolio() != null) profile.setPortfolio(request.getPortfolio());
        if (request.getHourlyRate() != null) profile.setHourlyRate(request.getHourlyRate());
        if (request.getAvailability() != null) profile.setAvailability(request.getAvailability());

        profile = freelancerProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Override
    public List<FreelancerProfileResponse> getAll() {
        return freelancerProfileRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FreelancerProfileResponse toResponse(FreelancerProfile p) {
        return FreelancerProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .name(p.getUser().getName())
                .email(p.getUser().getEmail())
                .bio(p.getBio())
                .experienceYears(p.getExperienceYears())
                .skills(p.getSkills())
                .portfolio(p.getPortfolio())
                .hourlyRate(p.getHourlyRate())
                .availability(p.getAvailability())
                .verified(p.getVerified())
                .rating(p.getRating())
                .totalReviews(p.getTotalReviews())
                .totalEarnings(p.getTotalEarnings())
                .build();
    }
}
