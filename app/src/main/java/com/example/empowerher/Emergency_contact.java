package com.example.empowerher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Emergency_contact extends AppCompatActivity {

    Button addFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        addFriendButton = findViewById(R.id.addFriendButton);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_send_friend_request, null);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Emergency_contact.this);
                dialogBuilder.setView(dialogView);

                final EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
                Button buttonSend = dialogView.findViewById(R.id.buttonSend);

                AlertDialog dialog = dialogBuilder.create();

                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Here, implement the logic to send the friend request
                        String email = editTextEmail.getText().toString();
                        // For example, use Retrofit or Volley to make the network request here

                        // Assuming you've implemented the function to send the request
                        // sendFriendRequest(email);

                        Toast.makeText(Emergency_contact.this, "Request sent to: " + email, Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Close the dialog
                    }
                });

                dialog.show();
            }
        });
    }
}
