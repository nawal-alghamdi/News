package com.example.news.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.news.R;
import com.example.news.data.News;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String DATABASE_REFERENCE_PATH = "Favorite News";
    private static final String DATABASE_NEWS_CHILD = "news";
    private Context context;
    private List<News> favoriteNews;
    private CountDownLatch countDownLatch;


    public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
        context = applicationContext;
        favoriteNews = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
        // To see how CountDownLatch works go to this documentation it's so simple
        // https://developer.android.com/reference/java/util/concurrent/CountDownLatch
        countDownLatch = new CountDownLatch(1);
        if (isConnected()) {
            // read data from database
            readDataFromDatabase();
        }
        try {
            // wait for all to finish
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(context.getPackageName(), "trying onDataSet ");
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        Log.d(context.getPackageName(), "trying getCount  ");
        if (favoriteNews == null) return 0;
        return favoriteNews.size();
    }

    /**
     * This method acts like the onBindViewHolder method in an Adapter
     *
     * @param position The current position of the item in the ListView to be displayed
     * @return The RemoteViews object to display for the provided position
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (favoriteNews.isEmpty()) return null;
        Log.d(context.getPackageName(), "trying getViewAt  ");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_news_widget);

        News currentNews = favoriteNews.get(position);

        views.setTextViewText(R.id.newsSection_textView, currentNews.getSection());
        views.setTextViewText(R.id.newsTitle_textView, currentNews.getTitle());
        views.setTextViewText(R.id.newsAuthors_textView, currentNews.getAuthors());

        // The date in the json response is come in this form  2019-07-12T17:46:06Z  and we will
        // take the fist part before the litter T as shown below
        String newsDate = currentNews.getDate();
        String[] parts = newsDate.split("T");
        views.setTextViewText(R.id.newsDate_textView, parts[0]);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private boolean isConnected() {
        Log.d(context.getPackageName(), "trying isConnected()  ");

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean connected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return connected;
    }


    private void readDataFromDatabase() {
        Log.d(context.getPackageName(), "trying readDataFromDatabase()  ");

        // Clear the list so the favorite news doesn't duplicate
        favoriteNews.clear();
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
                // let all threads proceed
                countDownLatch.countDown();
                Log.d(context.getPackageName(), "trying readDataFromDatabase() last ");
            }

            // Failed to read value
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(context.getPackageName(), "Failed to read value.", databaseError.toException());
            }
        });
    }

}

