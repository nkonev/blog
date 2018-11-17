package com.github.nkonev.blog.repo.jpa;

import com.github.nkonev.blog.entity.jpa.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findCommentByPostIdOrderByIdAsc(Pageable page, long postId);

    void deleteByPostId(long postId);

    long countByPostId(long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE posts.comment SET owner_id = :toUserId WHERE owner_id = :fromUserId", nativeQuery = true)
    void moveToAnotherUser(@Param("fromUserId") long fromUserId, @Param("toUserId") long toUserId);
}
