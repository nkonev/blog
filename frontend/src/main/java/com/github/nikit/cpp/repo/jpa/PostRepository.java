package com.github.nikit.cpp.repo.jpa;

import com.github.nikit.cpp.entity.jpa.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.owner.id = ?#{ principal?.id }")
    Page<Post> findMyPosts(Pageable pageable);

    Page<Post> findByTextContainsOrTitleContainsOrderByIdDesc(Pageable page, String textContain, String titleContain);

    Optional<Post> findById(Long id);
}
