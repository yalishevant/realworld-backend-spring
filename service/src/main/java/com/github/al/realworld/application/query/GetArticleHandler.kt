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

import com.github.al.realworld.api.query.GetArticle
import com.github.al.realworld.api.query.GetArticleResult
import com.github.al.realworld.application.dto.toDto
import com.github.al.realworld.bus.QueryHandler
import com.github.al.realworld.domain.repository.ArticleRepository
import com.github.al.realworld.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.github.al.realworld.application.exception.NotFoundException

@Service
class GetArticleHandler(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) : QueryHandler<GetArticleResult, GetArticle> {

    @Transactional(readOnly = true)
    override fun handle(query: GetArticle): GetArticleResult {
        val article = articleRepository.findBySlug(query.slug)
            .orElseThrow { NotFoundException.notFound("article [slug=%s] does not exists", query.slug) }

        val currentUser = if (query.currentUsername != null) userRepository.findByUsername(query.currentUsername) else null

        return GetArticleResult(article.toDto(currentUser))
    }
}
