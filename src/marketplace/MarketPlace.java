package marketplace;


import bankrmi.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tobiaj on 2016-11-14.
 */
public class MarketPlace extends UnicastRemoteObject implements MarketplaceInterface {
    private String marketName;
    private int itemNumber = 0;
    private HashMap<String, Trader> traders = new HashMap<>();
    private HashMap<String, ClientInterface> clients = new HashMap<>();
    private Map<String, HashMap<Integer, Items>> categories = new HashMap<>();
    private Map<String, HashMap<String, Float>> lookupItems = new HashMap<>();

    public MarketPlace(String marketName) throws RemoteException {
        super();
        this.marketName = marketName;
    }

    public synchronized void printOutsOfLists(){
        System.out.println("Traders " + traders);
        System.out.println("Clients " + clients);
        System.out.println("Categories " + categories);
        System.out.println("lookupItems " + lookupItems);

    }

    public synchronized void login(String clientName, ClientInterface client){
        clients.put(clientName, client);
    }

    public synchronized void registerUser(String clientName, Account account, ClientInterface client){
        System.out.println("Registered client: " + clientName + " with account: " + account);
        Trader trader = new Trader(clientName, account);
        traders.put(clientName, trader);
        clients.put(clientName, client);
    }

    public synchronized void unRegisterUser(String clientName){
        System.out.println("Unregistered client: " + clientName);
        clients.remove(clientName);
        traders.remove(clientName);

        for (HashMap items : categories.values()){
            Iterator<Items> iterator = items.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Items item = (Items) entry.getValue();
                if (item.getOwner().equals(clientName)){
                    items.remove(entry.getKey());
                }
            }
        }

    }

    public synchronized void list(ClientInterface clientInterface){

        for (HashMap items : categories.values()) {
            Iterator<Items> iterator = items.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Items item = (Items) entry.getValue();
                String itemToReturn = ("ID: " + entry.getKey() + " Item: " + item.getName() + " Price: "
                        + item.getPrice() + " Seller: " + item.getOwner());
                try {
                    clientInterface.callBack(itemToReturn);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void addNewItemToSell(String clientName, String itemName, float price, ClientInterface clientInterface) {
        Trader trader = traders.get(clientName);
        if (trader != null ) {
            System.out.println("Add new item to sell : " + itemName + " " + price + " client: " + clientName);
            Items item = new Items(itemName, price, clientName);
            HashMap<Integer, Items> checkIfExist = categories.get(itemName);

            if (checkIfExist != null) {
                checkIfExist.put(itemNumber, item);
                itemNumber++;
            } else {
                checkIfExist = new HashMap<>();
                checkIfExist.put(itemNumber, item);
                categories.put(itemName, checkIfExist);
                itemNumber++;
            }

            checkIfSomeoneIsLookingForThisItem(item);
        }
        else {
            try {
                clientInterface.callBack("you need to register a account before selling an item");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    private synchronized void checkIfSomeoneIsLookingForThisItem(Items item) {
        String itemNameToLookup = item.getName();
        Float priceOnItemToLookup = item.getPrice();
        int i = 0;
        for (HashMap map : lookupItems.values()){
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String itemName = (String) entry.getKey();
                Float itemPrice = (Float) entry.getValue();
                if (itemName.equals(itemNameToLookup) && itemPrice.equals(priceOnItemToLookup)){
                    ClientInterface client = clients.get(lookupItems.keySet().toArray()[i]);
                    try {
                        client.callBack("The item: " + itemName + " with the price " +itemPrice + " is now available at the market");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            i++;
        }
    }

    public synchronized void buyItem(String clientName, String itemName, int itemNumber, ClientInterface clientInterface) {
        System.out.println("Buy item");
        Trader trader =  traders.get(clientName);
        if (trader != null) {
            Account account = trader.getAccount();
            System.out.println("Inputs : " + clientName + " " + itemName + " " + itemNumber);
            HashMap<Integer, Items> map = categories.get(itemName);
            Items itemToBuy = map.get(itemNumber);

            if (map != null){
                if (itemToBuy != null){
                    float price = itemToBuy.getPrice();

                    Boolean bool = checkBalance(price, account);
                    if (bool) {
                        try {
                            account.withdraw(price);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (RejectedException e) {
                            e.printStackTrace();
                        }

                        String owner = itemToBuy.getOwner();
                        trader = traders.get(owner);
                        account = trader.getAccount();
                        ClientInterface client;
                        try {
                            account.deposit(price);
                            client = clients.get(owner);
                            client.callBack(" The Client with name: " + clientName + " has bought your item " + itemToBuy.getName()
                                    + " and we have deposited " + price + " to your account");
                            client = clients.get(clientName);
                            client.callBack("Purchase of item: " + itemName + " has been successfull and we have drawn " + price + " from your account");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (RejectedException e) {
                            e.printStackTrace();
                        }
                        map.remove(itemNumber);
                    } else {
                        ClientInterface client = clients.get(clientName);
                        try {
                            client.callBack("Insufficient funds on your account");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                }
                else{
                    ClientInterface client = clients.get(clientName);
                    try {
                        client.callBack("Could not find item you were looking for!");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                ClientInterface client = clients.get(clientName);
                try {
                    client.callBack("Could not find item you were looking for!");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
        else{
            try {
                clientInterface.callBack("you need to register a account before selling an item");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void lookForItem(String clientName, String itemName, float price, ClientInterface clientInterface) {
        System.out.println("Look for item");
        Trader trader = traders.get(clientName);
        Items item = new Items(itemName, price, clientName);
        if (trader != null) {
            boolean bool = itemAlreadyExist(item);
            System.out.println(bool);
            if (!bool) {
                HashMap<String, Float> checkIfExist = lookupItems.get(clientName);

                if (checkIfExist != null) {
                    checkIfExist.put(itemName, price);
                } else {
                    checkIfExist = new HashMap<>();
                    checkIfExist.put(itemName, price);
                    lookupItems.put(clientName, checkIfExist);
                }
            }
            else{
                try {
                    clientInterface.callBack("Item you were looking for does already exist at the market");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
        else {
            try {
                clientInterface.callBack("you need to register a account before looking for an item");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

    }

    private Boolean itemAlreadyExist(Items itemToCheck) {
        for (HashMap items : categories.values()) {
            Iterator<Items> iterator = items.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Items item = (Items) entry.getValue();
                if (itemToCheck.getPrice() <= item.getPrice() && item.getName().equals(itemToCheck.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkBalance(float price, Account account) {
        System.out.println(price + " " + account);
        float balance = 0;
        try {
            balance = account.getBalance();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (balance >= price){
            return true;
        }
        else {
            return false;
        }

    }
}
