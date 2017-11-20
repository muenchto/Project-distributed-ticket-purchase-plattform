package client;

import auxiliary.DataStorageIF;
import auxiliary.WideBoxIF;

import java.rmi.Remote;

public class FaultGeneratorThread extends Thread{

    private String serverId;
    private WideBoxIF wideBoxStub;
    private DataStorageIF dbServerStub;

    public FaultGeneratorThread(String serverIdi, Remote stub){
        this.serverId = serverId;
        if(stub.getClass().equals(WideBoxIF.class)){
            this.wideBoxStub = (WideBoxIF) stub;
        }else{
            this.dbServerStub = (DataStorageIF) stub;
        }
    }

    @Override
    public void run(){
           
    }

}
