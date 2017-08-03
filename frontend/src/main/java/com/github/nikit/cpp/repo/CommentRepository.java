package com.github.nikit.cpp.repo;

import com.github.nikit.cpp.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findCommentByPostIdOrderByIdAsc(Pageable page, long postId);
}
