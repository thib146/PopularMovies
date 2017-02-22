package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements
        MoviesListFragment.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_fragment) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            //MoviesListFragment.mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            //((MoviesListFragment) getSupportFragmentManager().findFragmentById(
            //        R.id.movies_list)).setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link MoviesListFragment.OnItemSelectedListener} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String movieId) {
        //if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString(MovieDetailsFragment.ARG_ITEM_ID, movieId);
//            MovieDetailsFragment fragment = new MovieDetailsFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.movie_detail_fragment, fragment).commit();

        //} else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            //Intent detailIntent = new Intent(this, MovieDetailsFragment.class);
            //detailIntent.putExtra(Intent.EXTRA_TEXT, movieId);
            //startActivity(detailIntent);
        //}
    }
}