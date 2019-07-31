package com.example.news.data;

/**
 * An {@link News} object contains information related to a single news.
 */
public class News {

    /**
     * Title of the news
     */
    private String title;

    /**
     * Section of the news
     */
    private String section;

    /**
     * Time of the news
     */
    private String date;

    /**
     * Authors of the news
     */
    private String authors;

    /**
     * Website URL of the news
     */
    private String url;

    /**
     * Image of the news
     */
    private String thumbnail;

    /**
     * Id of the news
     */
    private String newsId;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public News() {
    }

    /**
     * Constructs a new {@link News} object.
     *
     * @param title     is the title of the news
     * @param section   is the section of the news
     * @param date      is the date
     * @param authors   are the authors of the news
     * @param url       is the website URL to find more details about the news
     * @param thumbnail is the thumbnail of the news
     * @param newsId    is the news id
     */
    public News(String title, String section, String date, String authors, String url, String thumbnail, String newsId) {
        this.title = title;
        this.section = section;
        this.date = date;
        this.authors = authors;
        this.url = url;
        this.thumbnail = thumbnail;
        this.newsId = newsId;
    }

    /**
     * Returns the title of the news.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the section of the news.
     */
    public String getSection() {
        return section;
    }

    /**
     * Returns the date of the news.
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the authors of the news.
     */
    public String getAuthors() {
        return authors;
    }

    /**
     * Returns the website URL to find more information about the news.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the thumbnail of the news.
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Returns the news id.
     */
    public String getNewsId() {
        return newsId;
    }
}

