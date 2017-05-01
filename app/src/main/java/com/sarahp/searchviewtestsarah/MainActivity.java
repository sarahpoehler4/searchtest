package com.sarahp.searchviewtestsarah;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view_fragment) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_toolbar_title) TextView tvToolbarTitle;
    private ListAdapter mListAdapter;
    private List<GitHubRepo> mGitHubRepoList;
    private ProgressDialog mProgressDialog;
    private SearchView mSearchView;
    public static final String TAG = "CallInstances";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        swipeRefreshLayout.setOnRefreshListener(this);

        setToolbar();
        setProgressDialog();
        setRecyclerView();
    }

    private void setToolbar() {
        tvToolbarTitle.setText(R.string.placeholder);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public void setProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching repos...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String searchText = newText.toLowerCase();
        List<GitHubRepo> list = new ArrayList<>();
        for (GitHubRepo gitHubRepoList : mGitHubRepoList) {
            String name = String.valueOf(gitHubRepoList.getName()).toLowerCase();
            if (name.contains(searchText)) {
                list.add(gitHubRepoList);
            }
        }
        mListAdapter.setFilter(list);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query, true);
        }
    }

    @Override
    public void onRefresh() {
        if (checkInternetConnection()) {
            Toast.makeText(this, "Repos...", Toast.LENGTH_SHORT).show();
            loadJson();
        } else {
            checkInternetConnection();
            Toast connectionToast = Toast.makeText(this, "No internet", Toast.LENGTH_SHORT);
            connectionToast.show();
            swipeRefreshLayout.setRefreshing(false);
            mProgressDialog.hide();
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setItemViewCacheSize(20);
        loadJson();
    }

    private void loadJson() {
        String user = "JakeWharton";
        GitHubService gitHubService = ServiceGenerator.createService(GitHubService.class);
        Call<List<GitHubRepo>> call = gitHubService.reposForUser(user);
        call.enqueue(new Callback<List<GitHubRepo>>() {
            @Override
            public void onResponse(Call<List<GitHubRepo>> call, Response<List<GitHubRepo>> response) {
                if (response.isSuccessful()) {
                    mGitHubRepoList = response.body();
                    mListAdapter = new ListAdapter(mGitHubRepoList);
                    recyclerView.setAdapter(mListAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                    mProgressDialog.hide();
                } else {
                    int statusCode = response.code();
                    Toast toastStatus = Toast.makeText(getApplicationContext(), "Status code: " + statusCode, Toast.LENGTH_SHORT);
                    toastStatus.show();
                }
            }

            @Override
            public void onFailure(Call<List<GitHubRepo>> call, Throwable t) {
                if (call.isCanceled()) {
                    Log.e(TAG, "request was cancelled");
                } else {
                    Log.e(TAG, "request was cancelled");
                    Toast toastError = Toast.makeText(getApplicationContext(), "Error fetching data!", Toast.LENGTH_SHORT);
                    toastError.show();
                }
                swipeRefreshLayout.setRefreshing(false);
                mProgressDialog.hide();
            }
        });
    }
}
