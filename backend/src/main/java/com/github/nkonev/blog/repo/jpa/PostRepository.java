package com.github.nkonev.blog.repo.jpa;

import com.github.nkonev.blog.entity.jpa.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

@Validated
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.owner.id = ?#{ principal?.id }")
    Page<Post> findMyPosts(Pageable pageable);

    @Query("select p.id from Post p")
    Collection<Long> findPostIds();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE posts.post SET owner_id = :toUserId WHERE owner_id = :fromUserId", nativeQuery = true)
    void moveToAnotherUser(@Param("fromUserId") long fromUserId, @Param("toUserId") long toUserId);
}
