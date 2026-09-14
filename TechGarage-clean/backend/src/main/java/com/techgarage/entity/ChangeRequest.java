package com.techgarage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="change_requests", indexes={@Index(name="idx_change_request_job", columnList="job_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChangeRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="job_id", nullable=false) private Job job;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="requested_by", nullable=false) private User requestedBy;
    @Column(nullable=false, length=2000) private String description;
    @Column(nullable=false) private Double additionalPrice;
    @Column(nullable=false) private Integer additionalDays;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private ChangeRequestStatus status=ChangeRequestStatus.PENDING;
    @Column(updatable=false) private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    @PrePersist protected void onCreate(){ createdAt=LocalDateTime.now(); }
}
