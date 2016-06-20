package io.github.ashwinwadte.popularmovies.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import io.github.ashwinwadte.popularmovies.BuildConfig;
import io.github.ashwinwadte.popularmovies.DetailActivity;
import io.github.ashwinwadte.popularmovies.R;
import io.github.ashwinwadte.popularmovies.adapters.PosterAdapter;
import io.github.ashwinwadte.popularmovies.asynctasks.FetchMoviesTask;
import io.github.ashwinwadte.popularmovies.interfaces.TheMovieDbApi;
import io.github.ashwinwadte.popularmovies.models.Movie;
import io.github.ashwinwadte.popularmovies.models.Movies;
import io.github.ashwinwadte.popularmovies.utils.Constants;
import io.github.ashwinwadte.popularmovies.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    SharedPreferences pref;

    private ProgressBar mProgressBar;
    private PosterAdapter mPosterAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        GridView mGridView = (GridView) rootView.findViewById(R.id.gv_movie_posters);

        mPosterAdapter = new PosterAdapter(getContext(), new ArrayList<Movie>());
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_movies_list);

        mGridView.setEmptyView(rootView.findViewById(android.R.id.empty));
        mGridView.setAdapter(mPosterAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mPosterAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Constants.EXTRA_MOVIE, movie);

                startActivity(intent);
            }
        });

        pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateRetrofit();
    }

    private void updateRetrofit() {

        String sortOrder = pref.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_default));

        String title;
        String[] titleArray = getResources().getStringArray(R.array.pref_sort_by_titles);
        String[] sortOrderArray = getResources().getStringArray(R.array.pref_sort_by_values);


        TheMovieDbApi movieDbApi = Utils.getTheMovieDbApiInstance();

        //user chose favorite sort order. Show favorite movies of the user
        if (sortOrder.equals(sortOrderArray[2])) {
            title = titleArray[2];
            FetchMoviesTask task = new FetchMoviesTask(getContext(), mPosterAdapter, mProgressBar);
            task.execute();
        } else {

            mProgressBar.setVisibility(View.VISIBLE);
            mPosterAdapter.clear();

            if (sortOrder.equals(sortOrderArray[0]))
                title = titleArray[0];
            else
                title = titleArray[1];

            //fetch movies for respective order
            Call<Movies> call = movieDbApi.getMovies(sortOrder, BuildConfig.THE_MOVIE_DB_API_KEY);

            call.enqueue(new Callback<Movies>() {
                @Override
                public void onResponse(Call<Movies> call, Response<Movies> response) {
                    mPosterAdapter.clear();
                    Movies movies = response.body();

                    for (Movies.Result r :
                            movies.getResults()) {
                        Movie movie = new Movie(String.valueOf(r.getId()), r.getTitle(), r.getPosterPath(), r.getOverview(), String.valueOf(r.getVoteAverage()), r.getReleaseDate());
                        mPosterAdapter.add(movie);
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<Movies> call, Throwable t) {
                    mProgressBar.setVisibility(View.GONE);
                }
            });


        }

        getActivity().setTitle(title + " Movies");

    }
}
