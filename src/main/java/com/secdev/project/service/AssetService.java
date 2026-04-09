package com.secdev.project.service;

import com.secdev.project.dto.AssetRequest;
import com.secdev.project.model.Asset;
import com.secdev.project.model.User;
import com.secdev.project.repo.AssetRepository;
import com.secdev.project.repo.UserRepository;
import com.secdev.project.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetService(AssetRepository assetRepository, UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    public List<Asset> findAllForUser(String email) {
        return assetRepository.findByOwnerEmail(email);
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
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

        try {
            Asset saved = assetRepository.save(asset);

            auditLogger.info("TRANSACTION EVENT user={} action=ADD_ASSET status=SUCCESS assetId={} assetName={} value={} quantity={}",
                    LoggingUtil.sanitizeForLog(userEmail), saved.getId(), LoggingUtil.sanitizeForLog(saved.getName()), saved.getValue(), saved.getQuantity());

            return saved;
        } catch (Exception e) {
            auditLogger.error("TRANSACTION EVENT user={} action=ADD_ASSET status=FAILED assetName={} value={} quantity={} error={}",
                    LoggingUtil.sanitizeForLog(userEmail), LoggingUtil.sanitizeForLog(request.getName()), request.getValue(), request.getQuantity(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Asset editOwnAsset(Long assetId, String userEmail, AssetRequest request) {
        Asset asset = assetRepository.findByIdAndOwnerEmail(assetId, userEmail)
                .orElseThrow(() -> new RuntimeException("Asset not found or not owned by user"));

        String oldName = asset.getName();
        BigDecimal oldValue = asset.getValue();
        int oldQuantity = asset.getQuantity();

        asset.setName(request.getName().trim());
        asset.setValue(request.getValue());
        asset.setQuantity(request.getQuantity());
        asset.setUpdatedAt(LocalDateTime.now());

        try {
            Asset saved = assetRepository.save(asset);

            auditLogger.info("TRANSACTION EVENT user={} action=EDIT_ASSET status=SUCCESS assetId={} oldName={} oldValue={} oldQuantity={} newName={} newValue={} newQuantity={}",
                    LoggingUtil.sanitizeForLog(userEmail), saved.getId(), 
                    LoggingUtil.sanitizeForLog(oldName), oldValue, oldQuantity,
                    LoggingUtil.sanitizeForLog(saved.getName()), saved.getValue(), saved.getQuantity());

            return saved;
        } catch (Exception e) {
            auditLogger.error("TRANSACTION EVENT user={} action=EDIT_ASSET status=FAILED assetId={} newName={} newValue={} newQuantity={} error={}",
                    LoggingUtil.sanitizeForLog(userEmail), assetId, LoggingUtil.sanitizeForLog(request.getName()), request.getValue(), request.getQuantity(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteOwnAsset(Long assetId, String userEmail) {
        Asset asset = assetRepository.findByIdAndOwnerEmail(assetId, userEmail)
                .orElseThrow(() -> new RuntimeException("Asset not found or not owned by user"));

        try {
            auditLogger.info("TRANSACTION EVENT user={} action=DELETE_ASSET status=SUCCESS assetId={} assetName={} value={} quantity={}",
                    LoggingUtil.sanitizeForLog(userEmail), asset.getId(), LoggingUtil.sanitizeForLog(asset.getName()), asset.getValue(), asset.getQuantity());

            assetRepository.delete(asset);
        } catch (Exception e) {
            auditLogger.error("TRANSACTION EVENT user={} action=DELETE_ASSET status=FAILED assetId={} error={}",
                    LoggingUtil.sanitizeForLog(userEmail), assetId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void adminDeleteAsset(Long assetId, String adminEmail) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        try {
            auditLogger.info("TRANSACTION EVENT admin={} action=DELETE_ASSET status=SUCCESS assetId={} assetName={} value={} quantity={}",
                    LoggingUtil.sanitizeForLog(adminEmail), asset.getId(), LoggingUtil.sanitizeForLog(asset.getName()), asset.getValue(), asset.getQuantity());

            assetRepository.delete(asset);
        } catch (Exception e) {
            auditLogger.error("TRANSACTION EVENT admin={} action=DELETE_ASSET status=FAILED assetId={} error={}",
                    LoggingUtil.sanitizeForLog(adminEmail), assetId, e.getMessage());
            throw e;
        }
    }
}