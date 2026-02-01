package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreatePost {

  PostRepository postRepository;
    UserRepository userRepository;

    public CreatePost(PostRepository postRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public Post handle(CreatePostRequest req) throws NullException {
        //  Validation
        if (req.title() == null || req.title().isBlank()) {
            throw new NullException("Title cannot be blank");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new NullException("Content cannot be blank");
        }
        if (req.authorId() == null) {
            throw new NullException("Author Id cannot be blank");
        }

        //  Fetch User entity
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NullException("Author not found"));

        //  Create Post entity
        Post post = Post.create(req.title(), req.content(), author);

        // 4️⃣ Save to DB
        postRepository.save(post);

        return post;
    }


}
