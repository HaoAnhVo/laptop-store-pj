package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Post;
import com.codegym.salesmanagement.repository.IPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private IPostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll(Sort.by("createdAt").descending());
    }

    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    public long countPost() {
        return postRepository.count();
    }

    public int findPageContainingPost(Long postId, int pageSize) {
        List<Post> posts = postRepository.findAll();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                return i / pageSize;
            }
        }
        return 0;
    }
}
