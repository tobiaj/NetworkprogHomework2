package marketplace;

import bankrmi.Client;

import java.util.HashMap;

/**
 * Created by tobiaj on 2016-11-08.
 */
public class Items {
    private String name;
    private float price;
    private String owner;

    public Items(String name, float price, String owner){
        this.name = name;
        this.price = price;
        this.owner = owner;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
