package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.dto.FrontendConfigurationDTO;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.repo.jpa.RuntimeSettingsRepository;
import com.github.nkonev.blog.security.BlogSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.sql.SQLException;
import static com.github.nkonev.blog.Constants.Urls.*;

@RestController
public class ConfigurationController {

    @Autowired
    private RuntimeSettingsRepository runtimeSettingsRepository;

    @Autowired
    private BlogSecurityService blogSecurityService;

    @Autowired
    private ImageSettingsUploadController imageSettingsUploadController;

    public static final String IMAGE_PART = "image";
    public static final String DTO_PART = "dto";


    private static final String IMAGE_BACKGROUND = "image.background";
    private static final String HEADER = "header";
    private static final String SUB_HEADER = "header.sub";
    private static final String TITLE_TEMPLATE = "title.template";

    @GetMapping(API+CONFIG)
    public FrontendConfigurationDTO getConfig(@AuthenticationPrincipal UserAccountDetailsDTO userAccount){
        String imageBackground = runtimeSettingsRepository.findById(IMAGE_BACKGROUND).orElseThrow().getValue();
        String header = runtimeSettingsRepository.findById(HEADER).orElseThrow().getValue();
        String subHeader = runtimeSettingsRepository.findById(SUB_HEADER).orElseThrow().getValue();
        String titleTemplate = runtimeSettingsRepository.findById(TITLE_TEMPLATE).orElseThrow().getValue();
        boolean showSettings = blogSecurityService.hasSettingsPermission(userAccount);
        return new FrontendConfigurationDTO(header, subHeader, titleTemplate, imageBackground, showSettings);
    }

    @Transactional
    @PutMapping(value = API+CONFIG, consumes = {"multipart/form-data"})
    @PreAuthorize("@blogSecurityService.hasSettingsPermission(#userAccount)")
    public FrontendConfigurationDTO putConfig(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestPart(value = DTO_PART) FrontendConfigurationDTO dto,
            @RequestPart(value = IMAGE_PART, required = false) MultipartFile imagePart
    ) throws SQLException {
        var runtimeSettingsBackgroundImage = runtimeSettingsRepository.findById(IMAGE_BACKGROUND).orElseThrow();

        // update image
        if (imagePart!= null && !imagePart.isEmpty()) {
            var imageResponse = imageSettingsUploadController.postImage(imagePart, userAccount);
            String relativeUrl = imageResponse.getRelativeUrl();
            runtimeSettingsBackgroundImage.setValue(relativeUrl);
        }

        // remove image
        if (Boolean.TRUE.equals(dto.getRemoveImageBackground())) {
            runtimeSettingsBackgroundImage.setValue(null);
        }

        // update header
        var runtimeSettingsHeader = runtimeSettingsRepository.findById(HEADER).orElseThrow();
        runtimeSettingsHeader.setValue(dto.getHeader());

        // update subheader
        var runtimeSettingsSubHeader = runtimeSettingsRepository.findById(SUB_HEADER).orElseThrow();
        runtimeSettingsSubHeader.setValue(dto.getSubHeader());

        // update title
        var runtimeTitleTemplate = runtimeSettingsRepository.findById(TITLE_TEMPLATE).orElseThrow();
        runtimeTitleTemplate.setValue(dto.getTitleTemplate());

        return getConfig(userAccount);
    }
}
