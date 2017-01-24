package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;
import com.example.android.popularmovies.widget.SegmentedButton;

import java.net.URL;

import static com.example.android.popularmovies.utilities.NetworkUtils.isNetworkAvailable;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private String sortQuery = "popular";

    private String[] mMovieId;

    private String mPosterPortraitVersion = "w500";
    private String mPosterLandscapeVersion = "w342";

    private static boolean mConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /**
         * Creation of the Segmented Buttons using Widget/SegmentedButton
         */
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.segmented);
        buttons.clearButtons();
        buttons.addButtons("Most Popular", "Top Rated");
        // First button is selected
        buttons.setPushedButtonIndex(0);
        // Some example click handlers. Note the click won't get executed
        // if the segmented button is already selected (dark blue)
        buttons.setOnClickListener(new SegmentedButton.OnClickListenerSegmentedButton() {
            @Override
            public void onClick(int index) {
                if (index == 0) {
                    sortQuery = "popular";
                    mMovieAdapter.setMovieData(null);
                    loadMovieData();
                    //Toast.makeText(MainActivity.this, "Sort by Most Popular", Toast.LENGTH_SHORT).show();
                } else {
                    sortQuery = "top_rated";
                    mMovieAdapter.setMovieData(null);
                    loadMovieData();
                    //Toast.makeText(MainActivity.this, "Sort by Top Rated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Management of menu buttons
         */
        // REFRESH BUTTON
        ImageView reload = (ImageView) findViewById(R.id.iv_refresh_menu);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieAdapter.setMovieData(null);
                loadMovieData();
            }
        });
        // SETTINGS BUTTON
        final ImageView settings = (ImageView) findViewById(R.id.iv_settings_menu);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToStartSettingsActivity = new Intent(MainActivity.this, MainSettings.class);
                startActivity(intentToStartSettingsActivity);
            }
        });

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
//        LinearLayoutManager layoutManager
//                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);


        //GridLayoutManager layoutManager
        //        = new GridLayoutManager(this, GridLayoutManager.DEFAULT_SPAN_COUNT, GridLayoutManager.VERTICAL, false);

        GridLayoutManager layoutManager
                = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*x
         * The MovieAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         */
        mMovieAdapter = new MovieAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMovieAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the weather data. */
        loadMovieData();
    }

    /**
     * This method will get sort query from the user, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData() {
        showMovieDataView();

        new FetchMovieTask().execute(sortQuery);
    }

    /**
     * This method will check the internet connection
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    static Handler connectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 1) { // If not connected
                mConnected = false;
            } else { // If connected
                mConnected = true;
            }
        }
    };

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param movieID The view that was clicked
     */
    @Override
    public void onClick(String movieID) {
        int adapterPosition = mMovieAdapter.adapterPosition;
        String movieId = mMovieId[adapterPosition];

        Intent intentToStartMovieDetailActivity = new Intent(this, MovieDetails.class);

        intentToStartMovieDetailActivity.putExtra(Intent.EXTRA_TEXT, movieId);

        if (mConnected) {
            startActivity(intentToStartMovieDetailActivity);
        } else {
            showErrorMessage();
        }
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Chose which error message to display */
        if (!mConnected) {
            mErrorMessageDisplay.setText(R.string.error_message_internet);
        } else {
            mErrorMessageDisplay.setText(R.string.error_message_common);
        }
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class Movie {
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

    public class FetchMovieTask extends AsyncTask<String, Void, Movie> {

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

            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            URL movieRequestUrl = NetworkUtils.buildUrl(sortQuery);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                String mPosterVersion;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPosterVersion = mPosterLandscapeVersion;
                } else {
                    mPosterVersion = mPosterPortraitVersion;
                }

                TheMovieDBJsonUtils.Movie JsonMovieData = TheMovieDBJsonUtils
                            .getMovieTitleFromJson(MainActivity.this, jsonMovieResponse, mPosterVersion);

                Movie movie = new Movie();

                movie.posterPath = JsonMovieData.posterPath;
                movie.title = JsonMovieData.title;
                movie.id = JsonMovieData.id;

                mMovieId = movie.id;

                return movie;

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
                mMovieAdapter.setMovieData(movieData);
            } else {
                showErrorMessage();
            }
        }
    }
}