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

import com.github.al.realworld.api.command.RegisterUser
import com.github.al.realworld.api.command.RegisterUserResult
import com.github.al.realworld.application.UserAssembler
import com.github.al.realworld.application.exception.BadRequestException
import com.github.al.realworld.application.exception.BadRequestException.badRequest
import com.github.al.realworld.application.service.JwtService
import com.github.al.realworld.bus.CommandHandler
import com.github.al.realworld.domain.model.User
import com.github.al.realworld.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RegisterUserHandler(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) : CommandHandler<RegisterUserResult, RegisterUser> {

    @Transactional
    override fun handle(command: RegisterUser): RegisterUserResult {
        require(userRepository.findByEmail(command.email) == null) {
            throw badRequest("user [email=${command.email}] already exists")
        }

        require(userRepository.findByUsername(command.username) == null) {
            throw badRequest("user [name=${command.username}] already exists")
        }

        val user = User(
            id = UUID.randomUUID(),
            username = command.username,
            email = command.email,
            password = passwordEncoder.encode(command.password)
        )
        userRepository.save(user)

        return RegisterUserResult(UserAssembler.assemble(user, jwtService))
    }
}
