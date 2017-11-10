package ru.aryumin.nedvizmonitorchiklite;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.w3c.dom.Text;

import java.text.Format;
import java.text.SimpleDateFormat;

public class PostInfo extends AppCompatActivity {

    //Этот id поста используется, чтобы менять статус избранного у поста
    //Он используется в классе CollectedPosts в переопределенном методе onActivityResult
    //Этот метод получает итоговый статус избранного для поста. В нем же проиходит поиск поста
    //с этим id и измеение свойства в объекте ThePost для списка всех постов ListView
    public static int postId;
    AdView mAdView;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

        //баннерная реклама
        mAdView = (AdView) findViewById(R.id.adViewPostInfo);
        //AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                //.addTestDevice("B5B083E466C03CBB920F2482192B9E93")  // An example device ID
                .build();
        mAdView.loadAd(adRequest);

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

        final DbHelper dbHelper = new DbHelper(PostInfo.this);
        final Intent intent = getIntent();
        final ThePost post = (ThePost)intent.getSerializableExtra("selectedPost");

        postId = post.getId();

        String offerType;
        if(post.getOfferType() == OfferType.LET)
            offerType = "СДАМ";
        else if (post.getOfferType() == OfferType.RENT)
            offerType = "СНИМУ";
        else if (post.getOfferType() == OfferType.BUY)
            offerType = "КУПЛЮ";
        else
            offerType = "ПРОДАМ";

        String propType;
        if(post.getPropertyType() == PropertyType.Room)
            propType = "КОМНАТУ";
        else if(post.getPropertyType() == PropertyType.Flat)
            propType = "КВАРТИРУ";
        else if(post.getPropertyType() == PropertyType.House)
            propType = "ДОМ";
        else if(post.getPropertyType() == PropertyType.Office)
            propType = "ОФИС";
        else if(post.getPropertyType() == PropertyType.Garage)
            propType = "ГАРАЖ";
        else if(post.getPropertyType() == PropertyType.Terra)
            propType = "ЗЕМЕЛЬНЫЙ УЧАСТОК";
        else if(post.getPropertyType() == PropertyType.PhotoStudia)
            propType = "ФОТОСТУДИЮ";
        else if(post.getPropertyType() == PropertyType.Stock)
            propType = "СКЛАД";
        else if(post.getPropertyType() == PropertyType.Trade)
            propType = "ТОРГОВОЕ ПОМЕЩЕНИЕ";
        else if(post.getPropertyType() == PropertyType.Factory)
            propType = "ЗАВОДСКОЕ ПОМЕЩЕНИЕ";
        else
            propType = "КАФЕ/РЕСТОРАН";

        setTitle(offerType + " " + propType);

        TextView postMetaTextView = (TextView)findViewById(R.id.post_info_meta_tv);
        TextView postTextTextView = (TextView)findViewById(R.id.post_info_text_tv);
        Button openInBrowserButton = (Button)findViewById(R.id.post_open_in_browser_button);
        final Button addToFavouriteButton = (Button)findViewById(R.id.post_add_to_favourite_button);

        openInBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                urlIntent.setData(Uri.parse(post.getLink()));
                startActivity(urlIntent);
            }
        });

        if(post.isFavourite())
            addToFavouriteButton.setText("Убрать из избранного");

        addToFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toastFav = "";
                if(post.isFavourite()){
                    post.setFavourite(false);
                    toastFav = "Убрано из избранного";
                    addToFavouriteButton.setText("Добавить в избранное");
                    setResult(0,intent);
                }

                else {
                    post.setFavourite(true);
                    toastFav = "Добавлено в избранное";
                    addToFavouriteButton.setText("Убрать из избранного");
                    setResult(1);
                }

                dbHelper.updatePost(post);
                Toast.makeText(PostInfo.this, toastFav, Toast.LENGTH_SHORT).show();
            }
        });

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String postDate = formatter.format(post.getPostPubDate()) + " (МСК)";
        String price = post.getPrice() != 0 ? "\nЦена: " + String.valueOf(post.getPrice()) : "";
        String isRealtor = post.isRealtor() ? "\nВозможно, посредник!" : "";

        postMetaTextView.setText("Опубликовано: " + postDate + price + isRealtor);
        postTextTextView.setText(post.getPostText());

    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }


}
