package ru.aryumin.nedvizmonitorchiklite;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.icu.util.TimeUnit;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aryumin on 03.01.17.
 */
public class APICallsService extends Service {

    public static boolean apiCallsServiceStarted = false;

    final String THE_LOG = "LOGGY";
    DbHelper dbHelper;
    SharedPreferences prefs = null;

    public void onCreate(){
        super.onCreate();
        Log.d(THE_LOG, "Сервис создан...");
        prefs = PreferenceManager.getDefaultSharedPreferences(APICallsService.this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(THE_LOG, "Сервис запущен...");
        makeApiCalls();
        //return super.onStartCommand(intent, flags, startId);
        //return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(THE_LOG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(THE_LOG, "ОнБайнд выполнен...");
        return null;
    }


    void makeApiCalls(){

        dbHelper = new DbHelper(APICallsService.this);
        ArrayList<ThePost> posts = dbHelper.getPostsFromLocalDb();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(true){
                    apiCall();
                    try {
                        //получаем текущий преференс со временем паузы между запросами
                        Log.d(THE_LOG, "спим...");
                        int pause = Integer.parseInt(prefs.getString("notify_freq", "30")) * 1000 * 60;
                        Thread.sleep(pause);
                    }
                    catch (InterruptedException ex){
                        Log.d(THE_LOG, ex.getMessage());
                    }
                }
            }
        }).start();
    }

    /***
     * Запрос к АПИ
     */
    void apiCall(){
        //запросы к апи и получение новых постов
        Log.d(THE_LOG, "Делаем запрос к АПИ...");
        PostsListFilter filter = PostsListFilter.getInstance(APICallsService.this);
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        ArrayList<String> apiLinks = filter.FormURLsList();
        ArrayList<ThePost> posts = new ArrayList<>();
        URL url;
        String resultXml = "";

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
        Log.d(THE_LOG, "Новых постов + " + posts.size());
        //Уведомление о получении новых постов
        if(posts.size() > 0){
            int NOTIFY_ID = 101;
            Context context = getApplicationContext();
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent =PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
            Resources res = context.getResources();
            Notification.Builder builder = new Notification.Builder(context);
            String mesaage = posts.size() + " новых объявлений записно в базу данных";
            builder.setContentIntent(contentIntent)
                    .setTicker("Новые (" + posts.size() + ") объявления о недвижимости!")
                    .setContentTitle("Есть новые объявления!")
                    .setContentText(mesaage)
                    .setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_notifications_blue_24dp1);

            Notification notification = builder.getNotification();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFY_ID, notification);
        }


        Log.d(THE_LOG, "Записываем посты в БД...");
        //запись в БД
        if(dbHelper == null)
            dbHelper = new DbHelper(APICallsService.this);
        for(ThePost post : posts){
            dbHelper.insertPostIntoDB(post);
        }
        Log.d(THE_LOG, "Записали посты в бд...");
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
