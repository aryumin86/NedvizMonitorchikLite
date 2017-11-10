package ru.aryumin.nedvizmonitorchiklite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class FavouritePosts extends AppCompatActivity {

    DbHelper dbHelper = null;
    SharedPreferences prefs = null;
    static PostsListFilter filter;
    public static ArrayList<ThePost> posts = new ArrayList<>();
    ListItemAdapter listItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_posts);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(dbHelper == null)
            dbHelper = new DbHelper(FavouritePosts.this);

        posts = dbHelper.getPostsFromLocalDb();
        ArrayList<ThePost> temp = new ArrayList<>();
        for(ThePost p : posts){
            if(p.isFavourite())
                temp.add(p);
        }

        posts = temp;
        ListView postsListView = (ListView)findViewById(R.id.favoiritePostsListView);
        listItemAdapter = new ListItemAdapter(FavouritePosts.this, posts);
        postsListView.setAdapter(listItemAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int postIdWithChangedFavouriteStatus = PostInfo.postId;
        for(ThePost p : posts){
            if(p.getId() == postIdWithChangedFavouriteStatus){
                if(resultCode == 1)
                    p.setFavourite(true);
                else
                    p.setFavourite(false);

                break;
            }
        }

        //поменяли избранность у поста в активити избранных. А теперь надо сделать это для спсика
        //постов для активити всех собранных постов
        if(CollectedPosts.posts != null){
            for(ThePost p : CollectedPosts.posts){
                if(p.getId() == postIdWithChangedFavouriteStatus){
                    if(resultCode == 1)
                        p.setFavourite(true);
                    else
                        p.setFavourite(false);

                    break;
                }
            }
        }


    }
}
