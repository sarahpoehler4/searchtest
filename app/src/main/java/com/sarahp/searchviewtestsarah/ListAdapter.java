package com.sarahp.searchviewtestsarah;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private List<GitHubRepo> mGitHubRepoList;

    public ListAdapter(List<GitHubRepo> gitHubRepoList) {
        mGitHubRepoList = gitHubRepoList;
    }

    @Override
    public ListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        GitHubRepo gitHubRepoList = mGitHubRepoList.get(position);

        Glide.with(holder.civAvatar.getContext())
                .load(gitHubRepoList.getOwner().getAvatarUrl())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(holder.civAvatar);

        holder.tvPlaceholderOne.setText(String.format(Locale.getDefault(), "%d", gitHubRepoList.getId()));
        holder.tvPlaceholderTwo.setText(gitHubRepoList.getName());
    }

    @Override
    public int getItemCount() {
        return mGitHubRepoList.size();
    }

    public void setFilter(List<GitHubRepo> list) {
        mGitHubRepoList = new ArrayList<>();
        mGitHubRepoList.addAll(list);
        notifyDataSetChanged();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_avatar) ImageView civAvatar;
        @BindView(R.id.tv_placeholder_one) TextView tvPlaceholderOne;
        @BindView(R.id.tv_placeholder_two) TextView tvPlaceholderTwo;
        final View view;

        ListViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }
    }
}
