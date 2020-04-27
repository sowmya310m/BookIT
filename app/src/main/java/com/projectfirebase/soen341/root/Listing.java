package com.projectfirebase.soen341.root;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class Listing {
	private String ID;
	private String sellerId;
	private String Name;
	private double Price;
	private String ImageURL;
	private int Category;
	private int SubCategory;
	private boolean inProcess;

	public Listing(){
		this.ID = "";
		this.sellerId = "";
		this.Name = "";
		this.Price = 0;
		this.ImageURL = "";
		this.Category = 0;
		this.SubCategory = 0;
	}

	public Listing(String id, String sellerId){
		this.ID = id;
		this.sellerId = sellerId;
	}

	public Listing(String id, String name, double price, String imageURL) {
		this.ID = id;
		this.Name = name;
		this.Price = price;
		this.ImageURL = imageURL;
	}

	public Listing(String id, String name, double price, String imageURL, int category, int subCategory) {
		this.ID = id;
		this.Name = name;
		this.Price = price;
		this.ImageURL = imageURL;
		this.Category = category;
		this.SubCategory = subCategory;
	}

    public Listing(String id, String name, Double price, String imageURL, int category, int subCategory, boolean inProcess) {
        this.ID = id;
        this.Name = name;
        this.Price = price;
        this.ImageURL = imageURL;
        this.Category = category;
        this.SubCategory = subCategory;
        this.inProcess = inProcess;
    }

    @PropertyName("ID")
	public String getID() {
		return ID;
	}

	@PropertyName("OwnerID")
	public String getsellerId() {
		return sellerId;
	}

	@PropertyName("Name")
	public String getName() {
		return Name;
	}

	@PropertyName("Price")
	public double getPrice() {
		return Price;
	}

	@PropertyName("ImageURL")
	public String getImageURL(){
		return this.ImageURL;
	}

	@PropertyName("Category")
	public int getCategory() { return this.Category; }

	@PropertyName("SubCategory")
	public int getSubCategory() { return this.SubCategory; }

	@Exclude
	public void setName(String name) {
		this.Name = name;
	}

	@Exclude
	public void setPrice(double price) {
		this.Price = price;
	}

	@Exclude
	public void setImageURL(String url){
		this.ImageURL = url;
	}

	@Exclude
	public void setCategory(int category) { this.Category = category; }

	@Exclude
	public void setSubCategory(int subCategory) { this.SubCategory = subCategory; }

	public boolean getInProcess() { return this.inProcess; }

	public void setInProcess(boolean inProcess) { this.inProcess = inProcess; }

}
