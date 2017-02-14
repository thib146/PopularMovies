package com.example.android.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;

import com.example.android.popularmovies.data.PopularMoviesContract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by thiba on 13/02/2017.
 */

public class FakeDataUtils {

    //private static int [] weatherIDs = {200,300,500,711,900,962};

    /**
     * Creates a single ContentValues object with random weather data for the provided date
     * @param id movie id
     * @return ContentValues object filled with random weather data
     */
    private static ContentValues createTestFavoritesContentValues(long id) {
        ContentValues testFavoritesValues = new ContentValues();
        testFavoritesValues.put(PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID, id);
        testFavoritesValues.put(PopularMoviesContract.MovieEntry.COLUMN_TITLE, "Interstellar");
        testFavoritesValues.put(PopularMoviesContract.MovieEntry.COLUMN_POSTER_PATH, "http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg");
        return testFavoritesValues;
    }

    /**
     * Creates random movie data for 4 movies
     * @param context
     */
    public static void insertFakeData(Context context, ArrayList<String> movieId) {

        List<ContentValues> fakeValues = new ArrayList<ContentValues>();
        movieId = new ArrayList<>();
        //loop over 4 movies
        for(int i=0; i<6; i++) {
            fakeValues.add(createTestFavoritesContentValues(341174));
            movieId.add(i, "341174");
        }
        // Bulk Insert our new weather data into PopularMovies' Database
        context.getContentResolver().bulkInsert(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                fakeValues.toArray(new ContentValues[6]));
    }

    public static void deleteAllData(Context context) {
        context.getContentResolver().delete(PopularMoviesContract.MovieEntry.CONTENT_URI, null, null);
    }
}