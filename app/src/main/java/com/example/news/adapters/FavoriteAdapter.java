package com.example.news.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private FavoriteAdapterOnClickHandler clickHandler;
    private Context context;
    private List<News> favoriteNews;

    public FavoriteAdapter(Context context, FavoriteAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.favorite_list_item, viewGroup, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int i) {
        News currentNews = favoriteNews.get(i);
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
        if (favoriteNews == null) return 0;
        return favoriteNews.size();
    }

    public void setFavoriteNewsData(List<News> favoriteNews) {
        this.favoriteNews = favoriteNews;
        notifyDataSetChanged();
    }

    public void clear() {
        final int size = getItemCount();
        this.favoriteNews.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface FavoriteAdapterOnClickHandler {
        void onClickNewsHolder(News news, View view);
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.favoriteNews_imageView)
        ImageView newsImageView;
        @BindView(R.id.favorite_section_textView)
        TextView sectionTextView;
        @BindView(R.id.favorite_title_textView)
        TextView titleTextView;
        @BindView(R.id.favorite_authors_textView)
        TextView authorsTextView;
        @BindView(R.id.favorite_date_textView)
        TextView dateTextView;
        @BindView(R.id.favoriteNewsHolder)
        ConstraintLayout constraintLayout;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    News currentNews = favoriteNews.get(getAdapterPosition());
                    clickHandler.onClickNewsHolder(currentNews, view);
                }
            });
        }
    }
}
