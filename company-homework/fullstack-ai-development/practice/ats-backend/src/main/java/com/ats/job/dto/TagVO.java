package com.ats.job.dto;

import com.ats.entity.TagCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVO {
    private Long id;
    private String slug;
    private String name;
    private TagCategory category;
}
