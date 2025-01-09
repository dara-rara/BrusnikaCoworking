package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.reserval.CodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, Long> {
    void deleteBySendTimeBefore(LocalDateTime sendTime);
    Optional<CodeEntity> findTopByOrderBySendTimeDesc();
}