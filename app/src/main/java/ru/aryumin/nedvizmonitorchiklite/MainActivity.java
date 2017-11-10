package ru.aryumin.nedvizmonitorchiklite;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    //Синглтон - фильтр настроек
    public static PostsListFilter postsListFilter;

    TextView mainTitleEditText;
    ImageButton databaseImageButton;
    ImageButton favouriteImageButton;
    ImageButton settingsImageButton;
    ImageButton aboutImageButton;
    Context context;


    final public static String THE_LOG = "THE_LOG";
    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //инициализируем гугл рекламу
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.google_ads_sdk_key));

        //инициализируем дефолтные настройки
        PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

        //создаем объект филььтра настроек
        context = MainActivity.this;
        postsListFilter = PostsListFilter.getInstance(context);

        //запускаем сервис проверки, если в фильтре свойство сервиса проверки - true
        if(!APICallsService.apiCallsServiceStarted && postsListFilter.isMakeServiceApiCalls()){
            APICallsService.apiCallsServiceStarted = true;
            startService(new Intent(this, APICallsService.class));
        }

        mainTitleEditText = (TextView)findViewById(R.id.mainTitleEditText);
        databaseImageButton = (ImageButton)findViewById(R.id.databaseImageButton);
        favouriteImageButton = (ImageButton)findViewById(R.id.favouriteImageButton);
        settingsImageButton = (ImageButton)findViewById(R.id.settingsImageButton);
        aboutImageButton = (ImageButton)findViewById(R.id.aboutImageButton);

        settingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        aboutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });


        databaseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CollectedPosts.class);
                startActivity(intent);
            }
        });

        favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavouritePosts.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //запуск сервиса
        if(!APICallsService.apiCallsServiceStarted && postsListFilter.isMakeServiceApiCalls()){
            APICallsService.apiCallsServiceStarted = true;
            startService(new Intent(this, APICallsService.class));
        }

        postsListFilter = PostsListFilter.getInstance(context);
        //указываем в текст вью над кнопками, сколько городов сейчас отслеживается
        mainTitleEditText = (TextView)findViewById(R.id.mainTitleEditText);
        mainTitleEditText.setText("Городов мониторится: " + postsListFilter.getCitiesIds().size());
    }


    /***
     * Проверка - работает ли сервис фоновой проверки новых постов
     * @param ctx
     * @param serviceClassName
     * @return
     */
    public static boolean isServiceRunning(Context ctx, String serviceClassName) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (APICallsService.class.getName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }
}
