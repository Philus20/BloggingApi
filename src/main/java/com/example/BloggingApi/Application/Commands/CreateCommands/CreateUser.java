package com.example.BloggingApi.Application.Commands.CreateCommands;


import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateUser {

    UserRepository userRepository;

    public CreateUser( UserRepository userRepository) {
    }

    public User handle(CreateUserRequest req) throws NullException {
        //  Validation
        if (req.email() == null || req.email().isBlank()) {
            throw new NullException("Title cannot be blank");
        }
        if (req.username() == null || req.username().isBlank()) {
            throw new NullException("Content cannot be blank");
        }
        if (req.password() == null) {
            throw new NullException("Author Id cannot be blank");
        }

        //  Fetch User entity
        User author = userRepository.findByUsername(req.username());

        if(author != null){
            throw new NullException("Username already exists");
        }

        //  Create Post entity
      User user = User.create(req.username(), req.email(), req.password());

        // 4️⃣ Save to DB
        userRepository.save(user);

        return user;
    }


}
