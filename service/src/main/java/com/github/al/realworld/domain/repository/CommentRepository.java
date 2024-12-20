package com.github.al.realworld.domain.repository;

import com.github.al.realworld.domain.model.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {

    boolean existsById(UUID articleId, long id);

    Optional<Comment> findById(long id);

    Optional<Comment> findByIdAndArticleId(Long id, UUID articleId);

    List<Comment> findAllByArticleId(UUID articleId);

    Comment save(Comment comment);

    void deleteById(long id);

}
