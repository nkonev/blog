package com.github.nkonev.repo.jpa;

import com.github.nkonev.entity.jpa.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findCommentByPostIdOrderByIdAsc(Pageable page, long postId);

    void deleteByPostId(long postId);

    long countByPostId(long commentId);
}
