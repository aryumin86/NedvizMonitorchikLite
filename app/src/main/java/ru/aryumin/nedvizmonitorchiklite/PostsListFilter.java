package ru.aryumin.nedvizmonitorchiklite;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by aryumin on 31.12.16.
 * Фильтр найстроек запросов к АПИ и отображения постов в списке
 * Поля фильтра определяются sharedPreferences
 */
public class PostsListFilter {

    private static Context context; //контекст вызывающего объект активити
    private boolean showPostsFromRieltors;
    private int numPostsToShow;
    private int apiCallsFrequency;
    private ArrayList<Integer> citiesIds;
    private ArrayList<String> citiesTitles;
    private boolean showLivingPropertyPosts;
    private boolean showNONLivingPropertyPosts;
    private boolean showLetPosts;
    private boolean showRentPosts;
    private boolean showSellPosts;
    private boolean showBuyPosts;
    private boolean isDemoUSer;
    private boolean makeServiceApiCalls;

    public boolean isMakeServiceApiCalls() {
        return makeServiceApiCalls;
    }

    public void setMakeServiceApiCalls(boolean makeServiceApiCalls) {
        this.makeServiceApiCalls = makeServiceApiCalls;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isShowPostsFromRieltors() {
        return showPostsFromRieltors;
    }

    public void setShowPostsFromRieltors(boolean showPostsFromRieltors) {
        this.showPostsFromRieltors = showPostsFromRieltors;
    }

    public int getNumPostsToShow() {
        return numPostsToShow;
    }

    public void setNumPostsToShow(int numPostsToShow) {
        this.numPostsToShow = numPostsToShow;
    }

    public int getApiCallsFrequency() {
        return apiCallsFrequency;
    }

    public void setApiCallsFrequency(int apiCallsFrequency) {
        this.apiCallsFrequency = apiCallsFrequency;
    }

    public ArrayList<Integer> getCitiesIds() {
        return citiesIds;
    }

    public void setCitiesIds(ArrayList<Integer> citiesIds) {
        this.citiesIds = citiesIds;
    }

    public ArrayList<String> getCitiesTitles() {
        return citiesTitles;
    }

    public void setCitiesTitles(ArrayList<String> citiesTitles) {
        this.citiesTitles = citiesTitles;
    }

    public boolean isShowLivingPropertyPosts() {
        return showLivingPropertyPosts;
    }

    public void setShowLivingPropertyPosts(boolean showLivingPropertyPosts) {
        this.showLivingPropertyPosts = showLivingPropertyPosts;
    }

    public boolean isShowNONLivingPropertyPosts() {
        return showNONLivingPropertyPosts;
    }

    public void setShowNONLivingPropertyPosts(boolean showNONLivingPropertyPosts) {
        this.showNONLivingPropertyPosts = showNONLivingPropertyPosts;
    }

    public boolean isShowLetPosts() {
        return showLetPosts;
    }

    public void setShowLetPosts(boolean showLetPosts) {
        this.showLetPosts = showLetPosts;
    }

    public boolean isShowRentPosts() {
        return showRentPosts;
    }

    public void setShowRentPosts(boolean showRentPosts) {
        this.showRentPosts = showRentPosts;
    }

    public boolean isShowSellPosts() {
        return showSellPosts;
    }

    public void setShowSellPosts(boolean showSellPosts) {
        this.showSellPosts = showSellPosts;
    }

    public boolean isShowBuyPosts() {
        return showBuyPosts;
    }

    public void setShowBuyPosts(boolean showBuyPosts) {
        this.showBuyPosts = showBuyPosts;
    }

    public boolean isDemoUSer() {
        return isDemoUSer;
    }

    public void setDemoUSer(boolean demoUSer) {
        isDemoUSer = demoUSer;
    }


    private static PostsListFilter filterInstance;

    public static PostsListFilter getInstance(Context invokerContext) {
        context = invokerContext;
        if(filterInstance == null)
            filterInstance = new PostsListFilter();
        return filterInstance;
    }

    private PostsListFilter() {

        SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(context);
        setShowPostsFromRieltors(prefs.getBoolean("SHOW_FROM_REALTORS", true));
        setNumPostsToShow(Integer.parseInt(prefs.getString("num_posts_to_show", "30")));
        setApiCallsFrequency(Integer.parseInt(prefs.getString("notify_freq", "15")));
        setShowLivingPropertyPosts(prefs.getBoolean("SHOW_LIVINGS", true));
        setShowNONLivingPropertyPosts(prefs.getBoolean("SHOW_NON_LIVINGS", false));
        setShowLetPosts(prefs.getBoolean("SHOW_LET_POSTS",true));
        setShowRentPosts(prefs.getBoolean("SHOW_RENT_POSTS",false));
        setShowBuyPosts(prefs.getBoolean("SHOW_BUY_POSTS",false));
        setShowSellPosts(prefs.getBoolean("SHOW_SELL_POSTS",false));
        setDemoUSer(prefs.getBoolean("is_demo_user",false));
        setMakeServiceApiCalls(prefs.getBoolean("API_CALLS_BACKGROUND_SERVICE_ACTIVE", true));

        citiesIds = new ArrayList<>();
        citiesTitles = new ArrayList<>();

        //итерируемся по всем преференсам, чтобы собрать id городов
        Map<String,?> keys = prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            if(entry.getKey().matches("^\\d+$") && prefs.getBoolean(entry.getKey(), false)
                    && !citiesIds.contains(Integer.parseInt(entry.getKey())))
                citiesIds.add(Integer.parseInt(entry.getKey()));
        }

    }

    /***
     * Уничтожает синглтон (приравнивает null)
     */
    public void dropPostsListFilter(){
        filterInstance = null;
    }

