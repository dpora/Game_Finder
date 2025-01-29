package com.example.demo.controller;

import com.example.demo.model.VideoGame;
import com.example.demo.service.GameDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private GameDataService gameDataService;

    /**
     * Handles the /search endpoint and renders the search.html page.
     * Initially fetches 10 games from the IGDB API.
     *
     * @param model Spring model to pass data to the view
     * @return the name of the Thymeleaf template (search.html)
     */
    @GetMapping("/search")
    public String searchPage(Model model) {
        // Fetch the first 10 games (limit=10, offset=0)
        List<VideoGame> videoGames = gameDataService.getGames(10, 0);

        // Add the video game list to the model
        model.addAttribute("videoGames", videoGames);

        return "search"; // Render the search.html page
    }

    /**
     * Handles requests for paginated game data from the IGDB API.
     * This method returns JSON data for the frontend to handle dynamically.
     *
     * @param limit          the number of games to fetch
     * @param offset         the starting point for fetching games
     * @param query          optional search term for filtering games
     * @param filterGenre    optional genre filter
     * @param filterDeveloper optional developer filter
     * @param filterPlatform optional platform filter
     * @param filterRating   optional rating filter
     * @param sortBy         optional sorting field
     * @param sortOrder      optional sorting order (asc/desc)
     * @return ResponseEntity containing a list of VideoGame objects
     */
    @GetMapping("/api/games")
    public ResponseEntity<List<VideoGame>> getGames(@RequestParam(defaultValue = "10") int limit,
                                                    @RequestParam(defaultValue = "0") int offset,
                                                    @RequestParam(required = false) String query,
                                                    @RequestParam(required = false) String filterGenre,
                                                    @RequestParam(required = false) String filterPlatform,
                                                    @RequestParam(required = false) String filterRating,
                                                    @RequestParam(defaultValue = "name") String sortBy,
                                                    @RequestParam(defaultValue = "asc") String sortOrder) {
        // Define cache control headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        List<VideoGame> games;

        if (query != null && !query.isEmpty()) {
            // Perform search if query is provided
            games = gameDataService.searchGames(query, limit, offset);
        } else {
            // Fetch games with filters and sorting
            games = gameDataService.getGamesWithFilters(limit, offset, filterGenre,  filterPlatform, filterRating, sortBy, sortOrder);
        }

        // Return the response with headers
        return ResponseEntity.ok().headers(headers).body(games);
    }

    /**
     * Handles requests for genres from the IGDB API.
     *
     * @return ResponseEntity containing a list of genres
     */
    @GetMapping("/api/genres")
    public ResponseEntity<List<String>> getGenres() {
        List<String> genres = gameDataService.getGenres();
        return ResponseEntity.ok().body(genres);
    }

    /**
     * Handles requests for platforms from the IGDB API.
     *
     * @return ResponseEntity containing a list of platforms
     */
    @GetMapping("/api/platforms")
    public ResponseEntity<List<String>> getPlatforms() {
        List<String> platforms = gameDataService.getPlatforms();
        return ResponseEntity.ok().body(platforms);
    }
}
