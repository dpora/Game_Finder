package com.example.demo.util;

import com.example.demo.model.VideoGame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultSetMapper {

    /**
     * Maps the IGDB API response to a list of VideoGame objects.
     *
     * @param results the JSONArray from the API response
     * @return a list of VideoGame objects
     */
    public static List<VideoGame> mapApiResponseToVideoGames(JSONArray results) {
        List<VideoGame> videoGames = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject game = results.getJSONObject(i);
            VideoGame videoGame = new VideoGame();

            videoGame.setGameId(game.optLong("id"));
            videoGame.setGameName(game.optString("name", "Unknown Game"));

            // Extract all genres as a comma-separated string
            videoGame.setGenre(joinJSONArray(game.optJSONArray("genres"), "name"));

            // Extract all platforms as a comma-separated string
            videoGame.setPlatform(joinJSONArray(game.optJSONArray("platforms"), "name"));

            // Extract all involved companies as a comma-separated string
            videoGame.setInvolvedCompanies(joinJSONArray(game.optJSONArray("involved_companies"), "company.name"));

            // Round rating to 2 decimal places
            double rawRating = game.optDouble("rating", 0.0);
            double roundedRating = Math.round(rawRating * 100.0) / 100.0;
            videoGame.setRating(roundedRating);

            videoGame.setReviewCount(game.optInt("total_rating_count", 0));

            videoGame.setMaturityRating("Unknown"); // Placeholder for ESRB ratings
            videoGame.setDescription(game.optString("summary", "No description available."));

            videoGame.setImageUrl(
                    game.optJSONObject("cover") != null
                            ? game.optJSONObject("cover").optString("url", "No Image")
                            : "No Image"
            );

            videoGame.setDeveloper(game.optJSONArray("involved_companies") != null ?
                    getCompanyByRole(game.getJSONArray("involved_companies"), "developer") : "Unknown");

            videoGame.setPublisher(game.optJSONArray("involved_companies") != null ?
                    getCompanyByRole(game.getJSONArray("involved_companies"), "publisher") : "Unknown");

            videoGame.setReleaseDate(game.has("first_release_date") ?
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date(game.getLong("first_release_date") * 1000)) : "Unknown");

            videoGames.add(videoGame);
        }

        return videoGames;
    }

    /**
     * Helper method to extract company name by role (e.g., developer or publisher).
     *
     * @param companies JSONArray of involved companies
     * @param role      The role to look for (developer or publisher)
     * @return the name of the company with the given role
     */
    private static String getCompanyByRole(JSONArray companies, String role) {
        for (int i = 0; i < companies.length(); i++) {
            JSONObject company = companies.getJSONObject(i);
            if (company.optBoolean(role, false)) {
                return company.getJSONObject("company").optString("name", "Unknown");
            }
        }
        return "Unknown";
    }

    /**
     * Helper method to join the names of elements in a JSONArray into a comma-separated string.
     *
     * @param jsonArray The JSONArray to process
     * @param key       The key whose values will be joined
     * @return A comma-separated string of values or "Unknown" if the array is null or empty
     */
    private static String joinJSONArray(JSONArray jsonArray, String key) {
        if (jsonArray == null) {
            return "Unknown";
        }
        List<String> values = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String[] keys = key.split("\\.");
            JSONObject current = obj;
            for (int j = 0; j < keys.length - 1; j++) {
                current = current.optJSONObject(keys[j]);
                if (current == null) {
                    break;
                }
            }
            if (current != null) {
                values.add(current.optString(keys[keys.length - 1], "Unknown"));
            }
        }
        return values.isEmpty() ? "Unknown" : String.join(", ", values);
    }
}
