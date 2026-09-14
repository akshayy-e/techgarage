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
        return toResponse(freelancerProfileRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found")), false);
    }

    @Override
    public FreelancerProfileResponse getByUserId(Long userId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
        return toResponse(profile, true);
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
        return toResponse(profile, false);
    }

    @Override
    public List<FreelancerProfileResponse> getAll() {
        return freelancerProfileRepository.findAll().stream()
                .map(p -> toResponse(p, false))
                .collect(Collectors.toList());
    }

    private int profileCompletion(FreelancerProfile p) {
        int complete = 0;
        if (p.getUser() != null && p.getUser().getName() != null && !p.getUser().getName().isBlank()) complete++;
        if (p.getBio() != null && !p.getBio().isBlank()) complete++;
        if (p.getSkills() != null && !p.getSkills().isBlank()) complete++;
        if (p.getExperienceYears() != null) complete++;
        if (p.getHourlyRate() != null && p.getHourlyRate() > 0) complete++;
        if (p.getPortfolio() != null && !p.getPortfolio().isBlank()) complete++;
        if (p.getUser().isEmailVerified()) complete++;
        return Math.round((complete / 7f) * 100);
    }

    private FreelancerProfileResponse toResponse(FreelancerProfile p, boolean publicView) {
        return FreelancerProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .name(p.getUser().getName())
                .email(publicView ? null : p.getUser().getEmail())
                .bio(p.getBio())
                .experienceYears(p.getExperienceYears())
                .skills(p.getSkills())
                .portfolio(p.getPortfolio())
                .hourlyRate(p.getHourlyRate())
                .availability(p.getAvailability())
                .verified(p.getVerified())
                .emailVerified(publicView ? null : p.getUser().isEmailVerified())
                .profileCompletion(profileCompletion(p))
                .rating(p.getRating())
                .totalReviews(p.getTotalReviews())
                .totalEarnings(publicView ? null : p.getTotalEarnings())
                .build();
    }
}
