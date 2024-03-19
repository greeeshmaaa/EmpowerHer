package com.example.empowerher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Emergency_contact extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        sessionManager = new SessionManager(this);
        Button addFriendButton = findViewById(R.id.addFriendButton);
        Button viewPendingRequestsButton = findViewById(R.id.button);

        addFriendButton.setOnClickListener(view -> showAddFriendDialog());
        viewPendingRequestsButton.setOnClickListener(this::fetchPendingFriendRequests);
    }

    private void showAddFriendDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_send_friend_request, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
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
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendFriendRequest(String receiverEmail) {
        String url = "http://10.0.2.2:3000/api/friend-request";
        Map<String, String> params = new HashMap<>();
        params.put("receiver_email", receiverEmail);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "Friend request sent.", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String cookie = sessionManager.getSessionCookie();
                if (cookie != null) {
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    private void fetchPendingFriendRequests(View view) {
        String url = "http://10.0.2.2:3000/api/friend-requests/received";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                this::processPendingFriendRequestsResponse,
                error -> Toast.makeText(this, "Error fetching friend requests: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String cookie = sessionManager.getSessionCookie();
                if (cookie != null && !cookie.isEmpty()) {
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void processPendingFriendRequestsResponse(String response) {
        try {
            JSONArray friendRequests = new JSONArray(response);
            if (friendRequests.length() == 0) {
                Toast.makeText(this, "No pending friend requests", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pending_requests, null);
            builder.setView(dialogView);

            LinearLayout llPendingRequestsContainer = dialogView.findViewById(R.id.llPendingRequestsContainer);
            for (int i = 0; i < friendRequests.length(); i++) {
                JSONObject request = friendRequests.getJSONObject(i);
                View requestView = LayoutInflater.from(this).inflate(R.layout.item_pending_request, llPendingRequestsContainer, false);

                TextView tvSenderEmail = requestView.findViewById(R.id.tvSenderEmail);
                // Use "name" instead of "username"
                TextView tvSenderUsername = requestView.findViewById(R.id.tvSenderUsername);
                Button btnAccept = requestView.findViewById(R.id.btnAccept);

                String email = request.getString("email");
                // Correctly use "name" to match your backend response
                String name = request.optString("name", "Unknown");
                String requestId = request.getString("id");

                tvSenderEmail.setText(String.format("Email: %s", email));
                tvSenderUsername.setText(String.format("Username: %s", name));
                btnAccept.setOnClickListener(v -> acceptFriendRequest(requestId, requestView, llPendingRequestsContainer));

                llPendingRequestsContainer.addView(requestView);
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (JSONException e) {
            Toast.makeText(this, "Error processing friend requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void acceptFriendRequest(String requestId, View requestView, LinearLayout container) {
        String url = "http://10.0.2.2:3000/api/friend-requests/accept/" + requestId;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Friend request accepted.", Toast.LENGTH_SHORT).show();
                    container.removeView(requestView); // Remove the accepted request view
                },
                error -> Toast.makeText(this, "Failed to accept friend request.", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String cookie = sessionManager.getSessionCookie();
                if (cookie != null && !cookie.isEmpty()) {
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }
}
