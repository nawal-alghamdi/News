package com.example.news.UI;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.news.R;
import com.example.news.adapters.NewsAdapter;
import com.example.news.data.News;
import com.example.news.loaders.NewsLoader;
import com.example.news.widgets.FavoriteNewsWidget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>,
        NewsAdapter.NewsAdapterOnClickHandler {

    private static final String
            REQUEST_URL = "https://content.guardianapis.com/search?show-tags=contributor&show-fields=thumbnail,short-url&api-key=";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NEWS_LOADER_ID = 0;
    private static final String DATABASE_REFERENCE_PATH = "Favorite News";
    private static final String DATABASE_NEWS_CHILD = "news";
    private static final String RECYCLER_VIEW_STATE = "recyclerViewState";
    private static FirebaseDatabase database;
    private static DatabaseReference databaseReference;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.error_message_textView)
    TextView errorMessageTextView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private AdView mAdView;
    private Parcelable recyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(DATABASE_REFERENCE_PATH).child(DATABASE_NEWS_CHILD);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // To add a divider lines to RecyclerView items
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        newsAdapter = new NewsAdapter(this, this);
        recyclerView.setAdapter(newsAdapter);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        if (isConnected()) {
            LoaderManager.getInstance(this).initLoader(NEWS_LOADER_ID, null, MainActivity.this);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            showErrorMessage();
        }

        if (savedInstanceState != null) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        }

    }

    @NonNull
    @Override
    public Loader<List<News>> onCreateLoader(int i, @Nullable Bundle bundle) {
        loadingIndicator.setVisibility(View.VISIBLE);
        String urlWithApiKey = REQUEST_URL + getString(R.string.api_key);
        Log.i(TAG, " Api Key" + urlWithApiKey);
        return new NewsLoader(this, urlWithApiKey);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<News>> loader, List<News> news) {
        loadingIndicator.setVisibility(View.INVISIBLE);
        newsAdapter.setNewsData(news);
        // To restore recycler view position after rotation
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        if (news == null) {
            showErrorMessage();
        } else {
            showNewsDataView();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<News>> loader) {
        newsAdapter.clear();
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

    @Override
    // This method initialize the contents of the Activity's options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    // This method is called whenever an item in the options menu is selected.
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    protected void onStart() {
        super.onStart();
        LoaderManager.getInstance(this).restartLoader(NEWS_LOADER_ID, null, MainActivity.this);

    }

    @Override
    public void onClickFavorite(News news, View view) {
        if (isConnected()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                boolean newsExist = false;

                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        //If news exists then toast message is shown
                        if (data.getValue(News.class).getNewsId().equals(news.getNewsId())) {
                            newsExist = true;
                            Toast.makeText(MainActivity.this, getString(R.string.news_already_exists), Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (!newsExist) {
                        databaseReference.push().setValue(news);
                        Toast.makeText(MainActivity.this, getString(R.string.news_saved_to_favorite), Toast.LENGTH_SHORT).show();

                        // Update app widgets if any news added to favorite
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MainActivity.this, FavoriteNewsWidget.class));
                        //Trigger data update to handle the ListView widgets and force a data refresh
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_widget);

                    }

                }


                @Override
                public void onCancelled(final DatabaseError databaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                }
            });

        } else {
            Toast.makeText(this, getString(R.string.please_connect_to_the_internet), Toast.LENGTH_SHORT).show();
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // To save the state of recyclerView position
        outState.putParcelable(RECYCLER_VIEW_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
    }


}
