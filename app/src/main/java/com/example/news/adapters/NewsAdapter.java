package com.example.news.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.news.R;
import com.example.news.data.News;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private static final String TAG = NewsAdapter.class.getSimpleName();

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private NewsAdapterOnClickHandler clickHandler;
    private Context context;
    private List<News> news;

    public NewsAdapter(Context context, NewsAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }


    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.list_item, viewGroup, false);
        NewsViewHolder viewHolder = new NewsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int i) {
        News currentNews = news.get(i);
        if (!currentNews.getThumbnail().isEmpty()) {
            Picasso.get().load(currentNews.getThumbnail()).into(holder.newsImageView);
        }
        holder.sectionTextView.setText(currentNews.getSection());
        holder.titleTextView.setText(currentNews.getTitle());
        holder.authorsTextView.setText(currentNews.getAuthors());

        // The date in the json response is come in this form  2019-07-12T17:46:06Z  and we will
        // take the fist part before the litter T as shown below
        String newsDate = currentNews.getDate();
        String[] parts = newsDate.split("T");
        holder.dateTextView.setText(parts[0]);
    }

    @Override
    public int getItemCount() {
        if (news == null) return 0;
        return news.size();
    }

    public void setNewsData(List<News> news) {
        this.news = news;
        notifyDataSetChanged();
    }

    public void clear() {
        final int size = getItemCount();
        this.news.clear();
        notifyItemRangeRemoved(0, size);
    }


    /**
     * The interface that receives onClick messages.
     */
    public interface NewsAdapterOnClickHandler {

        void onClickFavorite(News news, View view);

        void onClickNewsHolder(News news, View view);
    }


    public class NewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.news_imageView)
        ImageView newsImageView;
        @BindView(R.id.section_textView)
        TextView sectionTextView;
        @BindView(R.id.title_textView)
        TextView titleTextView;
        @BindView(R.id.authors_textView)
        TextView authorsTextView;
        @BindView(R.id.date_textView)
        TextView dateTextView;
        @BindView(R.id.favoriteActionButton)
        FloatingActionButton favoriteButton;
        @BindView(R.id.newsHolder)
        ConstraintLayout constraintLayout;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    News currentNews = news.get(getAdapterPosition());
                    clickHandler.onClickNewsHolder(currentNews, view);
                }
            });

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    News currentNews = news.get(position);
                    clickHandler.onClickFavorite(currentNews, view);
                }
            });

        }

    }
}
