package marketplace;

import bankrmi.Account;
import bankrmi.Client;
import bankrmi.ClientInterface;
import bankrmi.RejectedException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by tobiaj on 2016-11-14.
 */
public interface MarketplaceInterface extends Remote  {
    public void addNewItemToSell(String clientName, String ItemName, float price, ClientInterface client) throws RemoteException, RejectedException;

    public void buyItem(String name, String item, int itemNumber, ClientInterface clientInterface) throws RemoteException;

    public void lookForItem(String name, String item, float price, ClientInterface clientInterface) throws RemoteException;

    public void list(ClientInterface clientInterface) throws RemoteException;

    public void registerUser(String name, Account account, ClientInterface client) throws RemoteException;

    public void unRegisterUser(String name) throws RemoteException;

    public void login(String name, ClientInterface client) throws RemoteException;

    public void printOutsOfLists() throws RemoteException;
}
