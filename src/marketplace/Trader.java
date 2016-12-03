package marketplace;

import bankrmi.Account;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by tobiaj on 2016-11-08.
 */
public class Trader implements Serializable {
    private String traderName;
    private Account account;

    public Trader(String clientName, Account account){
        this.traderName = clientName;
        this.account = account;
    }

    public String getTraderName() {
        return traderName;
    }

    public Account getAccount() {
        return account;
    }
}
