package com.example.empowerher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity {

    EditText editTextEmail2;
    EditText editTextPasswordLogin;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        sessionManager = new SessionManager(this);

        editTextEmail2 = findViewById(R.id.editTextEmail2);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordlogin);

        TextView textViewLogIn = findViewById(R.id.textViewLogIn);
        textViewLogIn.setOnClickListener(v -> login());
    }

    private void login() {
        String email = editTextEmail2.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://ec2-13-126-93-228.ap-south-1.compute.amazonaws.com:3000/api/login"; // Ensure this matches your server's actual URL.

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        String message = response.getString("message");
                        Toast.makeText(LogIn.this, message, Toast.LENGTH_SHORT).show();

                        if (message.equals("Login successful")) {
                            String userName = response.getString("userName");
                            sessionManager.saveUserName(userName);

                            // Navigate to the Home Activity
                            Intent intent = new Intent(LogIn.this, Home.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LogIn.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(LogIn.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                if (rawCookies != null && !rawCookies.isEmpty()) {
                    sessionManager.saveSessionCookie(rawCookies.split(";", 2)[0]);
                }
                return super.parseNetworkResponse(response);
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}