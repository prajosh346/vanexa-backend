
package com.vanexa.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BlogTagId implements Serializable {

    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "tag_id")
    private Long tagId;

    public BlogTagId() {
    }

    public BlogTagId(Long blogId, Long tagId) {
        this.blogId = blogId;
        this.tagId = tagId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlogTagId that)) return false;
        return Objects.equals(blogId, that.blogId)
                && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blogId, tagId);
    }
}
