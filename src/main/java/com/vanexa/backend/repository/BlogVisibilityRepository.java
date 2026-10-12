
package com.vanexa.backend.repository;

import com.vanexa.backend.entity.BlogVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogVisibilityRepository
        extends JpaRepository<BlogVisibility, Long> {

    List<BlogVisibility> findByBlog_Id(Long blogId);

    void deleteByBlog_Id(Long blogId);

    void deleteByBlog_IdAndVisibilityType_Id(
            Long blogId,
            Long visibilityTypeId
    );

    boolean existsByBlog_IdAndVisibilityType_Id(
            Long blogId,
            Long visibilityTypeId
    );
}
