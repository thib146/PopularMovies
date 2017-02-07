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
import com.example.android.popularmovies.utilities.Movie;
import com.example.android.popularmovies.utilities.MovieArrays;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;
import com.example.android.popularmovies.widget.SegmentedButton;

import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmovies.utilities.NetworkUtils.isNetworkAvailable;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private String sortQuery = "popular";

    //private String[] mMovieId;
    private ArrayList<String> mMovieId;
    //private Movie[] mMovie;
    private MovieArrays[] mMovie;

    private String mPosterPortraitVersion = "w500";
    private String mPosterLandscapeVersion = "w342";
    private int numColumns = 2;

    private String mCurrentPageNumber = "1"; // Initialization to 1
    private String mTotalPageNumber;

    private static boolean mConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Creation of the Segmented Buttons using Widget/SegmentedButton
         */
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.segmented);
        buttons.clearButtons();
        buttons.addButtons(getString(R.string.popular_button), getString(R.string.ratings_button));
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
                    mCurrentPageNumber = "1";
                    loadMovieData();
                } else {
                    sortQuery = "top_rated";
                    mMovieAdapter.setMovieData(null);
                    mCurrentPageNumber = "1";
                    loadMovieData();
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
                //mMovieAdapter.setMovieData(null);
                //mCurrentPageNumber = "1";
                //loadMovieData();
                loadMoreMovieData();
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
         * GridLayoutManager declaration for our movie posters. Limited to 2 columns
         */
        GridLayoutManager layoutManager
                = new GridLayoutManager(this, numColumns, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

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

    private void loadMoreMovieData() {
        showMovieDataView();

        new FetchMoreMovieTask().execute(sortQuery);
    }

//    private void loadNextPageMovieData() {
//        mMovieAdapter = new MovieAdapter(this);
//
//        /* Setting the adapter attaches it to the RecyclerView in our layout. */
//        mRecyclerView.setAdapter(mMovieAdapter);
//
//        showMovieDataView();
//
//        new FetchMoreMovieTask().execute(sortQuery);
//    }

    /**
     * This method will check the internet connection
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * This method will change mConnected according to the internet connection
     */
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
        //String movieId = mMovieId[adapterPosition];
        String movieId = mMovieId.get(adapterPosition);

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
     */
    private void showMovieDataView() {
        /* Hide the error message */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Make the movie data visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie
     * View.
     */
    private void showErrorMessage() {
        /* Hide the current data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Chose which error message to display */
        if (!mConnected) {
            mErrorMessageDisplay.setText(R.string.error_message_internet);
        } else {
            mErrorMessageDisplay.setText(R.string.error_message_common);
        }
        /* Show the error view */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // This method will get the 20 first movies in the background and send them to the Adapter
    public class FetchMovieTask extends AsyncTask<String, Void, MovieArrays[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        protected MovieArrays[] doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            URL movieRequestUrl = NetworkUtils.buildUrl(sortQuery, mCurrentPageNumber);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                String mPosterVersion;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPosterVersion = mPosterLandscapeVersion;
                } else {
                    mPosterVersion = mPosterPortraitVersion;
                }

                // Read the 20 first movies from the Json file
                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MainActivity.this, jsonMovieResponse, mPosterVersion);

                mTotalPageNumber = JsonMovieData.totalPageNumber;
                int totalPageNumberInt = Integer.valueOf(mTotalPageNumber);

                mMovie = new MovieArrays[totalPageNumberInt];
                for (int i = 0; i < totalPageNumberInt; i++) {
                    mMovie[i] = new MovieArrays();
                    mMovie[i].posterPath = new ArrayList<String>();
                    mMovie[i].title = new ArrayList<String>();
                    mMovie[i].id = new ArrayList<String>();
                    mMovie[i].description = new ArrayList<String>();
                    mMovie[i].releaseDate = new ArrayList<String>();
                    mMovie[i].originalTitle = new ArrayList<String>();
                    mMovie[i].popularity = new ArrayList<String>();
                    mMovie[i].voteCount = new ArrayList<String>();
                    mMovie[i].voteAverage = new ArrayList<String>();
                }

                mMovie[0].posterPath = JsonMovieData.posterPath;
                mMovie[0].title = JsonMovieData.originalTitle;
                mMovie[0].id = JsonMovieData.id;
                mMovie[0].description = JsonMovieData.description;
                mMovie[0].releaseDate = JsonMovieData.releaseDate;
                mMovie[0].originalTitle = JsonMovieData.originalTitle;
                mMovie[0].popularity = JsonMovieData.popularity;
                mMovie[0].voteCount = JsonMovieData.voteCount;
                mMovie[0].voteAverage = JsonMovieData.voteAverage;

                mMovieId = mMovie[0].id;

                mCurrentPageNumber = "2";

                return mMovie;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieArrays[] movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();
                mMovieAdapter.setMovieData(movieData[0]);
            } else {
                showErrorMessage();
            }
        }
    }

    // This method will get 20 more movies in the background and send them to the Adapter
    public class FetchMoreMovieTask extends AsyncTask<String, Void, MovieArrays[]> {

        protected MovieArrays[] doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            // Check the internet connection and stop everything if we lost it
            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            // Set the integer versions of the PageNumbers
            int currentPageNumberInt = Integer.valueOf(mCurrentPageNumber);
            int totalPageNumberInt = Integer.valueOf(mTotalPageNumber);

            // If we reached the last page, then we stop
            if (currentPageNumberInt > totalPageNumberInt) {
                return null;
            }

            // Make the new URL for the next page of data
            URL movieRequestUrl = NetworkUtils.buildUrl(sortQuery, mCurrentPageNumber);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                // Check the orientation of the phone to change the poster sizes accordingly
                String mPosterVersion;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPosterVersion = mPosterLandscapeVersion;
                } else {
                    mPosterVersion = mPosterPortraitVersion;
                }

                // Read 20 new movies from the Json file
                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MainActivity.this, jsonMovieResponse, mPosterVersion);

                // Get all the data from the new page in the Json file
                mMovie[currentPageNumberInt-1].posterPath = JsonMovieData.posterPath;
                mMovie[currentPageNumberInt-1].title = JsonMovieData.originalTitle;
                mMovie[currentPageNumberInt-1].id = JsonMovieData.id;
                mMovie[currentPageNumberInt-1].description = JsonMovieData.description;
                mMovie[currentPageNumberInt-1].releaseDate = JsonMovieData.releaseDate;
                mMovie[currentPageNumberInt-1].originalTitle = JsonMovieData.originalTitle;
                mMovie[currentPageNumberInt-1].popularity = JsonMovieData.popularity;
                mMovie[currentPageNumberInt-1].voteCount = JsonMovieData.voteCount;
                mMovie[currentPageNumberInt-1].voteAverage = JsonMovieData.voteAverage;

                // Add all the new IDs (20) to the mMovieId global variable
                for (int i = 0; i < 20; i++) {
                    mMovieId.add(mMovie[currentPageNumberInt-1].id.get(i));
                }

                // return the new data to use it in onPostExecute
                return mMovie;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieArrays[] movieData) {
            if (movieData != null) {
                int currentPageNumberInt = Integer.valueOf(mCurrentPageNumber);

                // Send the 20 new movies to the Adapter for it to add them to the RecyclerView
                mMovieAdapter.addMovieData(movieData[currentPageNumberInt-1], currentPageNumberInt);

                // Increase the currentPageNumber
                currentPageNumberInt += 1;
                mCurrentPageNumber = String.valueOf(currentPageNumberInt);
            } else {
                showErrorMessage();
            }
        }
    }
}