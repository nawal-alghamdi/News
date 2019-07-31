package com.example.news.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.news.data.News;
import com.example.news.utilities.QueryUtils;

import java.util.List;

/**
 * To define the NewsLoader class, we extend AsyncTaskLoader and specify List as the generic parameter,
 * which explains what type of data is expected to be loaded.
 * In this case, the loader is loading a list of News objects.
 * <p>
 * Loads a list of news by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class NewsLoader extends AsyncTaskLoader<List<News>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = NewsLoader.class.getName();

    /* This List will hold and help cache our news data */
    private List<News> newsData = null;

    /**
     * Query URL
     */
    private String url;


    /**
     * Constructs a new {@link NewsLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public NewsLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    // We override the onStartLoading() method to call forceLoad() which is a required
    // step to actually trigger the loadInBackground() method to execute.
    @Override
    protected void onStartLoading() {
        if (newsData != null) {
            deliverResult(newsData);
        } else {
            forceLoad();
        }
    }

    /**
     * This is the method of the AsyncTaskLoader that will load and parse the JSON data
     * from QueryUtils in the background.
     *
     * @return News data from QueryUtils as a List of News.
     * null if an error occurs
     */
    @Override
    public List<News> loadInBackground() {
        try {
            List<News> jsonNewsResponse = QueryUtils.fetchNewsData(url, getContext());
            return jsonNewsResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends the result of the load to the registered listener.
     *
     * @param data The result of the load
     */
    @Override
    public void deliverResult(List<News> data) {
        newsData = data;
        super.deliverResult(data);
    }
}

