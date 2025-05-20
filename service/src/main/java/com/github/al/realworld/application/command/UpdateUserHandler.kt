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

import com.github.al.realworld.api.command.UpdateUser
import com.github.al.realworld.api.command.UpdateUserResult
import com.github.al.realworld.application.UserAssembler
import com.github.al.realworld.application.exception.BadRequestException
import com.github.al.realworld.application.exception.NotFoundException
import com.github.al.realworld.application.service.JwtService
import com.github.al.realworld.bus.CommandHandler
import com.github.al.realworld.domain.model.User
import com.github.al.realworld.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional

@Service
class UpdateUserHandler(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val encoder: PasswordEncoder
) : CommandHandler<UpdateUserResult, UpdateUser> {

    @Transactional
    override fun handle(command: UpdateUser): UpdateUserResult {
        val user = userRepository.findByUsername(command.currentUsername)
            .orElseThrow { NotFoundException.notFound("user [name=%s] does not exist", command.currentUsername) }

        if (command.username != null
            && command.username != user.username
            && userRepository.findByUsername(command.username).isPresent) {
            throw BadRequestException.badRequest("user [name=%s] already exists", command.username)
        }

        if (command.email != null
            && command.email != user.email
            && userRepository.findByEmail(command.email).isPresent) {
            throw BadRequestException.badRequest("user [email=%s] already exists", command.email)
        }

        // Use copy() method instead of toBuilder().build()
        val alteredUser = user.copy(
            email = command.email ?: user.email,
            username = command.username ?: user.username,
            password = if (command.password != null) encoder.encode(command.password) else user.password,
            bio = command.bio ?: user.bio,
            image = command.image ?: user.image
        )

        val savedUser = userRepository.save(alteredUser)

        return UpdateUserResult(UserAssembler.assemble(savedUser, jwtService))
    }
}
