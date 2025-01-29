package com.example.demo.util;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    /**
     * Creates a cookie with the given name and value.
     *
     * @param response  the HTTP response to add the cookie
     * @param name      the name of the cookie
     * @param value     the value of the cookie
     * @param maxAge    the maximum age of the cookie in seconds (e.g., 86400 for 1 day)
     * @param httpOnly  if true, the cookie is set as HttpOnly
     */
    public static void createCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge); // Set expiration time
        cookie.setHttpOnly(httpOnly); // Restrict JavaScript access if true
        cookie.setPath("/"); // Make cookie available for the entire application
        response.addCookie(cookie); // Add the cookie to the response
    }

    /**
     * Retrieves the value of a cookie by its name.
     *
     * @param request the HTTP request containing cookies
     * @param name    the name of the cookie to retrieve
     * @return the value of the cookie, or null if not found
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Deletes a cookie by setting its max age to 0.
     *
     * @param response the HTTP response to remove the cookie
     * @param name     the name of the cookie to delete
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0); // Expire the cookie immediately
        cookie.setPath("/"); // Ensure the correct path
        response.addCookie(cookie); // Add the cookie to the response
    }
}
