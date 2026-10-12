
package com.vanexa.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "blog_visibility",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"blog_id", "visibility_type_id"}
        )
)
public class BlogVisibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visibility_type_id", nullable = false)
    private VisibilityType visibilityType;

    public BlogVisibility() {
    }

    public BlogVisibility(Blog blog, VisibilityType visibilityType) {
        this.blog = blog;
        this.visibilityType = visibilityType;
    }

    public Long getId() {
        return id;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public void setVisibilityType(VisibilityType visibilityType) {
        this.visibilityType = visibilityType;
    }
}
