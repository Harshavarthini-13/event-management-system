package com.eventmgmt.repository;

import com.eventmgmt.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Certificate entity.
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // Find certificate by registration ID
    Optional<Certificate> findByRegistrationId(Long registrationId);

    // Check if certificate already issued for a registration
    boolean existsByRegistrationId(Long registrationId);

    // Find by certificate number (for verification)
    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    // Get all certificates for a student
    @Query("SELECT c FROM Certificate c " +
           "JOIN FETCH c.registration r " +
           "JOIN FETCH r.event " +
           "WHERE r.user.id = :userId " +
           "ORDER BY c.issuedAt DESC")
    List<Certificate> findByUserId(@Param("userId") Long userId);

    // Get all certificates for an event
    @Query("SELECT c FROM Certificate c " +
           "JOIN FETCH c.registration r " +
           "JOIN FETCH r.user " +
           "WHERE r.event.id = :eventId " +
           "ORDER BY c.issuedAt DESC")
    List<Certificate> findByEventId(@Param("eventId") Long eventId);

    // Count certificates issued for an event
    @Query("SELECT COUNT(c) FROM Certificate c " +
           "JOIN c.registration r " +
           "WHERE r.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    // Get the last issued certificate number for sequence generation
    @Query("SELECT c.certificateNumber FROM Certificate c " +
           "ORDER BY c.id DESC LIMIT 1")
    Optional<String> findLastCertificateNumber();
}