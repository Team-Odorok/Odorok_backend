package com.odorok.OdorokApplication.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponse {
    private Long id;

    @JsonProperty("connected_at")
    private String connectedAt;

    private Properties properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;

        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        // 동의 관련 boolean 필드들: JSON 키와 정확히 매핑
        @JsonProperty("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;

        @JsonProperty("profile_image_needs_agreement")
        private Boolean profileImageNeedsAgreement;

        private Profile profile;

        @JsonProperty("has_email")
        private Boolean hasEmail;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        // 카카오가 쓰는 필드명: is_email_valid / is_email_verified
        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;

        private String email;

        // 필요하면 추가 필드(phone_number, birthday 등)을 여기에 넣어도 됨
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String nickname;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("is_default_image")
        private Boolean isDefaultImage;

        @JsonProperty("is_default_nickname")
        private Boolean isDefaultNickname;
    }

    /**
     * 편의 메서드: 안전하게 이메일 반환 (없으면 null)
     */
    public String getSafeEmail() {
        return Optional.ofNullable(kakaoAccount)
                .map(KakaoAccount::getEmail)
                .orElse(null);
    }

    /**
     * 편의 메서드: 우선순위로 닉네임 반환:
     * kakao_account.profile.nickname -> properties.nickname -> null
     */
    public String getSafeNickname() {
        return Optional.ofNullable(kakaoAccount)
                .map(KakaoAccount::getProfile)
                .map(Profile::getNickname)
                .orElseGet(() -> properties != null ? properties.getNickname() : null);
    }

    /**
     * 편의 메서드: 우선순위로 프로필이미지 URL 반환:
     * kakao_account.profile.profile_image_url -> properties.profile_image -> null
     */
    public String getSafeProfileImageUrl() {
        return Optional.ofNullable(kakaoAccount)
                .map(KakaoAccount::getProfile)
                .map(Profile::getProfileImageUrl)
                .orElseGet(() -> properties != null ? properties.getProfileImage() : null);
    }
}
