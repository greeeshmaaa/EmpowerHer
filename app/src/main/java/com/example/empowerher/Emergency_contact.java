package com.example.empowerher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
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

public class Emergency_contact extends AppCompatActivity {

    private SessionManager sessionManager; // Declare SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        sessionManager = new SessionManager(this); // Initialize SessionManager

        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(view -> showAddFriendDialog());
    }

    private void showAddFriendDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_send_friend_request, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Emergency_contact.this);
        dialogBuilder.setView(dialogView);

        final EditText editTextEmail = dialogView.findViewById(R.id.receiverEmail);
        Button buttonSend = dialogView.findViewById(R.id.buttonSend);

        AlertDialog dialog = dialogBuilder.create();

        buttonSend.setOnClickListener(v -> {
            String receiverEmail = editTextEmail.getText().toString().trim();
            if (!receiverEmail.isEmpty()) {
                sendFriendRequest(receiverEmail);
                dialog.dismiss();
            } else {
                Toast.makeText(Emergency_contact.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendFriendRequest(String receiverEmail) {
        String url = "http://10.0.2.2:3000/api/friend-request";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("receiver_email", receiverEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> Toast.makeText(Emergency_contact.this, "Friend request sent.", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(Emergency_contact.this, "Failed to send friend request: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String cookie = sessionManager.getSessionCookie(); // Correct use of SessionManager to get the cookie
                if (cookie != null && !cookie.isEmpty()) {
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}
