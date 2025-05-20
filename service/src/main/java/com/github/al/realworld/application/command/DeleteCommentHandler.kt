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

import com.github.al.realworld.api.command.DeleteComment
import com.github.al.realworld.api.command.DeleteCommentResult
import com.github.al.realworld.application.exception.ForbiddenException
import com.github.al.realworld.application.exception.NotFoundException
import com.github.al.realworld.bus.CommandHandler
import com.github.al.realworld.domain.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteCommentHandler(
    private val articleRepository: ArticleRepository
) : CommandHandler<DeleteCommentResult, DeleteComment> {

    @Transactional
    override fun handle(command: DeleteComment): DeleteCommentResult {
        val article = articleRepository.findBySlug(command.slug)
            .orElseThrow { NotFoundException.notFound("article [slug=%s] does not exist", command.slug) }

        val comment = article.comments.stream()
            .filter { c -> c.id == command.id }
            .findFirst()
            .orElseThrow { NotFoundException.notFound("comment [id=%s] does not exist", command.id) }

        if (comment.author!!.username != command.currentUsername) {
            throw ForbiddenException.forbidden("comment [id=%s] is not owned by %s", comment.id, command.currentUsername)
        }

        // Filter out the comment to be deleted
        val alteredComments = article.comments.filter { it != comment }.toSet()

        article.comments(alteredComments)
        articleRepository.save(article)

        return DeleteCommentResult()
    }
}
