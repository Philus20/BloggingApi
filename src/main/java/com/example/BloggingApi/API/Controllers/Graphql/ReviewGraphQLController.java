package com.example.BloggingApi.API.Controllers.Graphql;

import com.example.BloggingApi.API.Requests.CreateReviewRequest;
import com.example.BloggingApi.API.Requests.EditReviewRequest;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateReview;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteReview;
import com.example.BloggingApi.Application.Commands.EditCommands.EditReview;
import com.example.BloggingApi.Application.Queries.GetAllReviews;
import com.example.BloggingApi.Application.Queries.GetReviewById;
import com.example.BloggingApi.Application.Queries.SearchReviews;
import com.example.BloggingApi.Domain.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ReviewGraphQLController {

    private final CreateReview createReviewHandler;
    private final EditReview editReviewHandler;
    private final DeleteReview deleteReviewHandler;
    private final GetReviewById getReviewByIdHandler;
    private final GetAllReviews getAllReviewsHandler;
    private final SearchReviews searchReviewsHandler;

    public ReviewGraphQLController(CreateReview createReviewHandler, EditReview editReviewHandler, DeleteReview deleteReviewHandler, GetReviewById getReviewByIdHandler, GetAllReviews getAllReviewsHandler, SearchReviews searchReviewsHandler) {
        this.createReviewHandler = createReviewHandler;
        this.editReviewHandler = editReviewHandler;
        this.deleteReviewHandler = deleteReviewHandler;
        this.getReviewByIdHandler = getReviewByIdHandler;
        this.getAllReviewsHandler = getAllReviewsHandler;
        this.searchReviewsHandler = searchReviewsHandler;
    }

    @QueryMapping
    public Review getReview(@Argument Long id) {
        return getReviewByIdHandler.handle(id);
    }

    @QueryMapping
    public Page<Review> listReviews(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return getAllReviewsHandler.handle(pageable);
    }

    @QueryMapping
    public Page<Review> searchReviews(@Argument String comment, @Argument Integer rating, @Argument String author,
                                      @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (comment != null && !comment.isBlank()) {
            return searchReviewsHandler.searchByComment(comment, pageable);
        } else if (rating != null) {
            return searchReviewsHandler.searchByRating(rating, pageable);
        } else if (author != null && !author.isBlank()) {
            return searchReviewsHandler.searchByAuthor(author, pageable);
        }
        return Page.empty(pageable);
    }

    @MutationMapping
    public Review createReview(@Argument int rating, @Argument String comment, @Argument Long userId, @Argument Long postId) {
        CreateReviewRequest request = new CreateReviewRequest(rating, comment, userId, postId);
        return createReviewHandler.handle(request);
    }

    @MutationMapping
    public Review editReview(@Argument Long id, @Argument int rating, @Argument String comment) {
        EditReviewRequest request = new EditReviewRequest(id, rating, comment);
        return editReviewHandler.handle(request);
    }

    @MutationMapping
    public String deleteReview(@Argument Long id) {
        deleteReviewHandler.handle(id);
        return "Review with ID " + id + " deleted successfully.";
    }
}
