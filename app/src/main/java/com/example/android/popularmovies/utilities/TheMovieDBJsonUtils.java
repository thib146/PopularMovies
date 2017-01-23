package com.example.android.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.example.android.popularmovies.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

    public static class Movie {
        public String[] posterPath;
        public String[] description;
        public String[] releaseDate;
        public String[] id;
        public String[] title;
        public String[] originalTitle;
        public String[] popularity;
        public String[] voteCount;
        public String[] voteAverage;
    }

    public static Movie getMovieTitleFromJson(Context context, String movieJsonStr, String posterVersion)
            throws JSONException {

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";

        final String TMDB_POSTER_PATH = "poster_path";

        final String TMDB_OVERVIEW = "overview";

        final String TMDB_RELEASE_DATE = "release_date";

        final String TMDB_ID = "id";

        final String TMDB_TITLE = "title";

        final String TMDB_ORG_TITLE = "original_title";

        final String TMDB_POPULARITY = "popularity";

        final String TMDB_VOTE_COUNT = "vote_count";

        final String TMDB_VOTE_AVERAGE = "vote_average";

        final String TMDB_BASE_URL_POSTER = "http://image.tmdb.org/t/p/";

        //final String TMDB_POSTER_SIZE = posterVersion;

        final String TMDB_STATUS_CODE = "status_code";

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

        //JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

        URL[] urlPosterPath = null;

        JSONArray movieArray = new JSONArray();

        Movie parsedMovieData = new Movie();

        if (movieJson.has(TMDB_LIST)) {
            movieArray = movieJson.getJSONArray(TMDB_LIST);

            urlPosterPath = new URL[movieArray.length()];

            String[] movieTitle = new String[movieArray.length()];
            String[] posterPath = new String[movieArray.length()];
            String[] description = new String[movieArray.length()];
            String[] releaseDate = new String[movieArray.length()];
            String[] id = new String[movieArray.length()];
            String[] originalTitle = new String[movieArray.length()];
            String[] popularity = new String[movieArray.length()];
            String[] voteCount = new String[movieArray.length()];
            String[] voteAverage = new String[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {

                /* Get the JSON object representing one movie */
                JSONObject oneMovie = movieArray.getJSONObject(i);

                movieTitle[i] = oneMovie.getString(TMDB_TITLE);
                posterPath[i] = oneMovie.getString(TMDB_POSTER_PATH);
                description[i] = oneMovie.getString(TMDB_OVERVIEW);
                releaseDate[i] = oneMovie.getString(TMDB_RELEASE_DATE);
                id[i] = oneMovie.getString(TMDB_ID);
                originalTitle[i] = oneMovie.getString(TMDB_ORG_TITLE);
                popularity[i] = oneMovie.getString(TMDB_POPULARITY);
                voteCount[i] = oneMovie.getString(TMDB_VOTE_COUNT);
                voteAverage[i] = oneMovie.getString(TMDB_VOTE_AVERAGE);

                /**
                 * Remove the first letter from the moviePoser string : the character "/" which is not useful
                 */
                posterPath[i] = posterPath[i].substring(1);

                Uri builtUri = Uri.parse(TMDB_BASE_URL_POSTER).buildUpon()
                        .appendPath(posterVersion)
                        .appendPath(posterPath[i])
                        .build();

                try {
                    urlPosterPath[i] = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                posterPath[i] = urlPosterPath[i].toString();
            }

            parsedMovieData.description = description;
            parsedMovieData.id = id;
            parsedMovieData.originalTitle = originalTitle;
            parsedMovieData.popularity = popularity;
            parsedMovieData.posterPath = posterPath;
            parsedMovieData.releaseDate = releaseDate;
            parsedMovieData.title = movieTitle;
            parsedMovieData.voteAverage = voteAverage;
            parsedMovieData.voteCount = voteCount;

        } else {
            String movieTitle = movieJson.getString(TMDB_TITLE);
            String posterPath = movieJson.getString(TMDB_POSTER_PATH);
            String description = movieJson.getString(TMDB_OVERVIEW);
            String releaseDate = movieJson.getString(TMDB_RELEASE_DATE);
            String id = movieJson.getString(TMDB_ID);
            String originalTitle = movieJson.getString(TMDB_ORG_TITLE);
            String popularity = movieJson.getString(TMDB_POPULARITY);
            String voteCount = movieJson.getString(TMDB_VOTE_COUNT);
            String voteAverage = movieJson.getString(TMDB_VOTE_AVERAGE);

            /**
             * Remove the first letter from the moviePoser string : the character "/" which is not useful
             */
            posterPath = posterPath.substring(1);

            urlPosterPath = new URL[movieJson.length()];

            Uri builtUri = Uri.parse(TMDB_BASE_URL_POSTER).buildUpon()
                    .appendPath(posterVersion)
                    .appendPath(posterPath)
                    .build();

            try {
                urlPosterPath[0] = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            posterPath = urlPosterPath[0].toString();

            parsedMovieData.description = new String[movieJson.length()];
            parsedMovieData.id = new String[movieJson.length()];
            parsedMovieData.originalTitle = new String[movieJson.length()];
            parsedMovieData.popularity = new String[movieJson.length()];
            parsedMovieData.posterPath = new String[movieJson.length()];
            parsedMovieData.releaseDate = new String[movieJson.length()];
            parsedMovieData.title = new String[movieJson.length()];
            parsedMovieData.voteAverage = new String[movieJson.length()];
            parsedMovieData.voteCount = new String[movieJson.length()];

            parsedMovieData.description[0] = description;
            parsedMovieData.id[0] = id;
            parsedMovieData.originalTitle[0] = originalTitle;
            parsedMovieData.popularity[0] = popularity;
            parsedMovieData.posterPath[0] = posterPath;
            parsedMovieData.releaseDate[0] = releaseDate;
            parsedMovieData.title[0] = movieTitle;
            parsedMovieData.voteAverage[0] = voteAverage;
            parsedMovieData.voteCount[0] = voteCount;
        }

        return parsedMovieData;
    }

    public static String[] getMoviePosterFromJson(Context context, String movieJsonStr)
            throws JSONException {

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";

        final String TMDB_POSTER_PATH = "poster_path";

        final String TMDB_STATUS_CODE = "status_code";

        final String TMDB_BASE_URL_POSTER = "http://image.tmdb.org/t/p/";

        final String TMDB_POSTER_SIZE = "w500";

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

        JSONArray movieArray = new JSONArray();

        if (movieJson.has(TMDB_LIST)) {
            movieArray = movieJson.getJSONArray(TMDB_LIST);
        }

        //JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

        parsedMovieData = new String[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {

            String moviePoster;
            URL url = null;

            /* Get the JSON object representing one movie */
            JSONObject oneMovie = movieArray.getJSONObject(i);

            moviePoster = oneMovie.getString(TMDB_POSTER_PATH);

            /**
             * Remove the first letter from the moviePoser string : the character "/" which is not useful
             */
            moviePoster = moviePoster.substring(1);

            Uri builtUri = Uri.parse(TMDB_BASE_URL_POSTER).buildUpon()
                    .appendPath(TMDB_POSTER_SIZE)
                    .appendPath(moviePoster)
                    .build();

            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            parsedMovieData[i] = url.toString();
        }

        return parsedMovieData;
    }

    public static String[] getMovieInfosFromJson(Context context, String movieJsonStr)
            throws JSONException {

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";
        final String TMDB_MOVIE_TITLE = "title";
        final String TMDB_STATUS_CODE = "status_code";

        /* String array to hold each movie title String */
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

            String movieTitle;
            URL url = null;

            /* Get the JSON object representing one movie */
            JSONObject oneMovie = movieArray.getJSONObject(i);

            movieTitle = oneMovie.getString(TMDB_MOVIE_TITLE);

            parsedMovieData[i] = movieTitle;
        }

        return parsedMovieData;
    }
}
