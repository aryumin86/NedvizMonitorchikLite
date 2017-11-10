package ru.aryumin.nedvizmonitorchiklite;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aryumin on 20.12.16.
 */
@SuppressWarnings("serial")
public class ThePost implements Serializable {
    private int id;
    private String postText;
    private PropertyType propertyType;
    private OfferType offerType;
    private int countryId;
    private int cityId;
    private boolean isRealtor;
    private Date postPubDate;
    private String link;
    private int price;
    private int sourceId;
    private boolean wasSeen;
    private boolean isFavourite;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public OfferType getOfferType() {
        return offerType;
    }

    public void setOfferType(OfferType offerType) {
        this.offerType = offerType;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public boolean isRealtor() {
        return isRealtor;
    }

    public void setRealtor(boolean realtor) {
        isRealtor = realtor;
    }

    public Date getPostPubDate() {
        return postPubDate;
    }

    public void setPostPubDate(Date postPubDate) {
        this.postPubDate = postPubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public boolean isWasSeen() {
        return wasSeen;
    }

    public void setWasSeen(boolean wasSeen) {
        this.wasSeen = wasSeen;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public String toString(){
        return this.getPostPubDate() + " | " + this.getPrice() + "\n" + this.getPostText();
    }
}
