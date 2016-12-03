package bankrmi;

import marketplace.MarketPlace;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    private static final String USAGE = "java bankrmi.Server <bank_rmi_url>";
    private static final String BANK = "Nordea";
    private static final String MARKETPLACE = "Market";

    public Server(String bankName, String marketName) {
        try {
            Bank bankobj = new BankImpl(bankName);
            marketplace.MarketPlace marketPlace = new marketplace.MarketPlace(marketName);
            // Register the newly created object at rmiregistry.
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(bankName, bankobj);
            Naming.rebind(marketName, marketPlace);
            System.out.println(bankobj + " is ready.");
            System.out.println(marketPlace + " is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 1 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String bankName;
        String marketName = MARKETPLACE;
        if (args.length > 0) {
            bankName = args[0];
        } else {
            bankName = BANK;
        }


        new Server(bankName, marketName);
    }
}
