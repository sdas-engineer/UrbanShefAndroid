package com.urbanshef.urbanshefapp.objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Chef  implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("chef_street_address")
    @Expose
    private ChefStreetAddress chefStreetAddress;
    @SerializedName("chef_flat_number")
    @Expose
    private String chefFlatNumber;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("postcode")
    @Expose
    private String postcode;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("stripe_user_id")
    @Expose
    private String stripeUserId;
    @SerializedName("stripe_access_token")
    @Expose
    private String stripeAccessToken;
    @SerializedName("available")
    @Expose
    private Boolean available;
    @SerializedName("level_2_food_hygiene_certificate")
    @Expose
    private String level2FoodHygieneCertificate;
    @SerializedName("disabled_by_admin")
    @Expose
    private Boolean disabledByAdmin;
    @SerializedName("note")
    @Expose
    private String note;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("date_of_birth")
    @Expose
    private String dateOfBirth;
    @SerializedName("gender")
    @Expose
    private String gender;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ChefStreetAddress getChefStreetAddress() {
        return chefStreetAddress;
    }

    public void setChefStreetAddress(ChefStreetAddress chefStreetAddress) {
        this.chefStreetAddress = chefStreetAddress;
    }

    public String getChefFlatNumber() {
        return chefFlatNumber;
    }

    public void setChefFlatNumber(String chefFlatNumber) {
        this.chefFlatNumber = chefFlatNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getStripeUserId() {
        return stripeUserId;
    }

    public void setStripeUserId(String stripeUserId) {
        this.stripeUserId = stripeUserId;
    }

    public String getStripeAccessToken() {
        return stripeAccessToken;
    }

    public void setStripeAccessToken(String stripeAccessToken) {
        this.stripeAccessToken = stripeAccessToken;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getLevel2FoodHygieneCertificate() {
        return level2FoodHygieneCertificate;
    }

    public void setLevel2FoodHygieneCertificate(String level2FoodHygieneCertificate) {
        this.level2FoodHygieneCertificate = level2FoodHygieneCertificate;
    }

    public Boolean getDisabledByAdmin() {
        return disabledByAdmin;
    }

    public void setDisabledByAdmin(Boolean disabledByAdmin) {
        this.disabledByAdmin = disabledByAdmin;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
