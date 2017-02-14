package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.PopularMoviesContract;
import com.example.android.popularmovies.utilities.MovieArrays;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmovies.utilities.NetworkUtils.isNetworkAvailable;

/**
 * Created by thib146 on 21/01/2017.
 */

public class MovieDetails extends AppCompatActivity {

    private static final String TAG = MovieDetails.class.getSimpleName();

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private ConstraintLayout mDetailLayout;

    // Global movie variable containing the movie data to be used for the Favorites list
    private MovieArrays mMovie;

    // Boolean to know if the movie is already saved in the Favorites
    private boolean isFaved = true;

    private Cursor mCursor;

    // Declare the UI objects
    private TextView mMovieTitle;
    private TextView mMovieOriginalTitle;
    private TextView mReleaseDate;
    private TextView mMovieDescription;
    private TextView mMovieRatings;
    private ImageView mMoviePoster;
    private ImageView mFavoritesButton;
    private ImageView mFavedButton;

    // Only this size of poster will be used
    private String mPosterVersion = "w185";

    private String id = "";

    private static boolean mConnected = true;

    public static final String[] DETAIL_FAVORITES_PROJECTION = {
            PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            PopularMoviesContract.MovieEntry.COLUMN_TITLE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mFavoritesButton = (ImageView) findViewById(R.id.favorite_button_default);
        mFavedButton = (ImageView) findViewById(R.id.favorite_button_clicked);

        // Get the intent that started this Detailed View
        Intent intentThatStartedThatActivity = getIntent();

        // Get the ID that was passed though the intent
        id = intentThatStartedThatActivity.getStringExtra(Intent.EXTRA_TEXT);

        // Check if the movie is already a Favorite one by searching in the database
        String[] mSelectionArgs = {""};
        mSelectionArgs[0] = id;
        mCursor = MovieDetails.this.getContentResolver().query(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                DETAIL_FAVORITES_PROJECTION,
                PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                mSelectionArgs,
                null);
        if (null == mCursor) {
            Log.e(TAG, "The isFaved research provoked a prob");
        } else if (mCursor.getCount() < 1) {
            isFaved = false;
            showFavoritesButton();
        } else {
            isFaved = true;
            showFavedButton();
        }

        /**
         * Management of menu buttons
         */
        // BACK BUTTON
        final ImageView back = (ImageView) findViewById(R.id.iv_back_movie_details);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // FAVORITE BUTTON
        mFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues favValues = new ContentValues();

                favValues.put(PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID, id);
                favValues.put(PopularMoviesContract.MovieEntry.COLUMN_TITLE, mMovie.title.get(0));
                favValues.put(PopularMoviesContract.MovieEntry.COLUMN_POSTER_PATH, mMovie.posterPath.get(0));

                ArrayList<ContentValues> favValuesContent = new ArrayList<ContentValues>();
                favValuesContent.add(favValues);

                MovieDetails.this.getContentResolver().bulkInsert(
                        PopularMoviesContract.MovieEntry.CONTENT_URI,
                        favValuesContent.toArray(new ContentValues[1]));

                isFaved = true;
                showFavedButton();
            }
        });
        mFavedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mSelectionArgs = {""};
                mSelectionArgs[0] = id;
                MovieDetails.this.getContentResolver().delete(
                        PopularMoviesContract.MovieEntry.CONTENT_URI,
                        PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        mSelectionArgs);

                isFaved = false;
                showFavoritesButton();
            }
        });


        // Layout reference
        mDetailLayout = (ConstraintLayout) findViewById(R.id.ll_detail_layout);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display_detail);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator_detail);

        // UI references
        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title_details);
        mMovieOriginalTitle = (TextView) findViewById(R.id.tv_movie_original_title_details);
        mReleaseDate = (TextView) findViewById(R.id.tv_date_details);
        mMovieDescription = (TextView) findViewById(R.id.tv_description_details);
        mMovieRatings = (TextView) findViewById(R.id.tv_ratings_details);
        mMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster_detail);

        /* Once all of our views are setup, we can load the weather data. */
        loadMovieDetailData();
    }

    /**
     * This method will change mConnected according to the internet connection
     */
    static Handler connectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 1) { // code if not connected
                mConnected = false;
            } else { // code if connected
                mConnected = true;
            }
        }
    };

    /**
     * This method will tell some background method to get
     * the movie details data in the background, with the id.
     */
    private void loadMovieDetailData() {
        showMovieDataView();

        new FetchMovieDetailTask().execute(id);
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    private void showFavedButton() {
        // Hide the default Favorites button
        mFavoritesButton.setVisibility(View.INVISIBLE);
        // Then, show the Faved button
        mFavedButton.setVisibility(View.VISIBLE);
    }

    private void showFavoritesButton() {
        // Hide the default Favorites button
        mFavedButton.setVisibility(View.INVISIBLE);
        // Then, show the Faved button
        mFavoritesButton.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie
     * View.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mDetailLayout.setVisibility(View.INVISIBLE);
        if (!mConnected) {
            mErrorMessageDisplay.setText(R.string.error_message_internet);
        } else {
            mErrorMessageDisplay.setText(R.string.error_message_common);
        }
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * This method will change the ReleaseDate variable to display it properly
     */
    private String correctReleaseDate(String date) {
        String[] releaseDateSep = date.split("-");
        switch (releaseDateSep[1]) {
            case "01":
                releaseDateSep[1] = getString(R.string.january);
                break;
            case "02":
                releaseDateSep[1] = getString(R.string.february);
                break;
            case "03":
                releaseDateSep[1] = getString(R.string.march);
                break;
            case "04":
                releaseDateSep[1] = getString(R.string.april);
                break;
            case "05":
                releaseDateSep[1] = getString(R.string.may);
                break;
            case "06":
                releaseDateSep[1] = getString(R.string.june);
                break;
            case "07":
                releaseDateSep[1] = getString(R.string.july);
                break;
            case "08":
                releaseDateSep[1] = getString(R.string.august);
                break;
            case "09":
                releaseDateSep[1] = getString(R.string.september);
                break;
            case "10":
                releaseDateSep[1] = getString(R.string.october);
                break;
            case "11":
                releaseDateSep[1] = getString(R.string.november);
                break;
            case "12":
                releaseDateSep[1] = getString(R.string.december);
                break;
        }
        return releaseDateSep[2] + " " + releaseDateSep[1] + " " + releaseDateSep[0];
    }

    // This method will load the movie details in the background and send them to the Adapter
    public class FetchMovieDetailTask extends AsyncTask<String, Void, MovieArrays> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        protected MovieArrays doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            // If there's no internet connexion, stop
            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            // Create the URL with the current movie ID
            URL movieRequestUrl = NetworkUtils.buildUrlDetail(id);

            try {
                // Get the full HTTP response
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                // Read the movie details from the Json file
                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MovieDetails.this, jsonMovieResponse, mPosterVersion);

                // Instantiate all the variables that we need
                MovieArrays movie = new MovieArrays();

                movie.posterPath = new ArrayList<>();
                movie.title = new ArrayList<>();
                movie.description = new ArrayList<>();
                movie.originalTitle = new ArrayList<>();
                movie.releaseDate = new ArrayList<>();
                movie.voteAverage = new ArrayList<>();

                // Copy the data from the Json to our movie variable
                movie.posterPath = JsonMovieData.posterPath;
                movie.title = JsonMovieData.title;
                movie.description = JsonMovieData.description;
                movie.originalTitle = JsonMovieData.originalTitle;
                movie.releaseDate = JsonMovieData.releaseDate;
                movie.voteAverage = JsonMovieData.voteAverage;

                // Store the movie data in a global variable so that we can use it for the Favorites list
                mMovie = movie;

                // Return the movie variable for it to be used in onPostExecute
                return movie;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieArrays movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();

                Resources resources = getResources();
                Context context = mMoviePoster.getContext();

                // Display the movie poster
                Picasso.with(context).load(movieData.posterPath.get(0)).into(mMoviePoster);

                // Display all the movie info
                mMovieTitle.setText(movieData.title.get(0));
                mMovieOriginalTitle.setText(String.format(resources.getString(R.string.movie_original_title), movieData.originalTitle.get(0)));
                mReleaseDate.setText(String.format(resources.getString(R.string.movie_release_date), correctReleaseDate(movieData.releaseDate.get(0))));
                mMovieDescription.setText(String.format(resources.getString(R.string.movie_description), movieData.description.get(0)));
                mMovieRatings.setText(String.format(resources.getString(R.string.movie_ratings), movieData.voteAverage.get(0)));
            } else {
                showErrorMessage();
            }
        }
    }
}