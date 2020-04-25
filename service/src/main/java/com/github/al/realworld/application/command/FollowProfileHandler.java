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
package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.FollowProfile;
import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.application.ProfileAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.model.FollowRelation;
import com.github.al.realworld.domain.model.FollowRelationId;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.BadRequestException.badRequest;
import static com.github.al.realworld.application.exception.NotFoundException.notFound;

/**
 * follower - one who follows someone (current user)
 * followee - is one who is followed
 */
@RequiredArgsConstructor
@Service
public class FollowProfileHandler implements CommandHandler<FollowProfileResult, FollowProfile> {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public FollowProfileResult handle(FollowProfile command) {
        User currentUser = userRepository.findByUsername(command.getFollower())
                .orElseThrow(() -> badRequest("user [name=%s] does not exist", command.getFollower()));

        User followee = userRepository.findByUsername(command.getFollowee())
                .orElseThrow(() -> notFound("user [name=%s] does not exist", command.getFollowee()));

        FollowRelation follow = new FollowRelation(
                new FollowRelationId(currentUser.getId(), followee.getId()),
                currentUser,
                followee);

        User alteredFollowee = followee.toBuilder().follower(follow).build();
        userRepository.save(alteredFollowee);

        return new FollowProfileResult(ProfileAssembler.assemble(alteredFollowee, currentUser));
    }

}
