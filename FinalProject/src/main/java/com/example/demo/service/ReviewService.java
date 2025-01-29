package com.example.demo.service;

import com.example.demo.model.GamesPlayed;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@Service
public class ReviewService {

    private static final String DB_URL = "jdbc:sqlite:FinalProject/src/main/resources/db/video_games.db";

    public boolean saveReview(GamesPlayed review) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO GamesPlayed (userId, gameId, ratingScore, reviewComment) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(userId, gameId) DO UPDATE SET ratingScore = ?, reviewComment = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, review.getUserId());
            stmt.setLong(2, review.getGameId());
            stmt.setDouble(3, review.getRatingScore());
            stmt.setString(4, review.getReviewComment());
            stmt.setDouble(5, review.getRatingScore());
            stmt.setString(6, review.getReviewComment());
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<GamesPlayed> getReview(Long userId, Long gameId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM GamesPlayed WHERE userId = ? AND gameId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, userId);
            stmt.setLong(2, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GamesPlayed review = new GamesPlayed();
                review.setUserId(rs.getLong("userId"));
                review.setGameId(rs.getLong("gameId"));
                review.setRatingScore(rs.getDouble("ratingScore"));
                review.setReviewComment(rs.getString("reviewComment"));
                return Optional.of(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    public double getTotalReviewScore(Long gameId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT SUM(ratingScore) as total FROM GamesPlayed WHERE gameId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getReviewCount(Long gameId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT COUNT(*) as count FROM GamesPlayed WHERE gameId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
