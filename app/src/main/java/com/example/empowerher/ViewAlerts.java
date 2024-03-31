package com.example.empowerher;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewAlerts extends AppCompatActivity {

    private ListView alertsListView;
    private ArrayList<String> alertsList;
    private ArrayAdapter<String> adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_alerts);

        sessionManager = new SessionManager(this);
        alertsListView = findViewById(R.id.alertsListView);
        alertsList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alertsList);
        alertsListView.setAdapter(adapter);

        // Check if user is logged in (session cookie is present)
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
        } else {
            fetchAlerts();
        }
    }

    private void fetchAlerts() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://ec2-13-126-93-228.ap-south-1.compute.amazonaws.com:3000/api/alerts";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                this::onAlertsReceived, this::onError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Include the session cookie in the request headers
                if (sessionManager.isLoggedIn()) {
                    String cookie = sessionManager.getSessionCookie();
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };

        queue.add(stringRequest);
    }

    private void onAlertsReceived(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String alertMessage = jsonObject.getString("friendName") + " needs your help.";
                alertsList.add(alertMessage);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ViewAlerts.this, "Error parsing alerts", Toast.LENGTH_SHORT).show();
        }
    }

    private void onError(VolleyError error) {
        String errorMessage = "Failed to retrieve alerts. Please try again later.";
        if (error instanceof AuthFailureError) {
            errorMessage = "Authentication failure. Please log in again.";
            redirectToLogin();
        } else if (error instanceof NoConnectionError) {
            errorMessage = "Cannot connect to the internet. Please check your connection and try again.";
        } else if (error instanceof TimeoutError) {
            errorMessage = "Connection timed out. Please try again.";
        } else if (error instanceof ServerError) {
            errorMessage = "Server error. Please try again later or contact support.";
        } else if (error instanceof NetworkError) {
            errorMessage = "Network error. Please check your connection and try again.";
        } else if (error instanceof ParseError) {
            errorMessage = "Error parsing data. Please try again or contact support.";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e("ViewAlerts", "Volley error: " + error.toString());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ViewAlerts.this, LogIn.class); // Replace LoginActivity with your login activity class
        startActivity(intent);
        finish();
    }
}
