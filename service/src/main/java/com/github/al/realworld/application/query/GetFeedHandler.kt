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
package com.github.al.realworld.application.query

import com.github.al.realworld.api.dto.ArticleDto
import com.github.al.realworld.api.query.GetFeed
import com.github.al.realworld.api.query.GetFeedResult
import com.github.al.realworld.application.ArticleAssembler
import com.github.al.realworld.application.exception.BadRequestException
import com.github.al.realworld.bus.QueryHandler
import com.github.al.realworld.domain.repository.ArticleRepository
import com.github.al.realworld.domain.repository.FollowRelationRepository
import com.github.al.realworld.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetFeedHandler(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
    private val followRelationRepository: FollowRelationRepository
) : QueryHandler<GetFeedResult, GetFeed> {

    @Transactional(readOnly = true)
    override fun handle(query: GetFeed): GetFeedResult {
        val currentUser = userRepository.findByUsername(query.currentUsername)
            ?: throw BadRequestException.badRequest("user [name=%s] does not exist", query.currentUsername)

        val relations = followRelationRepository.findByFollowerId(currentUser.id)

        val followeesUsernames = relations.map { followRelation -> followRelation.followee?.id }

        val articles = articleRepository
            .findByFollowees(followeesUsernames, query.limit, query.offset)

        val results = mutableListOf<ArticleDto>()

        articles.forEach { article -> results.add(ArticleAssembler.assemble(article, currentUser)) }

        return GetFeedResult(results, results.size)
    }
}
