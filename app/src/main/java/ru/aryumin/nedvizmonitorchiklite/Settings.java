package ru.aryumin.nedvizmonitorchiklite;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences prefs;
    String THE_LOG = "LOGGY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        prefs = getPreferenceScreen().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        Preference clearDbButtonPref = findPreference("clear_db_pref");
        clearDbButtonPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setMessage("Стереть все собранные посты?")
                        .setTitle("Подтвердите");

                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DbHelper dbHelper = new DbHelper(Settings.this);
                        dbHelper.truncatePostsTable();
                        FavouritePosts.posts = new ArrayList<ThePost>();
                        CollectedPosts.posts = new ArrayList<ThePost>();
                        Toast.makeText(Settings.this,"Все посты удалены", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Нет", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        Preference setDefaultSettingsButtonPref = findPreference("set_default_vals_pref");
        setDefaultSettingsButtonPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setMessage("Вернуть начальные настройки приложения и очистить базу данных?")
                        .setTitle("Подтвердите");

                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DbHelper dbHelper = new DbHelper(Settings.this);
                        dbHelper.truncatePostsTable();
                        PostsListFilter postsListFilter = PostsListFilter.getInstance(Settings.this);
                        postsListFilter.dropPostsListFilter();

                        FavouritePosts.posts = new ArrayList<ThePost>();
                        CollectedPosts.posts = new ArrayList<ThePost>();

                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
                        PreferenceManager.setDefaultValues(Settings.this, R.xml.prefs, true);
                        postsListFilter = PostsListFilter.getInstance(Settings.this);

                        Toast.makeText(Settings.this,"Все посты удалены. настройки возвращены к начальным", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                builder.setNegativeButton("Нет", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        PostsListFilter filter = MainActivity.postsListFilter;

        if(key.equals("SHOW_FROM_REALTORS")){
            filter.setShowPostsFromRieltors(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты от риелторов: " + key);
        }

        else if(key.equals("SHOW_LET_POSTS")){
            filter.setShowLetPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о LET: " + key);
        }

        else if(key.equals("SHOW_RENT_POSTS")){
            filter.setShowRentPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о RENT: " + key);
        }

        else if(key.equals("SHOW_SELL_POSTS")){
            filter.setShowSellPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о SELL: " + key);
        }

        else if(key.equals("SHOW_BUY_POSTS")){
            filter.setShowBuyPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о BUY: " + key);
        }

        else if(key.equals("SHOW_LIVINGS")){
            filter.setShowLivingPropertyPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о LIVINGS: " + key);
        }

        else if(key.equals("SHOW_NON_LIVINGS")){
            filter.setShowNONLivingPropertyPosts(sharedPreferences.getBoolean(key,true));
            Log.d(THE_LOG, "Показывать посты о NON_LIVING: " + key);
        }

        else if(key.equals("num_posts_to_show")){
            filter.setNumPostsToShow(Integer.parseInt(sharedPreferences.getString(key,"666")));
            Log.d(THE_LOG, "Показывать постов вот сколько: " + key);
        }

        else if(key.equals("notify_freq")){
            filter.setApiCallsFrequency(Integer.parseInt(sharedPreferences.getString(key,"30")));
            Log.d(THE_LOG, "Частота обращения к API: " + key);
        }

        //если это целочислоенное значение - это id города
        else if(key.matches("^\\d+$")){
            boolean checked = sharedPreferences.getBoolean(key, true);
            if(!filter.getCitiesIds().contains(Integer.parseInt(key)) && checked){
                filter.getCitiesIds().add(Integer.parseInt(key));
                Log.d(THE_LOG, "Добавлен город " + key);
            }

            else {
                filter.getCitiesIds().remove(filter.getCitiesIds().indexOf(Integer.parseInt(key)));
                Log.d(THE_LOG, "удален город " + key);
            }

        }
    }
}
