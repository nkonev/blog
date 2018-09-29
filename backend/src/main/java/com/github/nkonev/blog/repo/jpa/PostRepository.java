package com.github.nkonev.blog.repo.jpa;

import com.github.nkonev.blog.entity.jpa.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query(value = "SELECT p.* FROM posts.post p WHERE p.id < :post ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Post getLeft(@Param("post") @NotNull Long post);

    @Query(value = "SELECT p.* FROM posts.post p WHERE p.id > :post ORDER BY id ASC LIMIT 1", nativeQuery = true)
    Post getRight(@Param("post") @NotNull Long post);

    @Query("select p.id from Post p")
    Collection<Long> findPostIds();
}
