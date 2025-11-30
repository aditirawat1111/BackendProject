package com.aditi.backendcapstoneproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProfileResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String address;
    private String role;
    private Date createdAt;
    private Date lastModified;

    public static ProfileResponseDto from(com.aditi.backendcapstoneproject.model.User user) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastModified(user.getLastModified());
        return dto;
    }

}

