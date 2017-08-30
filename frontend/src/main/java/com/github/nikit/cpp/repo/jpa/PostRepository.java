package com.github.nikit.cpp.repo.jpa;

import com.github.nikit.cpp.entity.jpa.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.Supplier;

@Validated
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.owner.id = ?#{ principal?.id }")
    Page<Post> findMyPosts(Pageable pageable);

    Page<Post> findByTextContainsOrTitleContainsOrderByIdDesc(Pageable page, String textContain, String titleContain);

    Optional<Post> findById(Long id);

    @Query(value = "SELECT p.* FROM posts.post p WHERE p.id < :post ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Post getLeft(@Param("post") @NotNull Long post);

    @Query(value = "SELECT p.* FROM posts.post p WHERE p.id > :post ORDER BY id ASC LIMIT 1", nativeQuery = true)
    Post getRight(@Param("post") @NotNull Long post);

    Post findByTitle(@NotNull String title);
}
