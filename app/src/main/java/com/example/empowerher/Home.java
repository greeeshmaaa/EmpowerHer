package com.example.empowerher;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        String userName = sessionManager.getUserName();

        TextView textViewWelcome = findViewById(R.id.textViewWelcome);
        if (userName != null) {
            textViewWelcome.setText("Welcome, " + userName + "!");
        } else {
            textViewWelcome.setText("Welcome!");
        }
    }
}
