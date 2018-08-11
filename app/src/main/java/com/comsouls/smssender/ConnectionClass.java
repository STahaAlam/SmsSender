package com.comsouls.smssender;

/**
 * Created by Syed Taha Alam on 4/19/2018.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Syed Taha Alam on 4/16/2018.
 */

public class ConnectionClass {
    String ip,db,un,password;
    String classs="net.sourceforge.jtds.jdbc.Driver";
    Context context;
    Connection conn;
    
    public ConnectionClass(String ips, String dbs, String uns, String password) {
        this.ip = ips;
        this.db = dbs;
        this.un = uns;
        this.password = password;

    }
    
    public void setContext(Context context){
        this.context=context;
        
    }


    //    String ip="70.35.202.41";
//    String db="NEW_GARMENTS_DB";
//    String un="helahi";
//    String password="Cst42866800123456789";
    @SuppressLint("NewApi")
    public Connection CONN(){
        if(ConnectionStatus.isConnectedToInternet(context)||ConnectionStatus.isConnectedToMobileNetwork(context)||ConnectionStatus.isConnectedToWifi(context)) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {

                Class.forName(classs);
                String ConnURL = "jdbc:jtds:sqlserver://" + ip + "/"
                        + db +
                        ";user=" + un +
                        ";password=" + password + ";";
                conn = DriverManager.getConnection(ConnURL);
            } catch (ClassNotFoundException e) {
                Log.e("class nahi mli", "blkl");
                Log.e("error ", "" + e.getMessage());
                e.printStackTrace();
            } catch (SQLException e) {

                Log.e("sql nahi chalega", "");
                e.printStackTrace();
            }
            return conn;
        } else
            ConnectionStatus.showNoInternetAvailableErrorDialog(context);
    return null;}
        
}
