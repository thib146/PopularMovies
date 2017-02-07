package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.utilities.MovieArrays;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmovies.utilities.NetworkUtils.isNetworkAvailable;

/**
 * Created by thiba on 21/01/2017.
 */

public class MovieDetails extends AppCompatActivity {

    private static final String TAG = MovieDetails.class.getSimpleName();

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private ConstraintLayout mDetailLayout;

    private TextView mMovieTitle;
    private TextView mReleaseDate;
    private TextView mMovieDescription;
    private TextView mMovieRatings;
    private ImageView mMoviePoster;

    private String mPosterVersion = "w185";

    private String id = "";

    private static boolean mConnected = true;

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
                finish();
            }
        });

        mDetailLayout = (ConstraintLayout) findViewById(R.id.ll_detail_layout);

        Intent intentThatStartedThatActivity = getIntent();

        id = intentThatStartedThatActivity.getStringExtra(Intent.EXTRA_TEXT);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display_detail);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator_detail);

        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title_details);
        mReleaseDate = (TextView) findViewById(R.id.tv_date_details);
        mMovieDescription = (TextView) findViewById(R.id.tv_description_details);
        mMovieRatings = (TextView) findViewById(R.id.tv_ratings_details);
        mMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster_detail);

        /* Once all of our views are setup, we can load the weather data. */
        loadMovieDetailData();
    }

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

    private void loadMovieDetailData() {
        showMovieDataView();

        new FetchMovieDetailTask().execute(id);
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
        if (!mConnected) {
            mErrorMessageDisplay.setText(R.string.error_message_internet);
        } else {
            mErrorMessageDisplay.setText(R.string.error_message_common);
        }
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

            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            URL movieRequestUrl = NetworkUtils.buildUrlDetail(id);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                //Movie JsonMovieData = TheMovieDBJsonUtils
                //.getMovieDataFromJson(MovieDetails.this, jsonMovieResponse, mPosterVersion);

                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MovieDetails.this, jsonMovieResponse, mPosterVersion);

                //Movie movie = new Movie();
                MovieArrays movie = new MovieArrays();
                movie.posterPath = new ArrayList<>();
                movie.title = new ArrayList<>();
                movie.description = new ArrayList<>();
                movie.originalTitle = new ArrayList<>();
                movie.releaseDate = new ArrayList<>();
                movie.voteAverage = new ArrayList<>();

//                movie.posterPath = JsonMovieData.posterPath[0];
//                movie.title = JsonMovieData.originalTitle[0];
//                movie.description = JsonMovieData.description[0];
//                //movie.id = JsonMovieData.id[0];
//                movie.originalTitle = JsonMovieData.originalTitle[0];
//                //movie.popularity = JsonMovieData.popularity[0];
//                movie.releaseDate = JsonMovieData.releaseDate[0];
//                movie.voteAverage = JsonMovieData.voteAverage[0];
//                //movie.voteCount = JsonMovieData.voteCount[0];

                movie.posterPath = JsonMovieData.posterPath;
                movie.title = JsonMovieData.originalTitle;
                movie.description = JsonMovieData.description;
                //movie.id = JsonMovieData.id;
                movie.originalTitle = JsonMovieData.originalTitle;
                //movie.popularity = JsonMovieData.popularity;
                movie.releaseDate = JsonMovieData.releaseDate;
                movie.voteAverage = JsonMovieData.voteAverage;
                //movie.voteCount = JsonMovieData.voteCount;

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

                //Picasso.with(context).load(movieData.posterPath).into(mMoviePoster);
                Picasso.with(context).load(movieData.posterPath.get(0)).into(mMoviePoster);

//                mMovieTitle.setText(movieData.title);
//                mReleaseDate.setText(String.format(resources.getString(R.string.movie_release_date), movieData.releaseDate));
//                mMovieDescription.setText(String.format(resources.getString(R.string.movie_description), movieData.description));
//                mMovieRatings.setText(String.format(resources.getString(R.string.movie_ratings), movieData.voteAverage));

                mMovieTitle.setText(movieData.title.get(0));
                mReleaseDate.setText(String.format(resources.getString(R.string.movie_release_date), movieData.releaseDate.get(0)));
                mMovieDescription.setText(String.format(resources.getString(R.string.movie_description), movieData.description.get(0)));
                mMovieRatings.setText(String.format(resources.getString(R.string.movie_ratings), movieData.voteAverage.get(0)));
            } else {
                showErrorMessage();
            }
        }
    }
}