
package com.vanexa.backend.repository;

import com.vanexa.backend.entity.BlogTag;
import com.vanexa.backend.entity.BlogTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogTagRepository
        extends JpaRepository<BlogTag, BlogTagId> {

    List<BlogTag> findByBlog_Id(Long blogId);
    void deleteByBlog_Id(Long blogId);
    void deleteByBlog_IdAndTag_Id(Long blogId, Long tagId);
}
