package com.comsouls.smssender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import static com.comsouls.smssender.MainActivity.editor;
import static com.comsouls.smssender.MainActivity.sh;

/**
 * Created by Syed Taha Alam on 4/19/2018.
 */

public class msgSender extends Service {
    private static final String TAG = "MyService";
    private boolean isRunning = false;
    private Looper looper;
    private MyServiceHandler myServiceHandler;
    private int no;
    public static long noOfMessegesSend=0;
    private static long noOfMesseagesTillLife;


    private String ip;
    private String password;
    private String un;
    private String db;
    private ConnectionClass connectionClass;

    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        msgSender getService() {
            // Return this instance of LocalService so clients can call public methods
            return msgSender.this;
        }
    }
    @Override
    public void onCreate() {
        HandlerThread handlerthread = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        looper = handlerthread.getLooper();
        myServiceHandler = new MyServiceHandler(looper);
        isRunning = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = myServiceHandler.obtainMessage();
        msg.arg1 = startId;
        no = 1;

        if(intent!=null) {
            noOfMesseagesTillLife=0;
            ip=intent.getStringExtra("ip");
            db=intent.getStringExtra("db");
            no=intent.getIntExtra("no",0);
            un=intent.getStringExtra("un");
            password=intent.getStringExtra("password");
            connectionClass=new ConnectionClass(ip,db,un,password);
            myServiceHandler.sendMessage(msg);
        }

        //If service is killed while starting, it restarts.
        return START_REDELIVER_INTENT;
    }

    public static void setNoOfMessegesSend(long noOfMessegesSend) {
        msgSender.noOfMessegesSend = noOfMessegesSend;
    }

    public long getNoOfMessegesSend(){
        return noOfMessegesSend;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        no = 0;
        noOfMessegesSend=0;
        editor.putString("NoOfMessages",String.valueOf(noOfMessegesSend)).commit();
    }


    private final class MyServiceHandler extends Handler {
        public MyServiceHandler(Looper looper) {
            super(looper);
        }

        @SuppressLint("UnlocalizedSms")
        @Override
        public void handleMessage(Message msg) {

            synchronized (this) {
                Connection conn=null;
                final String toGetStatus = "status";
                String toGetPhoneNo = "pNo";
                String toGetMessage = "message";
                Context mContext = getApplicationContext();

                while (no == 1) {
                    if(ConnectionStatus.isConnectedToInternet(getApplicationContext())||ConnectionStatus.isConnectedToMobileNetwork(getApplicationContext())||ConnectionStatus.isConnectedToWifi(getApplicationContext())) {

                        conn = connectionClass.CONN();
                    }
                    ResultSet rs = null;
                    if (conn != null) {
                        try {
                            conn.setReadOnly(false);
                            Log.e("no ","matched");
                            String str = "select * from MESSAGES where "+toGetStatus +" = 'Pending'";
                            if(ConnectionStatus.isConnectedToInternet(getApplicationContext())||ConnectionStatus.isConnectedToMobileNetwork(getApplicationContext())||ConnectionStatus.isConnectedToWifi(getApplicationContext())) {

                                Statement statement = conn.createStatement();
                                rs = statement.executeQuery(str);
                            }
                            while (rs.next()) {
                                Log.e("status",rs.getString(toGetStatus));

                                    SmsManager sms = SmsManager.getDefault();
                                    noOfMessegesSend++;
                                    sms.sendTextMessage(rs.getString(toGetPhoneNo), null, rs.getString(toGetMessage), null, null);

                                    String name = "Sent";
                                    String query = "UPDATE Messages SET  " + toGetStatus + " =\'" + name + "\' WHERE msgID = " + rs.getInt("msgID") + ";";
                                    Statement stmt = conn.createStatement();
                                    stmt.executeUpdate(query);
                                    Thread.sleep(1000*5);



                            }
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }

                    }

                }
                //stops the service for the start id.
                if (no == 0) {stopSelfResult(msg.arg1);

                }
            }

        }
    }

}