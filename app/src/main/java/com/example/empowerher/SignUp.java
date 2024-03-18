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
import java.util.Map;

public class SignUp extends AppCompatActivity {

    EditText editTextName;
    EditText editTextEmailAddress;
    EditText editTextPhone;
    EditText editTextPassword;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextName = findViewById(R.id.editTextName);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);

        sessionManager = new SessionManager(this);

        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        String name = editTextName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();
        String email = editTextEmailAddress.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("phone_number", phoneNumber);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://10.0.2.2:3000/api/signup";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        String message = response.getString("message");
                        Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();

                        if (message.equals("User registered successfully")) {
                            Intent intent = new Intent(SignUp.this, LogIn.class);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SignUp.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(SignUp.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                // Capture the cookies from headers
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                if (rawCookies != null) {
                    sessionManager.saveSessionCookie(rawCookies.split(";", 2)[0]);
                }
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}
