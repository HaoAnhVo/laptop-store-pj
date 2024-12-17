package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Comment;
import com.codegym.salesmanagement.repository.ICommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    private ICommentRepository iCommentRepository;

    public Comment getCommentById(Long commentId) {
        Optional<Comment> optionalComment = iCommentRepository.findById(commentId);
        return optionalComment.orElse(null);
    }

    public Comment saveComment(Comment comment) {
        return iCommentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        iCommentRepository.deleteById(commentId);
    }
}
