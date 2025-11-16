package com.dorm.controller.board;

import com.dorm.entity.board.AddBoardDTO;
import com.dorm.entity.board.BoardPO;
import com.dorm.entity.board.BoardVO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.service.board.BoardService;
import com.dorm.service.user.UserService;
import com.dorm.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class BoardController {

    @Resource
    private UserService userService;

    @Resource
    private BoardService boardService;
    @Autowired
    private SecurityUtils securityUtils;

    @RequestMapping("/board/list")
    // 数据库中获取根目录下的所有板块信息，并将其转换为视图对象
    // 然后将这些对象添加到模型中，最后返回一个视图名称
    public String listBoard(Model model) {
        List<BoardPO> boardPOList = boardService.listRootBoards();
        List<BoardVO> boards = new ArrayList<>();
        for (BoardPO boardPO : boardPOList) {
            UserPO userPO = userService.getById(boardPO.getUserId());
            BoardVO boardVO = BoardVO.valueOf(boardPO, userPO);
            boards.add(boardVO);
        }
        model.addAttribute("boards", boards);
        return "board/list";
    }

    @RequestMapping("/board/detail/{id}")
    public String boardDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        String notExistUrl = "redirect:/board/list";

        // 检查留言是否存在
        BoardPO boardPO = boardService.getById(id);
        if (boardPO == null) {
            //在重定向请求中添加一个Flash属性，键为"msg"，值为"留言不存在"
            redirectAttributes.addFlashAttribute("msg", "留言不存在");
            return notExistUrl;
        }
        // 检查留言是否是根留言
        if (boardPO.getParentBoardId() != null || boardPO.getRootBoardId() != null) {
            redirectAttributes.addFlashAttribute("msg", "留言无法查看详情");
            return notExistUrl;
        }

        // 获取留言的用户信息
        UserPO userPO = userService.getById(boardPO.getUserId());

        BoardVO board = BoardVO.valueOf(boardPO, userPO);

        List<BoardVO> children = new ArrayList<>();

        // 查询子留言（留言最多有三层）
        List<BoardPO> childrenBoardPOList = boardService.listChildrenBoardsById(board.getId());

        // 遍历子留言，查询每个子留言的子留言
        for (BoardPO child : childrenBoardPOList) {
            // 把我们遍历的 po 转换成 BoardVO
            UserPO childUserPO = userService.getById(child.getUserId());
            BoardVO childBoard = BoardVO.valueOf(child, childUserPO);

            // 查询子留言的子留言
            List<BoardPO> grandChildrenBoardPOList = boardService.listChildrenBoardsById(child.getId());
            // PO -> VO
            List<BoardVO> grandChildren = new ArrayList<>();
            for (BoardPO grandChild : grandChildrenBoardPOList) {
                UserPO grandChildUserPO = userService.getById(grandChild.getUserId());
                BoardVO grandChildBoard = BoardVO.valueOf(grandChild, grandChildUserPO);
                grandChildren.add(grandChildBoard);
            }

            // 孙子设置到儿子上
            childBoard.setChildren(grandChildren);

            children.add(childBoard);
        }

        // 儿子设置到父亲上
        board.setChildren(children);

        model.addAttribute("board", board);

        return "board/detail";
    }

    @RequestMapping("/api/board/add")
    public String addBoard(
        @ModelAttribute @Validated AddBoardDTO boardDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/board/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 校验用户是否存在
        UserPO userPO = userService.getById(boardDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "用户不存在");
            return url;
        }

        // 父留言ID和根留言ID必须同时为空，或同时不为空
        if (!Objects.equals(boardDTO.getParentBoardId() == null, boardDTO.getRootBoardId() == null)) {
            redirectAttributes.addFlashAttribute("msg", "父留言ID和根留言ID必须同时指定或同时不指定");
        }
        // 如果指定了父留言ID，检查父留言是否存在
        // 如果父留言的父留言和它的根留言不同，表明父留言已经是第三层留言，不能继续回复
        if (boardDTO.getParentBoardId() != null) {
            BoardPO parentBoard = boardService.getById(boardDTO.getParentBoardId());
            if (parentBoard == null) {
                redirectAttributes.addFlashAttribute("msg", "父留言不存在");
                return url;
            } else if (!Objects.equals(parentBoard.getParentBoardId(), parentBoard.getRootBoardId())) {
                redirectAttributes.addFlashAttribute("msg", "父留言已经是第三层留言，不能继续回复");
                return url;
            }
        }
        // 如果指定了根留言ID，检查根留言是否存在
        if (boardDTO.getRootBoardId() != null) {
            BoardPO rootBoard = boardService.getById(boardDTO.getRootBoardId());
            if (rootBoard == null) {
                redirectAttributes.addFlashAttribute("msg", "根留言不存在");
                return url;
            }
        }

        BoardPO boardPO = BoardPO.valueOf(boardDTO);
        boardPO.setCreateTime(new Date());
        boardService.save(boardPO);

        // 如果是回复，重定向到根留言的详情页
        if (boardDTO.getRootBoardId() != null) {
            return "redirect:/board/detail/" + boardDTO.getRootBoardId();
        }

        return url;
    }

    @RequestMapping("/api/board/delete/{id}")
    public String deleteBoard(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        BoardPO boardPO = boardService.getById(id);

        if (boardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "留言不存在");
            return "redirect:/board/list";
        }

        // 检查是不是自己的留言
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO == null || !Objects.equals(userVO.getId(), boardPO.getUserId())) {
            redirectAttributes.addFlashAttribute("msg", "只能删除自己的留言");
            return "redirect:/board/list";
        }

        // 删除所有子留言（留言最多有三层）
        List<BoardPO> children = boardService.listChildrenBoardsById(id);
        for (BoardPO child : children) {
            // 删除子留言的子留言
            List<BoardPO> grandChildren = boardService.listChildrenBoardsById(child.getId());
            for (BoardPO grandChild : grandChildren) {
                boardService.removeById(grandChild.getId());
            }
            boardService.removeById(child.getId());
        }
        boardService.removeById(id);

        if (boardPO.getParentBoardId() != null) {
            // 如果是回复，重定向到根留言的详情页
            return "redirect:/board/detail/" + boardPO.getRootBoardId();
        }
        return "redirect:/board/list";
    }

}
