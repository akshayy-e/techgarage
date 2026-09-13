package com.techgarage.service.impl;

import com.techgarage.dto.profile.FreelancerDirectoryResponse;
import com.techgarage.entity.FreelancerProfile;
import com.techgarage.entity.Role;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.service.FreelancerDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreelancerDirectoryServiceImpl implements FreelancerDirectoryService {
    private final FreelancerProfileRepository repository;

    @Override
    public List<FreelancerDirectoryResponse> search(String query, Boolean verified, Boolean available, Double minRating, String technology) {
        String q = query == null || query.isBlank() ? null : query.trim();
        String tech = technology == null || technology.isBlank() ? null : technology.trim();

        return repository.searchDirectory(q, verified, available, minRating, tech).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private int profileCompletion(FreelancerProfile p) {
        int complete = 0;
        if (p.getUser() != null && p.getUser().getName() != null && !p.getUser().getName().isBlank()) complete++;
        if (p.getBio() != null && !p.getBio().isBlank()) complete++;
        if (p.getSkills() != null && !p.getSkills().isBlank()) complete++;
        if (p.getExperienceYears() != null) complete++;
        if (p.getHourlyRate() != null && p.getHourlyRate() > 0) complete++;
        if (p.getPortfolio() != null && !p.getPortfolio().isBlank()) complete++;
        if (Boolean.TRUE.equals(p.getUser().isEmailVerified())) complete++;
        return Math.round((complete / 7f) * 100);
    }

    private FreelancerDirectoryResponse toResponse(FreelancerProfile p) {
        return FreelancerDirectoryResponse.builder()
                .userId(p.getUser().getId()).name(p.getUser().getName()).bio(p.getBio())
                .experienceYears(p.getExperienceYears()).skills(p.getSkills()).portfolio(p.getPortfolio())
                .hourlyRate(p.getHourlyRate()).availability(p.getAvailability()).verified(p.getVerified())
                .emailVerified(p.getUser().isEmailVerified())
                .profileCompletion(profileCompletion(p))
                .rating(p.getRating()).totalReviews(p.getTotalReviews()).build();
    }
}
