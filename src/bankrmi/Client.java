package bankrmi;

import marketplace.MarketPlace;
import marketplace.MarketplaceInterface;
import marketplace.Trader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.StringTokenizer;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private static final String USAGE = "java bankrmi.Client <bank_url>";
    private static final String DEFAULT_BANK_NAME = "Nordea";
    private static final String DEFAULT_MARKET_NAME = "Market";
    Account account;
    Bank bankobj;
    MarketplaceInterface marketPlace;//Why is this suppose to be interface?
    private String bankname;
    private String marketname;
    String clientname;

    static enum CommandName {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit, help, list,
        listItems, sellItem, buyItem, lookup, register, login, unregister, print;
    };

    public Client(String bankName, String marketName) throws RemoteException {
        super();
        this.bankname = bankName;
        this.marketname = marketName;
        System.out.println(bankName + " " + marketName);
        try {
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            bankobj = (Bank) Naming.lookup(bankname);
            marketPlace = (MarketplaceInterface) Naming.lookup(marketname);
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to bank: " + bankname);

    }

    public Client() throws RemoteException {
        this(DEFAULT_BANK_NAME, DEFAULT_MARKET_NAME);
    }

    public void run() {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(clientname + "@" + bankname + ">");
            try {
                String userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (RejectedException re) {
                System.out.println(re);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void callBack(String happened){
        System.out.println(happened);

    }

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        CommandName commandName = null;
        String userName = null;
        float amount = 0;
        int userInputTokenNo = 1;
        String item = null;

        while (tokenizer.hasMoreTokens()) {
            switch (userInputTokenNo) {
                case 1:
                    try {
                        String commandNameString = tokenizer.nextToken();
                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist) {
                        System.out.println("Illegal command 1");
                        return null;
                    }
                    break;
                case 2:
                    userName = tokenizer.nextToken();
                    break;
                case 3:
                    try {
                        amount = Float.parseFloat(tokenizer.nextToken());

                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount 1");
                        return null;
                    }
                    break;
                case 4:
                        item = tokenizer.nextToken();
                    break;
                default:
                    System.out.println("Illegal command 2");
                    return null;
            }
            userInputTokenNo++;
        }
        return new Command(commandName, userName, amount, item);
    }

    void execute(Command command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }

        switch (command.getCommandName()) {
            case list:
                try {
                    for (String accountHolder : bankobj.listAccounts()) {
                        System.out.println(accountHolder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;

            case listItems:
                marketPlace.list(this);
                return;
            case quit:
                System.exit(0);
            case help:
                for (CommandName commandName : CommandName.values()) {
                    System.out.println(commandName);
                }
                return;
                        
        }

        // all further commands require a name to be specified
        String userName = command.getUserName();
        if (userName == null) {
            userName = clientname;
        }

        if (userName == null) {
            System.out.println("name is not specified");
            return;
        }

        switch (command.getCommandName()) {
            case newAccount:
                clientname = userName;
                bankobj.newAccount(userName);
                return;
            case deleteAccount:
                clientname = userName;
                bankobj.deleteAccount(userName);
                return;
            case register: //Trying to register a trader
                Account account = null;
                if (bankobj.getAccount(userName) == null) {
                    System.out.println("Register a account first");
                }
                else {
                    account = bankobj.getAccount(userName);
                    clientname = userName;
                    marketPlace.registerUser(clientname, account, this);
                }

                return;
            case unregister:
                clientname = userName;
                marketPlace.unRegisterUser(clientname);
                return;
        }

        //command, clientName, price, item
        String itemName = null;
        float amount = 0;
        if (clientname == null){
            clientname = command.getUserName();
        }
        switch (command.getCommandName()) {
            case sellItem: //New added case
                    itemName = command.getUserName();
                    amount = command.getAmount();
                    marketPlace.addNewItemToSell(clientname, itemName, amount, this);
                return;
            case buyItem: //New Added case
                    itemName = command.getUserName();
                    int itemNumber = (int)command.getAmount();
                    marketPlace.buyItem(clientname, itemName, itemNumber, this);
                return;
            case lookup:
                itemName = command.getUserName();
                amount = command.getAmount();
                marketPlace.lookForItem(clientname, itemName, amount, this);
                return;

            case print:
                marketPlace.printOutsOfLists();
                return;
        }

        // all further commands require a Account reference
        Account acc = bankobj.getAccount(userName);
        if (acc == null) {
            System.out.println("No account for " + userName);
            return;
        } else {
            account = acc;
            clientname = userName;
        }

        switch (command.getCommandName()) {
            case login:
                marketPlace.login(clientname, this);
                System.out.println(account);
                break;
            case deposit:
                account.deposit(command.getAmount());
                break;
            case withdraw:
                account.withdraw(command.getAmount());
                break;
            case balance:
                System.out.println("balance: $" + account.getBalance());
                break;
            default:
                System.out.println("Illegal command 3");
        }

    }

    //Class which is always created when an input is being made.
    private class Command {
        private String userName;
        private float amount;
        private CommandName commandName;
        private String item;

        private String getUserName() {
            return userName;
        }

        private float getAmount() {
            return amount;
        }

        private CommandName getCommandName() {
            return commandName;
        }

        public String getItem() {
            return item;
        }

        private Command(CommandName commandName, String userName, float amount, String item) {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
            this.item = item;
        }
    }

    public static void main(String[] args) {
        if ((args.length > 1) || (args.length > 0 && args[0].equals("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String bankName;
        String marketName = DEFAULT_MARKET_NAME;

        if (args.length > 0) {
            bankName = args[0];
            try {
                new Client(bankName, marketName).run();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                new Client().run();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
