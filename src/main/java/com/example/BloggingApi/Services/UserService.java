package com.example.BloggingApi.Services;

import com.example.BloggingApi.RequestsDTO.CreateUserRequest;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.EditUserRequest;
import com.example.BloggingApi.ResposesDTO.UserResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final ValidateSearchParams validateSearchParams;

    public UserService(UserRepository userRepository, ValidateSearchParams validateSearchParams) {
        this.userRepository = userRepository;
        this.validateSearchParams = validateSearchParams;
    }



    @CacheEvict(value = {"users"}, allEntries = true)
    public User createUser(CreateUserRequest req)  {


        //  Fetch User entity

        User existingUser = userRepository.findByString(req.username());

        if (existingUser != null) {
            throw new NullException("Username already exists");
        }

        User user = User.create(req.username(), req.email(), req.password());

        userRepository.create(user);

        return user;
    }

    @Cacheable(value = "userPages", key = "#page + '-' + #size + '-' + #sortBy + '-' + #ascending")
    public Page<UserResponse> getAllUsers(
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId)  {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NullException("User not found"));
    }


    @CacheEvict(value = {"users", "userPages"}, allEntries = true)
    public User editUser(EditUserRequest request) throws NullException {
        User user = userRepository.findByInteger(request.id().intValue());

        if (user == null) {
            throw new NullException("User not found");
        }

        user.update(request.username(), request.email());

        userRepository.update(user);

        return user;
    }




    @CacheEvict(value = {"users"}, key = "#userId", allEntries = false)
    public void deleteUser(Long userId) throws NullException {
        User user = userRepository.findByInteger(userId.intValue());

        if (user == null) {
            throw new NullException("User not found");
        }

        userRepository.delete(userId.intValue());
    }

    public Page<UserResponse> searchUsers(
            String keyword,
            String username,
            String email,
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        validateSearchParams.Validate(keyword, username, email);

        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users;

        if (validateSearchParams.hasText(keyword)) {
            users = userRepository.searchByKeyword(keyword, pageable);
        } else if (validateSearchParams.hasText(username)) {
            users = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else {
            users = userRepository.findByEmailContainingIgnoreCase(email, pageable);
        }

        return users.map(this::mapToUserResponse);
    }





    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
