package com.ats.job;

import com.ats.entity.Tag;
import com.ats.entity.TagCategory;
import com.ats.job.dto.TagVO;
import com.ats.repository.TagMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    /** 标签库全量列表（前端选 tags 时下拉用）；按 category, name 排序方便分组。 */
    public List<TagVO> listAll() {
        return tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .orderByAsc(Tag::getCategory)
                        .orderByAsc(Tag::getName))
                .stream()
                .map(TagService::toVO)
                .toList();
    }

    static TagVO toVO(Tag t) {
        return TagVO.builder()
                .id(t.getId())
                .slug(t.getSlug())
                .name(t.getName())
                .category(TagCategory.valueOf(t.getCategory()))
                .build();
    }
}
