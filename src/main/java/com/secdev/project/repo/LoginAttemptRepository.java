package com.secdev.project.repo;

import com.secdev.project.model.LoginAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    long countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(String email, LocalDateTime after);
    long countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(String ipAddress, LocalDateTime after);
    long countByEmailAndIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(
        String email, String ipAddress, LocalDateTime after);

    @Modifying
    @Transactional
    @Query("delete from LoginAttempt la where la.attemptTime < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
