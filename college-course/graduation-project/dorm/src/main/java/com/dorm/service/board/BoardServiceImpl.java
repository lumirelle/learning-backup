package com.dorm.service.board;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.board.BoardMapper;
import com.dorm.entity.board.BoardPO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, BoardPO>implements BoardService {

    @Override
    public List<BoardPO> listRootBoards() {
        QueryWrapper<BoardPO> qw = new QueryWrapper<>();
        qw.isNull("parent_board_id");
        qw.orderByDesc("create_time");
        return list(qw);
    }

    @Override
    public List<BoardPO> listChildrenBoardsById(Integer id) {
        QueryWrapper<BoardPO> qw = new QueryWrapper<>();
        qw.eq("parent_board_id", id);
        qw.orderByDesc("create_time");
        return list(qw);
    }
}
