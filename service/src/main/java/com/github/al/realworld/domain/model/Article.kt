/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.domain.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "tbl_article")
data class Article(
    @Id
    var id: UUID? = null,
    var slug: String? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: ZonedDateTime? = null,
    var updatedAt: ZonedDateTime? = null,

    @Lob
    var body: String? = null,

    @OneToOne
    var author: User? = null,

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "article_id")
    var comments: MutableSet<Comment> = mutableSetOf(),

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "tbl_article_tags",
        joinColumns = [JoinColumn(name = "article_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "tbl_article_favorites",
        joinColumns = [JoinColumn(name = "article_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var favoredUsers: MutableSet<User> = mutableSetOf()
) {
    // No-args constructor required by JPA
    constructor() : this(null)

    // Methods for collection manipulation
    fun comment(comment: Comment): Article {
        comments.add(comment)
        return this
    }

    fun comments(newComments: Set<Comment>): Article {
        comments.clear()
        comments.addAll(newComments)
        return this
    }

    fun tag(tag: Tag): Article {
        tags.add(tag)
        return this
    }

    fun tags(newTags: Set<Tag>): Article {
        tags.clear()
        tags.addAll(newTags)
        return this
    }

    fun favoredUser(user: User): Article {
        favoredUsers.add(user)
        return this
    }

    fun favoredUsers(newFavoredUsers: Set<User>): Article {
        favoredUsers.clear()
        favoredUsers.addAll(newFavoredUsers)
        return this
    }

    fun clearFavoredUsers(): Article {
        favoredUsers.clear()
        return this
    }

    companion object {
        // For backward compatibility
        fun builder(): Article {
            return Article()
        }
    }

    // Override equals and hashCode to use only ID for comparison (JPA best practice)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Article

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
