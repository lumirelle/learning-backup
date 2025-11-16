package com.dorm.service.board;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.board.BoardPO;

import java.util.List;

public interface BoardService extends IService<BoardPO> {

    List<BoardPO> listRootBoards();

    List<BoardPO> listChildrenBoardsById(Integer id);

}
