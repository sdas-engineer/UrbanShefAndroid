package com.urbanshef.urbanshefapp.objects;

public class Order {

    private String id;
    private String chefName;
    private String customerName;
//    private String customerFlat;
    private String customerAddress;
    private String customerPhone;
    private String customerImage;


    public Order(String id, String chefName, String customerName, String customerAddress, String customerPhone, String customerImage)
    {
        this.id = id;
        this.chefName = chefName;
        this.customerName = customerName;
//        this.customerFlat = customerFlat;
        this.customerAddress = customerAddress;
        this.customerPhone= customerPhone;
        this.customerImage = customerImage;
    }

    public String getId() {
        return id;
    }

    public String getChefName() {
        return chefName;
    }

    public String getCustomerName() {
        return customerName;
    }

//    public String getCustomerFlat() {
//        return customerFlat;
//    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerImage() {return customerImage; }


}
