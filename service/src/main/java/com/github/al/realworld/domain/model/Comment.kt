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

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "tbl_comment")
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,
    
    var createdAt: ZonedDateTime? = null,
    var updatedAt: ZonedDateTime? = null,
    
    @Lob
    var body: String? = null,
    
    @OneToOne
    var author: User? = null
) {
    // No-args constructor required by JPA
    constructor() : this(null, null, null, null, null)

    // Builder-like methods for compatibility
    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private val comment = Comment()

        fun id(id: Long?): Builder {
            comment.id = id
            return this
        }

        fun createdAt(createdAt: ZonedDateTime?): Builder {
            comment.createdAt = createdAt
            return this
        }

        fun updatedAt(updatedAt: ZonedDateTime?): Builder {
            comment.updatedAt = updatedAt
            return this
        }

        fun body(body: String?): Builder {
            comment.body = body
            return this
        }

        fun author(author: User?): Builder {
            comment.author = author
            return this
        }

        fun build(): Comment {
            return comment
        }
    }

    // Override equals and hashCode to use only ID for comparison (JPA best practice)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}