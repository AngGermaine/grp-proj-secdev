package com.secdev.project.service;

import com.secdev.project.dto.AssetRequest;
import com.secdev.project.model.Asset;
import com.secdev.project.model.User;
import com.secdev.project.repo.AssetRepository;
import com.secdev.project.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetService(AssetRepository assetRepository, UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    public List<Asset> findAllForUser(String email) {
        return assetRepository.findByOwnerEmail(email);
    }

    @Transactional
    public Asset addAsset(String userEmail, AssetRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Asset asset = new Asset();
        asset.setName(request.getName().trim());
        asset.setValue(request.getValue());
        asset.setQuantity(request.getQuantity());
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setOwner(user);

        Asset saved = assetRepository.save(asset);

        logger.info("TRANSACTION EVENT user={} action=ADD_ASSET status=SUCCESS assetId={} assetName={}",
                userEmail, saved.getId(), saved.getName());

        return saved;
    }

    @Transactional
    public Asset editOwnAsset(Long assetId, String userEmail, AssetRequest request) {
        Asset asset = assetRepository.findByIdAndOwnerEmail(assetId, userEmail)
                .orElseThrow(() -> new RuntimeException("Asset not found or not owned by user"));

        asset.setName(request.getName().trim());
        asset.setValue(request.getValue());
        asset.setQuantity(request.getQuantity());
        asset.setUpdatedAt(LocalDateTime.now());

        Asset saved = assetRepository.save(asset);

        logger.info("TRANSACTION EVENT user={} action=EDIT_ASSET status=SUCCESS assetId={} assetName={}",
                userEmail, saved.getId(), saved.getName());

        return saved;
    }

    @Transactional
    public void deleteOwnAsset(Long assetId, String userEmail) {
        Asset asset = assetRepository.findByIdAndOwnerEmail(assetId, userEmail)
                .orElseThrow(() -> new RuntimeException("Asset not found or not owned by user"));

        assetRepository.delete(asset);

        logger.info("TRANSACTION EVENT user={} action=DELETE_ASSET status=SUCCESS assetId={} assetName={}",
                userEmail, asset.getId(), asset.getName());
    }

    @Transactional
    public void adminDeleteAsset(Long assetId, String adminEmail) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        assetRepository.delete(asset);

        logger.info("ADMIN ACTION admin={} action=DELETE_ANY_ASSET targetAssetId={} targetAssetName={}",
                adminEmail, asset.getId(), asset.getName());
    }
}