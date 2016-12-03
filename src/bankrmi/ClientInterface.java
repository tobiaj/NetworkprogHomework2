package bankrmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by tobiaj on 2016-11-16.
 */
public interface ClientInterface extends Remote, Serializable {

    public void callBack(String happened) throws RemoteException;
}
