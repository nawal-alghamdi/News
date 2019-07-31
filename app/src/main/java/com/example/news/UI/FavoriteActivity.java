package com.example.news.UI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.news.R;
import com.example.news.adapters.FavoriteAdapter;
import com.example.news.data.News;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.FavoriteAdapterOnClickHandler {

    private static final String TAG = FavoriteActivity.class.getSimpleName();
    private static final String DATABASE_REFERENCE_PATH = "Favorite News";
    private static final String DATABASE_NEWS_CHILD = "news";
    private static final String RECYCLER_VIEW_STATE = "recyclerViewState";
    @BindView(R.id.favorite_loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.favorite_error_message_textView)
    TextView errorMessageTextView;
    @BindView(R.id.favorite_toolbar)
    Toolbar toolbar;
    private FavoriteAdapter favoriteAdapter;
    private RecyclerView recyclerView;
    private List<News> favoriteNews;
    private Parcelable recyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favoriteNews = new ArrayList<>();

        recyclerView = findViewById(R.id.favorite_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // To add a divider lines to RecyclerView items
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        favoriteAdapter = new FavoriteAdapter(this, this);
        recyclerView.setAdapter(favoriteAdapter);


        if (isConnected()) {
            // read data from database
            readDataFromDatabase();
        } else {
            // show "Please connect to the internet" message
            loadingIndicator.setVisibility(View.INVISIBLE);
            errorMessageTextView.setText(R.string.please_connect_to_the_internet);
            showErrorMessage();
        }

        if (savedInstanceState != null) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        }

    }

    private boolean isConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean connected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return connected;
    }


    private void readDataFromDatabase() {
        // Read from the database
        FirebaseDatabase.getInstance().getReference(DATABASE_REFERENCE_PATH).child(DATABASE_NEWS_CHILD).addValueEventListener(new ValueEventListener() {

            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    // Add News objects from the realtime database to favoriteNews list
                    favoriteNews.add(dataSnapshotChild.getValue(News.class));
                }

                loadingIndicator.setVisibility(View.INVISIBLE);
                favoriteAdapter.setFavoriteNewsData(favoriteNews);
                // To restore recycler view position after rotation
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                showNewsDataView();

                if (favoriteNews.isEmpty()) {
                    // Show text message that the data is empty
                    errorMessageTextView.setText(getString(R.string.favorite_empty_text));
                    showErrorMessage();
                }

            }

            // Failed to read value
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }


    @Override
    public void onClickNewsHolder(News news, View view) {

        // Convert the String URL into a URI object (to pass into the Intent constructor)
        Uri newsUri = Uri.parse(news.getUrl());

        // Create a new intent to view the news URI
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

        // Send the intent to launch a new activity
        startActivity(websiteIntent);

    }

    /**
     * This method will make the View for the news data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showNewsDataView() {
        /* First, make sure the error is invisible */
        errorMessageTextView.setVisibility(View.INVISIBLE);
        /* Then, make sure the news data is visible */
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the news
     * View.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        recyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // To save the state of recyclerView position
        outState.putParcelable(RECYCLER_VIEW_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
    }
}
