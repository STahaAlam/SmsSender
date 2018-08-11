package com.comsouls.smssender;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.comsouls.smssender.MainActivity.editor;

public class GetConfig extends AppCompatActivity {
    private EditText ipAddress,databaseName,userName,password;
    private ProgressDialog progressBar;


// ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_config);

        progressBar=new ProgressDialog(this);
        progressBar.setCancelable(false);

        ipAddress=findViewById(R.id.ip_address);
        databaseName=findViewById(R.id.database_name);
        userName=findViewById(R.id.username);
        password=findViewById(R.id.password);
        if(ConnectionStatus.isConnectedToInternet(GetConfig.this)||ConnectionStatus.isConnectedToMobileNetwork(GetConfig.this)||ConnectionStatus.isConnectedToWifi(GetConfig.this)){
        final ConnectionClass connection=new ConnectionClass(ipAddress.getText().toString(),databaseName.getText().toString(),userName.getText().toString(),password.getText().toString());
        connection.setContext(GetConfig.this);
        Button connect=findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ConnectionStatus.isNetworkConnectionAvailable(GetConfig.this))
                {try {
                    progressBar.setMessage("Connecting...");
                    progressBar.show();
                    creatConnection(connection);
                    startActivity(new Intent(GetConfig.this,MainActivity.class));
					finish();
                } catch (SQLException e) {
                    progressBar.dismiss();
                    Toast.makeText(GetConfig.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }}
            }
        });

    }else{
            ConnectionStatus.showNoInternetAvailableErrorDialog(GetConfig.this);
        }
    }

    private void creatConnection(ConnectionClass connection) throws SQLException {
        Connection connection1=connection.CONN();
        if(connection1!=null){
            String str = "select * from MESSAGES";
                            Statement statement = connection1.createStatement();
                            final ResultSet rs = statement.executeQuery(str);
                            if(rs.next())
                            {
                                progressBar.dismiss();
                                Toast.makeText(getApplicationContext(), "Connection has been made", Toast.LENGTH_SHORT).show();

                            } else{
                                progressBar.dismiss();
                                Toast.makeText(getApplicationContext(),"No data is in the table",Toast.LENGTH_LONG).show();
                            }

        }else
        {
            progressBar.dismiss();
            Toast.makeText(getApplicationContext(), "Connection hasn't been made", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("ipAddress",ipAddress.getText().toString());
        intent.putExtra("databaseName",databaseName.getText().toString());
        intent.putExtra("userName",userName.getText().toString());
        intent.putExtra("password",password.getText().toString());
        editor.putString("ipAddress",ipAddress.getText().toString()).commit();
        editor.putString("databaseName",databaseName.getText().toString()).commit();
        editor.putString("userName",userName.getText().toString()).commit();
        editor.putString("password",password.getText().toString()).commit();
        startActivity(intent);
        finish();

    }
}
