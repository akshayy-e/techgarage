package com.techgarage.repository;

import com.techgarage.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {
    Optional<FreelancerProfile> findByUserId(Long userId);

    @Query("""
        select p from FreelancerProfile p join p.user u
        where u.role = com.techgarage.entity.Role.FREELANCER
          and u.enabled = true
          and (:verified is null or p.verified = :verified)
          and (:available is null or p.availability = :available)
          and (:minRating is null or coalesce(p.rating, 0) >= :minRating)
          and (:query is null or lower(coalesce(u.name, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(p.bio, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(p.skills, '')) like lower(concat('%', :query, '%')))
          and (:technology is null or lower(coalesce(p.skills, '')) like lower(concat('%', :technology, '%'))
               or lower(coalesce(p.bio, '')) like lower(concat('%', :technology, '%')))
        order by coalesce(p.rating, 0) desc, coalesce(p.totalReviews, 0) desc, u.name asc
        """)
    List<FreelancerProfile> searchDirectory(
            @Param("query") String query,
            @Param("verified") Boolean verified,
            @Param("available") Boolean available,
            @Param("minRating") Double minRating,
            @Param("technology") String technology);

}
