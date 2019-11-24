package org.dragonli.service.modules.accountmanagerservice.dto;

public class AccountDto {
    private Long userId;
    private String reflexId;
    private String assetName;

    public AccountDto(Long userId, String reflexId, String assetName) {
        this.userId = userId;
        this.reflexId = reflexId;
        this.assetName = assetName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReflexId() {
        return reflexId;
    }

    public void setReflexId(String reflexId) {
        this.reflexId = reflexId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
