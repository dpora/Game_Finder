package com.example.demo.service;

import com.example.demo.model.VideoGame;
import com.example.demo.util.ResultSetMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameDataService {

    private static final String CLIENT_ID = "hehe";
    private static final String ACCESS_TOKEN = "haha";
    private static final String API_URL = "https://api.igdb.com/v4/games";
    private static final String GENRES_URL = "https://api.igdb.com/v4/genres";
    private static final String PLATFORMS_URL = "https://api.igdb.com/v4/platforms";

    /**
     * Fetches games from the IGDB API with pagination.
     *
     * @param limit  number of games to fetch
     * @param offset offset for pagination
     * @return a list of VideoGame objects
     */
    public List<VideoGame> getGames(int limit, int offset) {
        try {
            String query = String.format(
                    "fields id, name, genres.name, platforms.name, rating, summary, cover.url, first_release_date, " +
                            "involved_companies.company.name, involved_companies.publisher, total_rating_count; " +
                            "limit %d; offset %d;",
                    limit, offset
            );
            return fetchGamesFromApi(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches games from the IGDB API with a search query.
     *
     * @param query  the search term
     * @param limit  number of games to fetch
     * @param offset offset for pagination
     * @return a list of VideoGame objects
     */
    public List<VideoGame> searchGames(String query, int limit, int offset) {
        try {
            String searchQuery = String.format(
                    "fields id, name, genres.name, platforms.name, rating, summary, cover.url, first_release_date, " +
                            "involved_companies.company.name, involved_companies.publisher, total_rating_count; " +
                            "search \"%s\"; limit %d; offset %d;",
                    query, limit, offset
            );

            return fetchGamesFromApi(searchQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches games from the IGDB API with filters and sorting.
     *
     * @param limit          number of games to fetch
     * @param offset         offset for pagination
     * @param filterGenre    genre filter
     * @param filterPlatform platform filter
     * @param filterRating   rating filter
     * @param sortBy         sorting field
     * @param sortOrder      sorting order (asc/desc)
     * @return a list of VideoGame objects
     */
    public List<VideoGame> getGamesWithFilters(int limit, int offset, String filterGenre, String filterPlatform, String filterRating, String sortBy, String sortOrder) {
        try {
            StringBuilder queryBuilder = new StringBuilder(
                    "fields id, name, genres.name, platforms.name, rating, summary, cover.url, first_release_date, " +
                            "involved_companies.company.name, involved_companies.publisher, total_rating_count; " +
                            "limit " + limit + "; offset " + offset + ";"
            );

            if (filterGenre != null && !filterGenre.isEmpty()) {
                queryBuilder.append(" where genres.name = \"").append(filterGenre).append("\";");
            }
            if (filterPlatform != null && !filterPlatform.isEmpty()) {
                queryBuilder.append(" where platforms.name = \"").append(filterPlatform).append("\";");
            }
            if (filterRating != null && !filterRating.isEmpty()) {
                queryBuilder.append(" where rating >= ").append(filterRating).append(";");
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                queryBuilder.append(" sort ").append(sortBy).append(" ").append(sortOrder).append(";");
            }

            return fetchGamesFromApi(queryBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches a game by its ID from the IGDB API.
     *
     * @param id the ID of the game
     * @return a VideoGame object
     */
    public VideoGame getGameById(Long id) {
        try {
            String query = String.format(
                "fields id, name, genres.name, platforms.name, rating, summary, cover.url, first_release_date, " +
                "involved_companies.company.name, involved_companies.publisher, total_rating_count; " +
                "where id = %d;", id
            );
            List<VideoGame> games = fetchGamesFromApi(query);
            return games.isEmpty() ? null : games.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null on error
    }

    /**
     * Fetches genres from the IGDB API.
     *
     * @return a list of genres
     */
    public List<String> getGenres() {
        try {
            return fetchFromApi(GENRES_URL, "fields name;");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches platforms from the IGDB API.
     *
     * @return a list of platforms
     */
    public List<String> getPlatforms() {
        try {
            return fetchFromApi(PLATFORMS_URL, "fields name;");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches data from the IGDB API based on the provided URL and query.
     *
     * @param apiUrl the IGDB API URL
     * @param query  the query string
     * @return a list of strings
     */
    private List<String> fetchFromApi(String apiUrl, String query) {
        try {
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Client-ID", CLIENT_ID);
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "text/plain"); // IGDB expects the query to be sent as plain text
            conn.setDoOutput(true);

            conn.getOutputStream().write(query.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray results = new JSONArray(response.toString());
            return results.toList().stream()
                    .map(obj -> (HashMap<?, ?>) obj)
                    .map(map -> (String) map.get("name"))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }

    /**
     * Fetches games from the IGDB API based on the provided query string.
     *
     * @param query the formatted IGDB API query string
     * @return a list of VideoGame objects
     */
    private List<VideoGame> fetchGamesFromApi(String query) {
        try {
            URL url = new URL(API_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Client-ID", CLIENT_ID);
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "text/plain"); // IGDB expects the query to be sent as plain text
            conn.setDoOutput(true);

            conn.getOutputStream().write(query.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray results = new JSONArray(response.toString());
            return ResultSetMapper.mapApiResponseToVideoGames(results);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(); // Return empty list on error
    }
}
