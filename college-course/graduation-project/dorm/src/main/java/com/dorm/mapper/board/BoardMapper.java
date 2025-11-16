package com.dorm.mapper.board;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.board.BoardPO;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardMapper extends BaseMapper<BoardPO> {
}
