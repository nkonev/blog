package com.github.nkonev.blog.dto;

public class SettingsDTO {
    private String header;

    private String subHeader;

    private String titleTemplate;

    private String imageBackground;

    private Boolean removeImageBackground;

    private boolean showSettings;

    public SettingsDTO() { }

    public SettingsDTO(String header, String subHeader, String titleTemplate, String imageBackground, boolean showSettings) {
        this.header = header;
        this.subHeader = subHeader;
        this.titleTemplate = titleTemplate;
        this.imageBackground = imageBackground;
        this.showSettings = showSettings;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getImageBackground() {
        return imageBackground;
    }

    public void setImageBackground(String imageBackground) {
        this.imageBackground = imageBackground;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    public Boolean getRemoveImageBackground() {
        return removeImageBackground;
    }

    public void setRemoveImageBackground(Boolean removeImageBackground) {
        this.removeImageBackground = removeImageBackground;
    }

    public String getSubHeader() {
        return subHeader;
    }

    public void setSubHeader(String subHeader) {
        this.subHeader = subHeader;
    }
}