    /***
     * сформировать список url для запросов к апи на основе найстроек приложения
     * @return
     */
    public ArrayList<String> FormURLsList(){
        ArrayList<String> links = new ArrayList<>();
        ArrayList<String> apis = new ArrayList<>();
        ArrayList<String> offerTypes = new ArrayList<>();
        //String rawLink = "http://91.240.87.222/api/FlatPosts/668/1/1000/2016-11-23-01-01";

        if(this.isShowLivingPropertyPosts()){
            apis.add("FlatPosts");
            apis.add("RoomPosts");
            apis.add("HousePosts");
        }
        if(this.showNONLivingPropertyPosts){
            apis.add("FactoryPosts");
            apis.add("GaragePosts");
            apis.add("OfficePosts");
            apis.add("PhotoStudiaPosts");
            apis.add("RestourantPosts");
            apis.add("StockPosts");
            apis.add("TerraPosts");
            apis.add("TradePosts");
        }

        if(this.showLetPosts)
            offerTypes.add("1");
        if(this.showRentPosts)
            offerTypes.add("2");
        if(this.showBuyPosts)
            offerTypes.add("3");
        if(this.showSellPosts)
            offerTypes.add("4");

        for(String api : apis){
            for(int cityId : this.getCitiesIds()){
                for(String ot : offerTypes){
                    String rawLink = "http://91.240.87.222/api/[api]/[city_id]/[offer_type]/10000/2016-11-23-01-01";
                    rawLink = rawLink.replace("[api]",api);
                    rawLink = rawLink.replace("[city_id]", String.valueOf(cityId));
                    rawLink = rawLink.replace("[offer_type]", ot);
                    links.add(rawLink);
                }
            }
        }

        return links;
    }


    /***
     * Из свежеполученных от API постов оставляет только новые, которых нет в БД
     * @param pp только что полученные от API посты
     * @return
     */
    public ArrayList<ThePost> FilterOldPostsFromNewlyGotList(ArrayList<ThePost> pp){
        DbHelper dbHelper = new DbHelper(context);
        ArrayList<ThePost> postsFromDb = dbHelper.getPostsFromLocalDb();
        ArrayList<Integer> dbPostsIds = new ArrayList<>();
        ArrayList<ThePost> result = new ArrayList<>();

        for(ThePost post : postsFromDb){
            dbPostsIds.add(post.getId());
        }
        for(ThePost post : pp){
            if(!dbPostsIds.contains(post.getId()))
                result.add(post);
        }

        return result;
    }


    /***
     * Фильтрует полученные от АПИ посты в соответствии с настройками приложения.
     * @param pp Весь список постов в таблице БД, из которого надо оставить только актуальную
     *           в соответствии с настройками часть и режимом (демо, оплаченный)
     * @return актуальная в соответствии с настройками часть постов
     */
    public ArrayList<ThePost> FilterPostsList(ArrayList<ThePost> pp){

        ArrayList<ThePost> result = new ArrayList<>();

        if(!isShowPostsFromRieltors()){
            for(ThePost p : pp){
                if(!p.isRealtor())
                    result.add(p);
            }
        }
        else
            result = pp;

        ArrayList<ThePost> temp = new ArrayList<>();

        if(isShowLetPosts()){
            for(ThePost p : result){
                if(p.getOfferType() == OfferType.LET)
                    temp.add(p);
            }
        }

        if(isShowRentPosts()){
            for(ThePost p : result){
                if(p.getOfferType() == OfferType.RENT)
                    temp.add(p);
            }
        }

        if(isShowBuyPosts()){
            for(ThePost p : result){
                if(p.getOfferType() == OfferType.BUY)
                    temp.add(p);
            }
        }

        if(isShowSellPosts()){
            for(ThePost p : result){
                if(p.getOfferType() == OfferType.SELL)
                    temp.add(p);
            }
        }

        result = temp;
        temp = new ArrayList<>();

        if(isShowLivingPropertyPosts()){
            for(ThePost p : result){
                if(p.getPropertyType() == PropertyType.Flat || p.getPropertyType() == PropertyType.Room
                        || p.getPropertyType() == PropertyType.House)
                    temp.add(p);
            }
        }

        if(isShowNONLivingPropertyPosts()){
            for(ThePost p : result){
                if(p.getPropertyType() == PropertyType.Factory || p.getPropertyType() == PropertyType.Garage
                        || p.getPropertyType() == PropertyType.Office || p.getPropertyType() == PropertyType.PhotoStudia
                        || p.getPropertyType() == PropertyType.Restaurant || p.getPropertyType() == PropertyType.Stock
                        || p.getPropertyType() == PropertyType.Terra || p.getPropertyType() == PropertyType.Trade)
                    temp.add(p);
            }
        }

        result = temp;
        temp = new ArrayList<>();

        if(isDemoUSer){
            if(result.size() > 0 && result.size() < 5)
                temp = new ArrayList<>(result.subList(0,1));
            else {
                for(int i = 0; i < result.size(); i++){
                    if(i % 4 == 0)
                        temp.add(result.get(i));
                }
            }
        }
        else {
            temp = result;
        }

        result = temp;

        //сортируем по дате - вначале будет самое свежее
        if(result.size() > 0){
            Collections.sort(result, new Comparator<ThePost>() {
                @Override
                public int compare(ThePost t1, ThePost t2) {
                    return t2.getPostPubDate().compareTo(t1.getPostPubDate());
                }
            });
        }

        if(result.size() > getNumPostsToShow()){
            //result = (ArrayList<ThePost>)result.subList(0, getNumPostsToShow());
            result = new ArrayList(result.subList(0, getNumPostsToShow()));
        }


        return result;
    }
}
