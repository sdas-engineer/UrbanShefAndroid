package com.urbanshef.urbanshefapp.objects;

public class Meal {

    private String id, name, short_description, image, availability, spicy, diet;
    private Float price;

    public Meal(String id, String name, String short_description, String image, Float price, String availability, String spicy, String diet) {
        this.id = id;
        this.name = name;
        this.short_description = short_description;
        this.image = image;
        this.price = price;
        this.availability = availability;
        this.spicy = spicy;
        this.diet = diet;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getImage() {
        return image;
    }

    public Float getPrice() {
        return price;
    }

    public String getAvailability() {
        return availability;
    }

    public String getSpicy() {
        return spicy;
    }

    public String getDiet() {
        return diet;
    }
}
