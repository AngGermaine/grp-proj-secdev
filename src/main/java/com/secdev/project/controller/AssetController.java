package com.secdev.project.controller;

import com.secdev.project.dto.AssetRequest;
import com.secdev.project.service.AssetService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "unknown";
    }

    @PostMapping("/add")
    public String addAsset(@ModelAttribute AssetRequest request, RedirectAttributes redirectAttributes) {
        try {
            assetService.addAsset(getCurrentUsername(), request);
            redirectAttributes.addFlashAttribute("success", "Asset added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add asset");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/edit")
    public String editAsset(@PathVariable Long id,
                            @ModelAttribute AssetRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            assetService.editOwnAsset(id, getCurrentUsername(), request);
            redirectAttributes.addFlashAttribute("success", "Asset updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update asset");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteOwnAsset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            assetService.deleteOwnAsset(id, getCurrentUsername());
            redirectAttributes.addFlashAttribute("success", "Asset deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete asset");
        }
        return "redirect:/dashboard";
    }
}