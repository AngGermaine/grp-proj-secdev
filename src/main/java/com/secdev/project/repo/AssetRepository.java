package com.secdev.project.repo;

import com.secdev.project.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByOwnerEmail(String email);
    Optional<Asset> findByIdAndOwnerEmail(Long id, String email);
}