
package com.vanexa.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_tags")
public class BlogTag {

    @EmbeddedId
    private BlogTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public BlogTag() {
    }

    public BlogTag(Blog blog, Tag tag) {
        this.blog = blog;
        this.tag = tag;
        this.id = new BlogTagId(blog.getId(), tag.getId());
    }

    public BlogTagId getId() {
        return id;
    }

    public void setId(BlogTagId id) {
        this.id = id;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
