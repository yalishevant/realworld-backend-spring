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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tbl_user")
data class User(
    @Id
    var id: UUID = UUID.randomUUID(),
    var username: String? = null,
    var email: String? = null,
    var password: String? = null,
    var bio: String? = null,
    var image: String? = null,
    @OneToMany(
        mappedBy = "followee",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var followers: MutableSet<FollowRelation> = mutableSetOf()
) {
    // No-args constructor required by JPA
    constructor() : this(UUID.randomUUID())

    // Methods for collection manipulation
    fun addFollower(follower: FollowRelation): User {
        followers.add(follower)
        return this
    }

    fun addFollowers(newFollowers: Collection<FollowRelation>): User {
        followers.addAll(newFollowers)
        return this
    }

    fun clearFollowers(): User {
        followers.clear()
        return this
    }

    companion object {
        // No longer needed - using data class features instead
    }

    // Override equals and hashCode to use only ID for comparison (JPA best practice)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
