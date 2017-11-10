package ru.aryumin.nedvizmonitorchiklite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/***
 * Для экрана со списком всех постов
 */
public class CollectedPosts extends AppCompatActivity {

    DbHelper dbHelper = null;
    SharedPreferences prefs = null;
    static PostsListFilter filter;
    public static ArrayList<ThePost> posts = new ArrayList<>();
    ListItemAdapter listItemAdapter;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_posts);

        //полноэкранная реклама
        mInterstitialAd = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_ad));
        AdRequest adRequestFullScreen = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                //.addTestDevice("B5B083E466C03CBB920F2482192B9E93")  // An example device ID
                .build();
        mInterstitialAd.loadAd(adRequestFullScreen);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });

        filter = PostsListFilter.getInstance(CollectedPosts.this);
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(dbHelper == null)
            dbHelper = new DbHelper(CollectedPosts.this);

        //проверяем, прошло ли как минимум 15 минут с момента последнего обращения к API
        //если прошло - снова делаем запрос и обновляем - ставим текущее время

        if(getMinutesSinceLastApiCall() > 15 || posts.size() == 0){
            getPostsFromAPI();
            SharedPreferences.Editor editor = prefs.edit();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            editor.putString("last_api_call",formatter.format(new Date()));
            editor.commit();
        }
        else {
            //Останавливаем крутяшку, если она крутится
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            ListView postsListView = (ListView)findViewById(R.id.postsListView);
            listItemAdapter = new ListItemAdapter(CollectedPosts.this, posts);
            postsListView.setAdapter(listItemAdapter);
        }

    }

    /***
     * Получение постов из API и запись в локальную БД
     */
    public void getPostsFromAPI() {
        FlatsAPICall flatsAPICall = new FlatsAPICall();
        flatsAPICall.execute();
    }

    /***
     * Класс для асинхронного обращения к АПИ с постами
     */
    class FlatsAPICall extends AsyncTask<Void, Void, Void> {
        String resultXml = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            //запросы к апи
            InputStream in = null;
            HttpURLConnection urlConnection = null;
            ArrayList<String> apiLinks = filter.FormURLsList();
            posts = new ArrayList<>();
            URL url;

            for (String l : apiLinks){
                try {
                    url = new URL(l);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream());
                    resultXml = getStringFromInputStream(in);
                } catch (Exception ex) {
                    Log.e(MainActivity.THE_LOG, ex.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //парсинг json
                posts.addAll(parseJSONFromFlatsAPI(resultXml));
            }

            posts = filter.FilterOldPostsFromNewlyGotList(posts);

            //запись в БД
            for(ThePost post : posts){
                if(dbHelper == null)
                    dbHelper = new DbHelper(CollectedPosts.this);
                dbHelper.insertPostIntoDB(post);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Останавливаем крутяшку
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            //получаем все посты в БД, применяем к ним фильтры и показываем в ListView
            posts = dbHelper.getPostsFromLocalDb();
            posts = filter.FilterPostsList(posts);
            ListView postsListView = (ListView)findViewById(R.id.postsListView);
            listItemAdapter = new ListItemAdapter(CollectedPosts.this, posts);
            postsListView.setAdapter(listItemAdapter);
        }


        // convert InputStream to String
        private String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return sb.toString();
        }

        /***
         * парсинг json ответа от API
         * @return список постов
         */
        private ArrayList<ThePost> parseJSONFromFlatsAPI(String jsonString){
            ArrayList<ThePost> posts = new ArrayList<>();
            JSONArray postsJsonArr = null;
            JSONObject postJsonObject = null;
            ThePost post;
            try {
                postsJsonArr = new JSONArray(jsonString);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(postsJsonArr != null){
                for(int i = 0; i < postsJsonArr.length(); i++){
                    try {
                        postJsonObject = postsJsonArr.getJSONObject(i);
                        post = new ThePost();
                        post.setId(postJsonObject.getInt("ID"));
                        post.setPostText(postJsonObject.getString("PostText"));
                        post.setPropertyType(PropertyType.values()[postJsonObject.getInt("PropertyType")]);
                        post.setOfferType(OfferType.values()[postJsonObject.getInt("OfferType")]);
                        post.setCountryId(postJsonObject.getInt("CountryId"));
                        post.setCityId(postJsonObject.getInt("CityId"));
                        post.setRealtor(postJsonObject.getBoolean("IsRealtor"));

                        String pubDateAsString = postJsonObject.getString("PostPubDate");
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                        Date pubDateAsDate;
                        try{
                            pubDateAsDate = df.parse(pubDateAsString);
                        }
                        catch (Exception ex){
                            Log.e(MainActivity.THE_LOG,ex.getMessage());
                            pubDateAsDate = new Date();
                            pubDateAsDate.setTime(0);
                        }
                        post.setPostPubDate(pubDateAsDate);

                        post.setLink(postJsonObject.getString("Link"));
                        post.setPrice(postJsonObject.getInt("Price"));
                        post.setSourceId(postJsonObject.getInt("SourceId"));

                        posts.add(post);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            return posts;
        }
    }

    /***
     * Получить количество минут с момента последнего обращения к апи
     * @return
     */
    public int getMinutesSinceLastApiCall(){
        prefs = PreferenceManager.getDefaultSharedPreferences(CollectedPosts.this);
        String dateLastApiCallAsString = prefs.getString("last_api_call","1970-01-01 00:00:00");
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateLastApiCall = null;
        try {
            dateLastApiCall = parser.parse(dateLastApiCallAsString);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        long ss = new Date().getTime() - dateLastApiCall.getTime();
        int mm = (int)(ss / (60 * 1000));

        return mm;
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
    }
}
