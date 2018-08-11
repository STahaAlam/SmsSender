package com.comsouls.smssender;

 import android.Manifest;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
import android.content.pm.PackageManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
 import android.util.Log;
 import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.comsouls.smssender.msgSender.LocalBinder;

 import java.sql.Connection;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    Intent i;
    private int mchk=0;
    public static Context c;
    public static SharedPreferences sh;
    public static int state=0;
    public static SharedPreferences.Editor editor;
    private  msgSender mService;
     boolean mBound = false;
    //    private String name;
    String ip,db,un,password;
    TextView msgsForActivity,msgsForLife,toConnect;
    long msgofLife;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msgsForLife=findViewById(R.id.no_of_msgs_in_Life);
        msgsForActivity=findViewById(R.id.noofmsgs);

        c=getApplicationContext();
        sh = PreferenceManager.getDefaultSharedPreferences(c);
        editor = sh.edit();
        if (sh.getBoolean("is_first_time", true)) {
            Log.d("TAG", "First time");

            editor.putLong("NoOfmsgsInLife",0).commit();
            editor.putBoolean("is_first_time", false).commit();


        }else{
            msgofLife=sh.getLong("msgCount",0);
        msgsForLife.setText(String.valueOf(msgofLife));
        }
        final Intent intent=getIntent();

        if(intent.getStringExtra("ipAddress")!=null&&intent.getStringExtra("databaseName")!=null&&intent.getStringExtra("userName")!=null&&intent.getStringExtra("password")!=null){
            ip=intent.getStringExtra("ipAddress");
            db=intent.getStringExtra("databaseName");
            un=intent.getStringExtra("userName");
            password=intent.getStringExtra("password");

        }else{

            ip=sh.getString("ipAddress",null);
            db=sh.getString("databaseName",null);
            un=sh.getString("userName",null);
            password=sh.getString("password",null);

    }
    toConnect=findViewById(R.id.connecttoNetwork);

    toConnect.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(getApplicationContext(),GetConfig.class);
            startActivity(intent);
			finish();
        }
    });
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestSmsPermission();
        startButton=findViewById(R.id.on_or_off);
        String name=sh.getString("name",null);
        if(name!=null){
            if(name.equals("Stop")) {
                startButton.setText(name);
                state=0;

            }else {
                startButton.setText("Start");
                state=1;


            }
        }else{
            startButton.setText("Start");

        }

        /**
         * table name =MESSAGES;
         * column= pNo,status,Message
         */
        ConnectionClass connectionClass=new ConnectionClass(ip,db,un,password);
        if(ConnectionStatus.isConnectedToInternet(MainActivity.this)||ConnectionStatus.isConnectedToMobileNetwork(MainActivity.this)||ConnectionStatus.isConnectedToWifi(MainActivity.this)){
            connectionClass.setContext(this);
            final Connection con=connectionClass.CONN();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("this is","the minute");
                Log.e("this is",""+sh.getBoolean("permission",false));
                Log.e("this is",""+(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP)+"      "+android.os.Build.VERSION.SDK_INT+(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1));

                String name=(String)startButton.getText();
                if(((sh.getBoolean("permission",false)==true)&&(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1))||(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)){
                    if(name.equals("Start")){

                        if(con!=null){
                        i=new Intent(getApplicationContext(),msgSender.class);
                    state=1;
                        i.putExtra("no",state);
                        i.putExtra("ip",ip);
                        i.putExtra("db",db);
                        i.putExtra("un",un);
                        i.putExtra("no",1);
                        i.putExtra("password",password);

//                        startService(i);
                        Log.e("name","is");

                        Log.e("have binded service","");
                      if(ConnectionStatus.isNetworkConnectionAvailable(MainActivity.this))
                        {

                            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                       startService(i);
                        if(mBound){
                            msgsForActivity.setText(String.valueOf(0));

                        }
                            startButton.setText("Stop");
                        editor.putString("name","Stop").commit();}

                    }   else{
                            Toast.makeText(getApplicationContext(),"Please Connect To Data Base",Toast.LENGTH_LONG).show();
                        }}
                    else{
                        Intent i=new Intent(getApplicationContext(),msgSender.class);

                        if(mBound){
                            msgsForActivity.setText(String.valueOf(mService.getNoOfMessegesSend()));
                            msgofLife=Long.valueOf(msgsForLife.getText().toString())+Long.valueOf(msgsForActivity.getText().toString());
                            msgsForLife.setText(String.valueOf(msgofLife));
                        }
                        intent.putExtra("no",0);
                        unbindService(mConnection);
                        stopService(i);
                        startButton.setText("Start");
                        editor.putString("name","Start").commit();
                        editor.putInt("state",0).commit();

                    }}
                else
                    Toast.makeText(getApplicationContext(),"Sms permission rejected",Toast.LENGTH_LONG);

            }
        });}else{
            ConnectionStatus.showNoInternetAvailableErrorDialog(MainActivity.this);
        }


    }

    @Override
    protected void onDestroy() {
        Log.e("on destroy","killed");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        i=new Intent(getApplicationContext(),msgSender.class);

        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        super.onResume();


    }

    @Override
    protected void onStop() {
        editor.putLong("msgCount", Long.valueOf(msgsForLife.getText().toString())).commit();

        super.onStop();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
       @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
           Log.e("nbnb","nbnb");
            mBound = true;

        }

        // Called when the connection with the service disconnects unexpectedly
       @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e("activity", "onServiceDisconnected");
            mBound = false;
        }
    };
    private int requestSmsPermission() {
        String permission = Manifest.permission.SEND_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
            return 1;
        }
        return 0;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"permission granted", Toast.LENGTH_SHORT).show();
                editor.putBoolean("permission",true).commit();

            } else {
                Toast.makeText(MainActivity.this,"permission not granted", Toast.LENGTH_SHORT).show();
                editor.putBoolean("permission",false).commit();

            }
        }



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(getApplicationContext(),GetConfig.class);
            startActivity(intent);
			finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
