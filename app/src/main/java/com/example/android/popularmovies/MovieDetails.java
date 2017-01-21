package com.example.android.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;

import java.net.URL;

/**
 * Created by thiba on 21/01/2017.
 */

public class MovieDetails extends AppCompatActivity {

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private LinearLayout mDetailLayout;

    private TextView mMovieTitle;

    // TODO : get the real movie id here using putExtra
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        /**
         * Management of menu buttons
         */
        // BACK BUTTON
        final ImageView back = (ImageView) findViewById(R.id.iv_back_movie_details);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display_detail);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator_detail);

        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title_details);

        /* Once all of our views are setup, we can load the weather data. */
        loadMovieDetailData();
    }

    private void loadMovieDetailData() {
        showMovieDataView();

        //new FetchMovieDetailTask().execute(id);
    }

    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mDetailLayout.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class Movie {
        public String posterPath;
        public String description;
        public String releaseDate;
        public String id;
        public String title;
        public String originalTitle;
        public String popularity;
        public String voteCount;
        public String voteAverage;
    }

    public class FetchMovieDetailTask extends AsyncTask<String, Void, Movie> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        protected Movie doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            URL movieRequestUrl = NetworkUtils.buildUrlDetail(id);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                String[] JsonMoviePosters = TheMovieDBJsonUtils
                        .getMoviePosterFromJson(MovieDetails.this, jsonMovieResponse);

                TheMovieDBJsonUtils.Movie JsonMovieData = TheMovieDBJsonUtils
                        .getMovieTitleFromJson(MovieDetails.this, jsonMovieResponse);

                Movie movie = new Movie();

                //movie.posterPath = JsonMoviePosters;
                movie.title = JsonMovieData.title[0];
                //movie.description = JsonMovieData.description;
                //movie.id = JsonMovieData.id;
                //movie.originalTitle = JsonMovieData.originalTitle;
                //movie.popularity = JsonMovieData.popularity;
                //movie.releaseDate = JsonMovieData.releaseDate;
                //movie.voteAverage = JsonMovieData.voteAverage;
                //movie.voteCount = JsonMovieData.voteCount;

                return movie;
                //return JsonMoviePosters;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();
                mMovieTitle.setText(movieData.title);
            } else {
                showErrorMessage();
            }
        }
    }
}