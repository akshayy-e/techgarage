package com.techgarage.util;

import com.techgarage.entity.*;
import com.techgarage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds demo data (1 admin, 2 clients, 5 freelancers, sample problems + proposals) so the
 * application can be demonstrated immediately after startup. Controlled by app.seed.enabled
 * (on for the "dev" profile, off for "prod"). All seed passwords are documented in README.md
 * under "Test Accounts" and are for local development only — never use them in production.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProblemRepository problemRepository;
    private final ProposalRepository proposalRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        // --- Admin ---
        User admin = userRepository.save(User.builder()
                .name("TechGarage Admin")
                .email("admin@techgarage.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .phone("+1-000-000-0000")
                .enabled(true)
                .emailVerified(true)
                .build());

        // --- Clients ---
        User client1 = userRepository.save(User.builder()
                .name("Aditi Sharma")
                .email("client1@techgarage.com")
                .password(passwordEncoder.encode("Client@123"))
                .role(Role.CLIENT)
                .phone("+91-90000-00001")
                .enabled(true)
                .emailVerified(true)
                .build());

        User client2 = userRepository.save(User.builder()
                .name("Ravi Kumar")
                .email("client2@techgarage.com")
                .password(passwordEncoder.encode("Client@123"))
                .role(Role.CLIENT)
                .phone("+91-90000-00002")
                .enabled(true)
                .emailVerified(true)
                .build());

        // --- Freelancers ---
        User f1 = userRepository.save(User.builder().name("Emma Wilson").email("react.dev@techgarage.com")
                .password(passwordEncoder.encode("Freelancer@123")).role(Role.FREELANCER).phone("+1-90000-00003").enabled(true).emailVerified(true).build());
        User f2 = userRepository.save(User.builder().name("Carlos Mendez").email("java.dev@techgarage.com")
                .password(passwordEncoder.encode("Freelancer@123")).role(Role.FREELANCER).phone("+1-90000-00004").enabled(true).emailVerified(true).build());
        User f3 = userRepository.save(User.builder().name("Priya Nair").email("python.dev@techgarage.com")
                .password(passwordEncoder.encode("Freelancer@123")).role(Role.FREELANCER).phone("+91-90000-00005").enabled(true).emailVerified(true).build());
        User f4 = userRepository.save(User.builder().name("Jason Lee").email("devops.dev@techgarage.com")
                .password(passwordEncoder.encode("Freelancer@123")).role(Role.FREELANCER).phone("+1-90000-00006").enabled(true).emailVerified(true).build());
        User f5 = userRepository.save(User.builder().name("Sofia Rossi").email("fullstack.dev@techgarage.com")
                .password(passwordEncoder.encode("Freelancer@123")).role(Role.FREELANCER).phone("+39-90000-00007").enabled(true).emailVerified(true).build());

        freelancerProfileRepository.save(FreelancerProfile.builder().user(f1)
                .bio("Frontend specialist focused on React performance and UI polish.")
                .experienceYears(5).skills("React, Redux, TypeScript, CSS, Vite").hourlyRate(45.0)
                .availability(true).verified(true).rating(4.8).totalReviews(12).totalEarnings(3200.0).build());

        freelancerProfileRepository.save(FreelancerProfile.builder().user(f2)
                .bio("Backend engineer specializing in Spring Boot microservices.")
                .experienceYears(7).skills("Java, Spring Boot, Hibernate, MySQL, Kafka").hourlyRate(55.0)
                .availability(true).verified(true).rating(4.9).totalReviews(20).totalEarnings(5400.0).build());

        freelancerProfileRepository.save(FreelancerProfile.builder().user(f3)
                .bio("Python developer for Django/Flask APIs and data pipelines.")
                .experienceYears(4).skills("Python, Django, Flask, PostgreSQL, Pandas").hourlyRate(40.0)
                .availability(true).verified(false).rating(4.5).totalReviews(6).totalEarnings(1100.0).build());

        freelancerProfileRepository.save(FreelancerProfile.builder().user(f4)
                .bio("AWS/DevOps engineer: CI/CD, containers, infra-as-code.")
                .experienceYears(6).skills("AWS, Docker, Kubernetes, Terraform, Jenkins").hourlyRate(60.0)
                .availability(true).verified(true).rating(4.7).totalReviews(15).totalEarnings(4300.0).build());

        freelancerProfileRepository.save(FreelancerProfile.builder().user(f5)
                .bio("Full-stack developer comfortable across the whole stack.")
                .experienceYears(5).skills("React, Node.js, Java, MySQL, AWS").hourlyRate(50.0)
                .availability(true).verified(true).rating(4.6).totalReviews(9).totalEarnings(2700.0).build());

        // --- Sample problems ---
        Problem p1 = problemRepository.save(Problem.builder()
                .client(client1).title("React app crashes on checkout page")
                .description("My React e-commerce app throws a blank white screen when a user clicks 'Checkout'. Console shows 'Cannot read properties of undefined'.")
                .category(ProblemCategory.FRONTEND).technology("React").priority(Priority.HIGH)
                .budget(150.0).expectedCompletionDate(LocalDateTime.now().plusDays(3))
                .status(ProblemStatus.OPEN).build());

        Problem p2 = problemRepository.save(Problem.builder()
                .client(client1).title("Spring Boot login API throws NullPointerException")
                .description("My Spring Boot application gives a NullPointerException when I call the login API. It happens right after validating the password.")
                .category(ProblemCategory.BACKEND).technology("Java/Spring Boot").priority(Priority.MEDIUM)
                .budget(120.0).expectedCompletionDate(LocalDateTime.now().plusDays(4))
                .status(ProblemStatus.OPEN).build());

        Problem p3 = problemRepository.save(Problem.builder()
                .client(client2).title("MySQL query timing out on large orders table")
                .description("A reporting query against our 2M-row orders table times out. Needs indexing/query optimization.")
                .category(ProblemCategory.DATABASE).technology("MySQL").priority(Priority.EMERGENCY)
                .budget(200.0).expectedCompletionDate(LocalDateTime.now().plusDays(2))
                .status(ProblemStatus.OPEN).build());

        Problem p4 = problemRepository.save(Problem.builder()
                .client(client2).title("AWS EC2 deployment failing on push")
                .description("Our GitHub Actions pipeline fails to deploy to EC2 after the last security group change.")
                .category(ProblemCategory.CLOUD).technology("AWS").priority(Priority.HIGH)
                .budget(180.0).expectedCompletionDate(LocalDateTime.now().plusDays(5))
                .status(ProblemStatus.OPEN).build());

        // --- Sample proposals ---
        proposalRepository.save(Proposal.builder().problem(p1).freelancer(f1)
                .price(140.0).estimatedDays(2).message("I can fix this today — likely an undefined cart item on load.").status(ProposalStatus.PENDING).build());
        proposalRepository.save(Proposal.builder().problem(p1).freelancer(f5)
                .price(160.0).estimatedDays(3).message("Happy to take this on, will add tests too.").status(ProposalStatus.PENDING).build());
        proposalRepository.save(Proposal.builder().problem(p2).freelancer(f2)
                .price(110.0).estimatedDays(1).message("Classic NPE in the auth service — I can trace and fix it fast.").status(ProposalStatus.PENDING).build());
        proposalRepository.save(Proposal.builder().problem(p3).freelancer(f4)
                .price(190.0).estimatedDays(2).message("I'll add a composite index and rewrite the query plan.").status(ProposalStatus.PENDING).build());

        p1.setStatus(ProblemStatus.PROPOSALS_RECEIVED);
        problemRepository.save(p1);
        p2.setStatus(ProblemStatus.PROPOSALS_RECEIVED);
        problemRepository.save(p2);
        p3.setStatus(ProblemStatus.PROPOSALS_RECEIVED);
        problemRepository.save(p3);
    }
}
