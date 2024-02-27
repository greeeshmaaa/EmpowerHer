// LogIn.java
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

public class LogIn extends AppCompatActivity {

    EditText editTextEmail2;
    EditText editTextPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        editTextEmail2 = findViewById(R.id.editTextEmail2);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordlogin);

        TextView textViewLogIn = findViewById(R.id.textViewLogIn);
        textViewLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
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

        String url = "http://10.0.2.2:3000/api/login"; // Replace with your server URL

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            Toast.makeText(LogIn.this, message, Toast.LENGTH_SHORT).show();

                            // If login is successful, navigate to the home page
                            if (message.equals("Login successful")) {
                                Intent intent = new Intent(LogIn.this, Home.class);
                                startActivity(intent);
                                finish(); // Close login activity
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LogIn.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LogIn.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}