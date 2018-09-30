package com.github.nkonev.blog.repo.jpa;

import com.github.nkonev.blog.entity.jpa.RuntimeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuntimeSettingsRepository extends JpaRepository<RuntimeSettings, String> {

}
