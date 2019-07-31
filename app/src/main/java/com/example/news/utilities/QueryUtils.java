package com.example.news.utilities;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.news.R;
import com.example.news.data.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news data from The Guardian.
 */
public final class QueryUtils {


    private static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * Keys for the json response
     */
    private static final String RESPONSE = "response";
    private static final String RESULTS = "results";
    private static final String NEWS_ID = "id";
    private static final String SECTION = "sectionName";
    private static final String DATE = "webPublicationDate";
    private static final String TITLE = "webTitle";
    private static final String NEWS_URL = "webUrl";
    private static final String FIELDS = "fields";
    private static final String THUMBNAIL = "thumbnail";
    private static final String TAGS = "tags";
    private static final String AUTHOR_NAME = "webTitle";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }


    /**
     * Query the Guardian dataset and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(String requestUrl, Context context) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link News}
        List<News> news = extractFeatureFromJson(jsonResponse, context);

        // Return the list of {@link News}
        return news;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractFeatureFromJson(String newsJSON, Context context) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // This is used to conjunct between authors if there is more than one author.
        String conjunction = context.getString(R.string.comma) + " ";

        Log.i(LOG_TAG, "authors" + conjunction);

        // Create an empty ArrayList that we can start adding news to
        List<News> news = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONObject associated with the key called "response"
            JSONObject responseJsonObject = baseJsonResponse.optJSONObject(RESPONSE);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of results (or news).
            JSONArray newsArray = responseJsonObject.optJSONArray(RESULTS);

            // For each news in the newsArray, create an {@link News} object
            for (int i = 0; i < newsArray.length(); i++) {

                // Get a single news at position i within the list of news
                JSONObject currentNews = newsArray.optJSONObject(i);

                // Extract the value for the key called "id"
                String newsId = currentNews.optString(NEWS_ID);

                // Extract the value for the key called "sectionName"
                String section = currentNews.optString(SECTION);

                // Extract the value for the key called "webPublicationDate"
                String date = currentNews.optString(DATE);

                // Extract the value for the key called "webTitle"
                String title = currentNews.optString(TITLE);

                // Extract the value for the key called "webUrl"
                String url = currentNews.optString(NEWS_URL);

                // Extract the value for the object called "fields" so from this object we can obtain the thumbnail
                JSONObject fields = currentNews.optJSONObject(FIELDS);

                // Extract the value for the key called "thumbnail"
                String thumbnail = fields.optString(THUMBNAIL);

                // For a given news, extract the JSONArray associated with the
                // key called "tags" and its types is contributor, which represents an array of the
                // authors for that news and their information
                String authors = "";
                JSONArray tagsArray = currentNews.optJSONArray(TAGS);

                // Extract the authors names from the tagsArray
                for (int j = 0; j < tagsArray.length(); j++) {
                    // Get a single author at position j within the list of authors
                    JSONObject currentAuthor = tagsArray.optJSONObject(j);

                    // Extract the value for the key called "webTitle"
                    String singleAuthor = currentAuthor.optString(AUTHOR_NAME);

                    // If there is more than one author then add "and" between the previous author and the new one
                    if (j > 0) {
                        authors = authors + conjunction + singleAuthor;
                    } else {
                        authors = singleAuthor;
                    }

                }

                // Create a new {@link News} object with the title, section, date, authors,
                // url, thumbnail and newsId from the JSON response.
                News newsObject = new News(title, section, date, authors, url, thumbnail, newsId);

                // Add the new {@link News} to the list of news.
                news.add(newsObject);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        // Return the list of news
        return news;
    }

}
