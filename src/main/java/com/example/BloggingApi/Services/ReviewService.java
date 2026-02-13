package com.example.BloggingApi.Services;

import com.example.BloggingApi.RequestsDTO.CreateReviewRequest;
import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.Review;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.EditReviewRequest;
import com.example.BloggingApi.ResposesDTO.ReviewResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private  final ValidateSearchParams validateSearchParams;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, PostRepository postRepository, ValidateSearchParams validateSearchParams) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.validateSearchParams = validateSearchParams;
    }


    public Review createReview(CreateReviewRequest req)  {


        // Fetch User entity
        User user = userRepository.findByInteger(req.userId().intValue());
        
        if (user == null) {
            throw new NullException("User not found");
        }

        // Fetch Post entity
        Post post = postRepository.findByInteger(req.postId().intValue());
        
        if (post == null) {
            throw new NullException("Post not found");
        }

        // Create Review entity
        Review review = Review.create(req.rating(), req.comment(), user, post);

        // Save to DB
        reviewRepository.create(review);

        return review;
    }


    public Review editReview(EditReviewRequest request) throws NullException {
        Review review = reviewRepository.findByInteger(request.id().intValue());



        review.update(request.rating(), request.comment());

        return review;
    }

    public void deleteReview(Long reviewId) throws NullException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NullException("Review not found"));

        reviewRepository.delete(review);
    }

    //Queries
    public Page<ReviewResponse> getAllReviews(
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

        return reviewRepository.findAll(pageable)
                .map(ReviewResponse::from);
    }


    public Review getReviewById(Long reviewId) throws NullException {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NullException("Review not found"));
    }




    public Page<ReviewResponse> search(
            String comment,
            Integer rating,
            String author,
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        validateSearchParams.validateSearchParamsForReview(comment, rating, author);

        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews;

        if (validateSearchParams.hasText(comment)) {
            reviews = reviewRepository.findByCommentContainingIgnoreCase(comment, pageable);
        } else if (rating != null) {
            reviews = reviewRepository.findByRating(rating, pageable);
        } else {
            reviews = reviewRepository.findByUserUsernameContainingIgnoreCase(author, pageable);
        }

        return reviews.map(ReviewResponse::from);
    }


}
