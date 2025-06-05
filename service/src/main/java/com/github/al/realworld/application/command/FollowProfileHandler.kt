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
package com.github.al.realworld.application.command

import com.github.al.realworld.api.command.FollowProfile
import com.github.al.realworld.api.command.FollowProfileResult
import com.github.al.realworld.application.dto.toProfileDto
import com.github.al.realworld.application.exception.BadRequestException
import com.github.al.realworld.application.exception.BadRequestException.*
import com.github.al.realworld.application.exception.NotFoundException
import com.github.al.realworld.application.exception.NotFoundException.*
import com.github.al.realworld.bus.CommandHandler
import com.github.al.realworld.domain.model.FollowRelation
import com.github.al.realworld.domain.model.FollowRelationId
import com.github.al.realworld.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * follower - one who follows someone (current user)
 * followee - is one who is followed
 */
@Service
class FollowProfileHandler(
    private val userRepository: UserRepository
) : CommandHandler<FollowProfileResult, FollowProfile> {

    @Transactional
    override fun handle(command: FollowProfile): FollowProfileResult {
        val currentUser = userRepository.findByUsername(command.follower)
            ?: throw badRequest("user [name=%s] does not exist", command.follower)

        val followee = userRepository.findByUsername(command.followee)
            ?: throw notFound("user [name=%s] does not exist", command.followee)

        val follow = FollowRelation(FollowRelationId(currentUser.id, followee.id), currentUser, followee)

        val alteredFollowee = followee.addFollower(follow)
        userRepository.save(alteredFollowee)

        return FollowProfileResult(alteredFollowee.toProfileDto(currentUser))
    }
}
