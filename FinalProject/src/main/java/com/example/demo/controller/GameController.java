package com.example.demo.controller;

import com.example.demo.model.VideoGame;
import com.example.demo.model.GamesPlayed;
import com.example.demo.service.GameDataService;
import com.example.demo.service.ReviewService;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class GameController {

    @Autowired
    private GameDataService gameDataService;

    @Autowired
    private ReviewService reviewService;

    /**
     * Handles requests for individual game details.
     *
     * @param gameId  the ID of the game
     * @param model   Spring model to pass data to the view
     * @param request the HTTP request to retrieve cookies
     * @return the name of the Thymeleaf template (game-details.html)
     */
    @GetMapping("/game/{gameId}")
    public String gameDetails(@PathVariable Long gameId, Model model, HttpServletRequest request) {
        VideoGame videoGame = gameDataService.getGameById(gameId);
        if (videoGame == null) {
            return "error/404"; // Redirect to a 404 error page if game is not found
        }

        // Retrieve userId from cookies
        String userIdValue = CookieUtil.getCookieValue(request, "userId");
        boolean isGuest = userIdValue == null || userIdValue.equals("-1");

        if (!isGuest) {
            try {
                Long userId = Long.parseLong(userIdValue);

                // Load existing review for this user and game
                Optional<GamesPlayed> existingReview = reviewService.getReview(userId, gameId);
                existingReview.ifPresent(review -> model.addAttribute("userReview", review));
            } catch (NumberFormatException e) {
                isGuest = true; // Treat invalid userId as guest
            }
        }

        model.addAttribute("isGuest", isGuest);

        // Combine reviews from the database and the game model
        double dbReviewTotal = reviewService.getTotalReviewScore(gameId);
        int dbReviewCount = reviewService.getReviewCount(gameId);

        double combinedReviewValue = (videoGame.getRating() * videoGame.getReviewCount() + dbReviewTotal) /
                (videoGame.getReviewCount() + dbReviewCount);
        combinedReviewValue = Math.round(combinedReviewValue * 100.0) / 100.0;
        int combinedReviewCount = videoGame.getReviewCount() + dbReviewCount;

        videoGame.setRating(combinedReviewValue);
        videoGame.setReviewCount(combinedReviewCount);

        model.addAttribute("videoGame", videoGame);
        return "game-details"; // Render the game-details.html page
    }

    /**
     * Handles submission of game reviews by users.
     *
     * @param request  the HTTP request containing cookies
     * @param response the HTTP response
     * @param review   the review object submitted by the user
     * @return a redirect to the game details page
     */
    @PostMapping("/game/review")
    public String submitReview(HttpServletRequest request, HttpServletResponse response, GamesPlayed review) {
        String userIdValue = CookieUtil.getCookieValue(request, "userId");
        if (userIdValue == null || userIdValue.equals("-1")) {
            return "redirect:/auth/login"; // Redirect to login if not signed in
        }

        try {
            Long userId = Long.parseLong(userIdValue);
            review.setUserId(userId);
            boolean success = reviewService.saveReview(review);
            if (success) {
                return "redirect:/game/" + review.getGameId() + "?reviewStatus=success";
            } else {
                return "redirect:/game/" + review.getGameId() + "?reviewStatus=error";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "redirect:/game/" + review.getGameId() + "?reviewStatus=error";
        }
    }
}
