package com.example.android.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by thiba on 20/01/2017.
 */

public class TheMovieDBJsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullMovieDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param movieJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String[] getMovieInfoFromJson(Context context, String movieJsonStr)
            throws JSONException {

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";

        /* All temperatures are children of the "temp" object */
        //final String OWM_TEMPERATURE = "temp";

        final String TMDB_POSTER_PATH = "poster_path";

        final String TMDB_OVERVIEW = "overview";

        final String TMDB_RELEASE_DATE = "release_date";

        final String TMDB_ID = "id";

        final String TMDB_TITLE = "title";

        final String TMDB_ORG_TITLE = "original_title";

        final String TMDB_POPULARITY = "popularity";

        final String TMDB_VOTE_COUNT = "vote_count";

        final String TMDB_VOTE_AVERAGE = "vote_average";


        final String TMDB_STATUS_CODE = "status_code";

        /* String array to hold each movie String */
        String[] parsedMovieData = null;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMDB_STATUS_CODE)) {
            int errorCode = movieJson.getInt(TMDB_STATUS_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

        parsedMovieData = new String[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {

            String title;
            String releaseDate;
            String moviePoster;
            double voteAverage;
            String description;
            int id;

            /* Get the JSON object representing one movie */
            JSONObject oneMovie = movieArray.getJSONObject(i);

            title = oneMovie.getString(TMDB_TITLE);
            releaseDate = oneMovie.getString(TMDB_RELEASE_DATE);
            moviePoster = oneMovie.getString(TMDB_POSTER_PATH);
            voteAverage = oneMovie.getDouble(TMDB_VOTE_AVERAGE);
            description = oneMovie.getString(TMDB_OVERVIEW);
            id = oneMovie.getInt(TMDB_ID);

            /**
             * TODO : change this to get the array with the movie info (for the detailed view)
             */
            parsedMovieData[i] = title;
        }

        return parsedMovieData;
    }

    public static String[] getMoviePosterFromJson(Context context, String movieJsonStr)
            throws JSONException {

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";

        final String TMDB_POSTER_PATH = "poster_path";

        final String TMDB_STATUS_CODE = "status_code";

        /* String array to hold each movie poster String */
        String[] parsedMovieData = null;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMDB_STATUS_CODE)) {
            int errorCode = movieJson.getInt(TMDB_STATUS_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

        parsedMovieData = new String[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {

            String moviePoster;

            /* Get the JSON object representing one movie */
            JSONObject oneMovie = movieArray.getJSONObject(i);

            moviePoster = oneMovie.getString(TMDB_POSTER_PATH);

            parsedMovieData[i] = moviePoster;
        }

        return parsedMovieData;
    }

    /**
     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
     *
     * @param context         An application context, such as a service or activity context.
     * @param movieJsonStr    The JSON to parse into ContentValues.
     *
     * @return An array of ContentValues parsed from the JSON.
     */
    public static ContentValues[] getFullMovieDataFromJson(Context context, String movieJsonStr) {
        /** This will be implemented in a future lesson **/
        return null;
    }
    
}
