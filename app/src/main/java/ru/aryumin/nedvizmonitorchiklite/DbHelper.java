package ru.aryumin.nedvizmonitorchiklite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by aryumin on 15.12.16.
 */
public class DbHelper extends SQLiteOpenHelper {

    String THE_LOG = "LOGGY";
    SQLiteDatabase db;

    public DbHelper(Context context){
        super(context, "nedvizka", null,1);
        db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE posts (" +
                "id INTEGER PRIMARY KEY, " +
                "post_text TEXT, " +
                "offer_type INTEGER, " +
                "property_type INTEGER, " +
                "country_id INTEGER, " +
                "city_id INTEGER, " +
                "is_realtor INTEGER, " +
                "link TEXT, " +
                "post_pub_date TEXT, " +
                "price INTEGER, " +
                "source_id INTEGER, " +
                "was_seen INTEGER, " +
                "is_favourite INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /***
     * Получение постов из локальной БД
     * @return
     */
    public ArrayList<ThePost> getPostsFromLocalDb(){
        ArrayList<ThePost> posts = new ArrayList<>();

        ThePost thePost;
        Cursor c = db.query("posts", null, null, null, null, null, null);

        if (c.moveToFirst()){
            int idColumnIndex = c.getColumnIndex("id");
            int postTextColumnIndex = c.getColumnIndex("post_text");
            int offerTypeColumnIndex = c.getColumnIndex("offer_type");
            int propertyTypeColumnIndex = c.getColumnIndex("property_type");
            int countryIdColumnIndex = c.getColumnIndex("country_id");
            int cityIdColumnIndex = c.getColumnIndex("city_id");
            int isRealtorColumnIndex = c.getColumnIndex("is_realtor");
            int linkColumnIndex = c.getColumnIndex("link");
            int postPubDateColumnIndex = c.getColumnIndex("post_pub_date");
            int priceColumnIndex = c.getColumnIndex("price");
            int sourceIdColumnIndex = c.getColumnIndex("source_id");
            int wasSeenColumnIndex = c.getColumnIndex("was_seen");
            int isFavouriteColumnIndex = c.getColumnIndex("is_favourite");

            do {
                thePost = new ThePost();

                thePost.setId(c.getInt(idColumnIndex));
                thePost.setPostText(c.getString(postTextColumnIndex));
                thePost.setOfferType(OfferType.values()[c.getInt(offerTypeColumnIndex)]);
                thePost.setPropertyType(PropertyType.values()[c.getInt(propertyTypeColumnIndex)]);
                thePost.setCountryId(c.getInt(countryIdColumnIndex));
                thePost.setCityId(c.getInt(cityIdColumnIndex));
                thePost.setRealtor(c.getInt(isRealtorColumnIndex) == 0 ? false : true);
                thePost.setLink(c.getString(linkColumnIndex));

                String pubDateAsString = c.getString(postPubDateColumnIndex);
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

                thePost.setPostPubDate(pubDateAsDate);
                thePost.setPrice(c.getInt(priceColumnIndex));
                thePost.setSourceId(c.getInt(sourceIdColumnIndex));
                thePost.setWasSeen(c.getInt(wasSeenColumnIndex) == 0 ? false : true);
                thePost.setFavourite(c.getInt(isFavouriteColumnIndex) == 0 ? false : true);

                posts.add(thePost);
            }
            while (c.moveToNext());
        }

        return posts;
    }

    /***
     * Добавление поста в БД
     * @param post
     */
    public void insertPostIntoDB(ThePost post){

        ContentValues cv = new ContentValues();
        cv.put("id", post.getId());
        cv.put("post_text", post.getPostText());
        cv.put("offer_type", post.getOfferType().ordinal());
        cv.put("property_type", post.getPropertyType().ordinal());
        cv.put("country_id", post.getCountryId());
        cv.put("city_id", post.getCityId());
        cv.put("is_realtor", post.isRealtor());
        cv.put("link", post.getLink());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        cv.put("post_pub_date", df.format(post.getPostPubDate()));
        cv.put("price", post.getPrice());
        cv.put("source_id", post.getSourceId());
        cv.put("was_seen", 0);
        cv.put("is_favourite", 0);

        db.insert("posts", null, cv);
    }


    /***
     * Обновление поста в локальной базе данных
     * @param post
     */
    public void updatePost(ThePost post){
        ContentValues cv = new ContentValues();
        cv.put("was_seen", post.isWasSeen() ? 1 : 0);
        cv.put("is_favourite", post.isFavourite() ? 1 : 0);

        String table = "posts";
        String whereClause = "id=?";
        String[] whereArgs = new String[] { String.valueOf(post.getId()) };
        db.update(table,cv,whereClause,whereArgs);
    }

    /***
     * Получение поста по id
     * @param postId id поста
     * @return
     */
    public ThePost getPostById(int postId){
        if(db == null)
            db = this.getWritableDatabase();

        ThePost thePost = null;
        Cursor c = db.query("posts", null, null, null, null, null, null);

        int idColumnIndex = c.getColumnIndex("id");
        int postTextColumnIndex = c.getColumnIndex("post_text");
        int offerTypeColumnIndex = c.getColumnIndex("offer_type");
        int propertyTypeColumnIndex = c.getColumnIndex("property_type");
        int countryIdColumnIndex = c.getColumnIndex("country_id");
        int cityIdColumnIndex = c.getColumnIndex("city_id");
        int isRealtorColumnIndex = c.getColumnIndex("is_realtor");
        int linkColumnIndex = c.getColumnIndex("link");
        int postPubDateColumnIndex = c.getColumnIndex("post_pub_date");
        int priceColumnIndex = c.getColumnIndex("price");
        int sourceIdColumnIndex = c.getColumnIndex("source_id");
        int wasSeenColumnIndex = c.getColumnIndex("was_seen");
        int isFavouriteColumnIndex = c.getColumnIndex("is_favourite");

        if(c.moveToFirst()){
            thePost = new ThePost();

            thePost.setId(c.getInt(idColumnIndex));
            thePost.setPostText(c.getString(postTextColumnIndex));
            thePost.setOfferType(OfferType.values()[c.getInt(offerTypeColumnIndex)]);
            thePost.setPropertyType(PropertyType.values()[c.getInt(propertyTypeColumnIndex)]);
            thePost.setCountryId(c.getInt(countryIdColumnIndex));
            thePost.setCityId(c.getInt(cityIdColumnIndex));
            thePost.setRealtor(c.getInt(isRealtorColumnIndex) == 0 ? false : true);
            thePost.setLink(c.getString(linkColumnIndex));

            String pubDateAsString = c.getString(postPubDateColumnIndex);
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

            thePost.setPostPubDate(pubDateAsDate);
            thePost.setPrice(c.getInt(priceColumnIndex));
            thePost.setSourceId(c.getInt(sourceIdColumnIndex));
            thePost.setWasSeen(c.getInt(wasSeenColumnIndex) == 0 ? false : true);
            thePost.setFavourite(c.getInt(isFavouriteColumnIndex) == 0 ? false : true);
        }

        return thePost;
    }

    /***
     * Удаляет все посты в таблице posts
     */
    public void truncatePostsTable(){
        db.delete("posts", null, null);
    }

}
