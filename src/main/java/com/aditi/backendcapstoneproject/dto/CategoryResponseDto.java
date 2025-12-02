package com.aditi.backendcapstoneproject.dto;

import com.aditi.backendcapstoneproject.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponseDto {

    private Long id;
    private String name;
    private String description;

    public static CategoryResponseDto from(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}


