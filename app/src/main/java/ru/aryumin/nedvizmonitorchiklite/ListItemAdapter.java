package ru.aryumin.nedvizmonitorchiklite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by aryumin on 01.01.17.
 */
public class ListItemAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater inflater;
    ArrayList<ThePost> posts;
    DbHelper dbHelper;

    ListItemAdapter(Context context, ArrayList<ThePost> posts){
        ctx = context;
        this.posts = posts;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    ThePost getPost(int position){
        return ((ThePost)getItem(position));
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null)
            view = inflater.inflate(R.layout.list_item, parent, false);

        final ThePost post = getPost(position);

        String resultTextPart = post.getPostText();
        if(resultTextPart.length() > 150)
            resultTextPart = resultTextPart.substring(0,149) + "...";
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String postDate = formatter.format(post.getPostPubDate()) + " (МСК)";

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

        String price = post.getPrice() != 0 ? ", " + String.valueOf(post.getPrice()) : "";

        String wasSeen = post.isWasSeen() ? "Просмотрено" : "Новое!";
        //сюда же в текствью просмотренно ли - добавим информацию, в избранном ли пост
        if(post.isFavourite()){
            wasSeen = "В избранном";
        }

        ((TextView)view.findViewById(R.id.post_meta_data_tv_at_list_item))
                .setText(postDate + "\n" + offerType + " " + propType + price);


        String simpleClassName = ctx.getClass().getSimpleName();

        if(!ctx.getClass().getSimpleName().equals("FavouritePosts")){
            ((TextView)view.findViewById(R.id.post_was_seen_tv_at_list_item))
                    .setText(wasSeen);
        }
        else{
            ((TextView)view.findViewById(R.id.post_was_seen_tv_at_list_item)).setVisibility(View.GONE);
        }

        ((TextView)view.findViewById(R.id.post_text_tv_at_list_item))
                .setText(resultTextPart);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, PostInfo.class);
                intent.putExtra("selectedPost", post);
                post.setWasSeen(true);
                if(dbHelper == null)
                    dbHelper = new DbHelper(ctx);
                dbHelper.updatePost(post);

                //Если пост в открываемом активити был добавлен в избранное, то код возврата 1.
                //Если не был добавлен (или был убран из избранного), то код будет 0;
                final int postAddedToFavouriteCode = 0;
                ((Activity)ctx).startActivityForResult(intent, postAddedToFavouriteCode);
            }
        });

        return view;
    }

}
