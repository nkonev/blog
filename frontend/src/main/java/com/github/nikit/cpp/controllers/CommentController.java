package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.utils.PageUtils;
import com.github.nikit.cpp.converter.CommentConverter;
import com.github.nikit.cpp.dto.CommentDTO;
import com.github.nikit.cpp.dto.CommentDTOWithAuthorization;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.Comment;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.exception.BadRequestException;
import com.github.nikit.cpp.repo.jpa.CommentRepository;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RestController
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentConverter commentConverter;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * List post comments
     * @param userAccount
     * @param postId
     * @return
     */
    @GetMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID +Constants.Uls.COMMENT)
    public Collection<CommentDTOWithAuthorization> getPostComments(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // nullable
            @PathVariable(Constants.PathVariables.POST_ID) long postId,
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
            ) {

        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size));

        List<Comment> comments = commentRepository.findCommentByPostIdOrderByIdAsc(springDataPage, postId);

        return comments
                .stream()
                .map(comment -> commentConverter.convertToDto(comment, userAccount))
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID +Constants.Uls.COMMENT)
    public CommentDTOWithAuthorization addComment(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // nullable
            @PathVariable(Constants.PathVariables.POST_ID) long postId,
            @RequestBody @NotNull CommentDTO commentDTO
    ){
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (commentDTO.getId()!=0){
            throw new BadRequestException("id cannot be set");
        }

        Comment comment = commentConverter.convertFromDto(commentDTO, postId, null);

        UserAccount ua = userAccountRepository.findOne(userAccount.getId()); // Hibernate caches it
        Assert.notNull(ua, "User account not found");
        comment.setOwner(ua);
        Comment saved = commentRepository.save(comment);

        return commentConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasCommentPermission(#commentDTO, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    @PutMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID +Constants.Uls.COMMENT)
    public CommentDTOWithAuthorization updateComment (
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // nullable
            @PathVariable(Constants.PathVariables.POST_ID) long postId,
            @RequestBody @NotNull CommentDTO commentDTO
    ){
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");

        Comment found = commentRepository.findOne(commentDTO.getId());
        Assert.notNull(found, "Comment with id " + commentDTO.getId() + " not found");

        Comment updatedEntity = commentConverter.convertFromDto(commentDTO, 0, found);
        Comment saved = commentRepository.save(updatedEntity);

        return commentConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasCommentPermission(#commentId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).DELETE)")
    @DeleteMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID +Constants.Uls.COMMENT+Constants.Uls.COMMENT_ID)
    public void deleteComment(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @PathVariable(Constants.PathVariables.POST_ID) long postId,
            @PathVariable(Constants.PathVariables.COMMENT_ID) long commentId
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        commentRepository.delete(commentId);
    }


}
