package com.allenbenny.sender;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private MaterialTextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;
    private ProgressBar myProgressBar;

    private List<Messages> messagesList = new ArrayList<>();
    private List<Messages> messages1 = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView userMessagesList;
    private MessageAdapter messageAdapter;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = null;
    private StorageTask uploadTask;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        IntaializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.round_person_24).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        DisplayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                            "Images", "PDF Files", "MS Word"
                };
                AlertDialog.Builder builder =  new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select any PDF"), 438);
                        }
                        if(which == 1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select any Image"), 438);
                        }
                        if(which == 2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select any File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void IntaializeControllers() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        userName = (MaterialTextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (MaterialTextView) findViewById(R.id.custom_profile_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        sendFilesButton = (ImageButton) findViewById(R.id.send_files_button);
        messageInputText = (EditText) findViewById(R.id.input_private_chat_message);
        myProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null){

            myProgressBar.setVisibility(View.VISIBLE);

            fileUri = data.getData();

            if (!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String downloadUrl = uri.toString();

                                    Map messageDocumentBody = new HashMap();
                                    messageDocumentBody.put("message", downloadUrl);
                                    messageDocumentBody.put("name", fileUri.getLastPathSegment());
                                    messageDocumentBody.put("type", checker);
                                    messageDocumentBody.put("from", messageSenderID);
                                    messageDocumentBody.put("to", messageReceiverID);
                                    messageDocumentBody.put("messageID", messagePushID);
                                    messageDocumentBody.put("time", saveCurrentTime);
                                    messageDocumentBody.put("date", saveCurrentDate);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageDocumentBody);
                                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageDocumentBody);

                                    RootRef.updateChildren(messageBodyDetails);
                                    myProgressBar.setVisibility(View.GONE);
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        myProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        myProgressBar.setProgress((int)p);
                    }
                });
            }
            else if (checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                myProgressBar.isIndeterminate();


                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){

                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri>  task) {

                        if (task.isSuccessful()){

                            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messagePictureBody = new HashMap();
                            messagePictureBody.put("message", myUrl);
                            messagePictureBody.put("name", fileUri.getLastPathSegment());
                            messagePictureBody.put("type", checker);
                            messagePictureBody.put("from", messageSenderID);
                            messagePictureBody.put("to", messageReceiverID);
                            messagePictureBody.put("messageID", messagePushID);
                            messagePictureBody.put("time", saveCurrentTime);
                            messagePictureBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messagePictureBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messagePictureBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){
                                        myProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(ChatActivity.this, "Message Sent !", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        myProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            else {
                myProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Nothing Selected, Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen(){

        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("userState").hasChild("state")){

                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("Online")){

                        userLastSeen.setText("Online");
                    }
                    else if(state.equals("Offline")){

                        userLastSeen.setText("Last Seen: " + date + ", " + time );
                    }
                }
                else {

                    userLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        final int[] count = {0};

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                Messages messages = dataSnapshot.getValue(Messages.class);

                    messages1.add(messages);


                Log.i("Test", "onChildAdded: " + messages.getMessage());

                    messagesList.clear();
                    messagesList.addAll(messages1);

                count[0]++;
                //Toast.makeText(ChatActivity.this, "Count: " + count[0], Toast.LENGTH_SHORT).show();
                messageAdapter.notifyDataSetChanged();


                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                messagesList.clear();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        messagesList.clear();
        messages1.clear();
        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }
    private void SendMessage(){

        String messageText = messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "First write Something !", Toast.LENGTH_SHORT).show();
        }
        else {

            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){

                        Toast.makeText(ChatActivity.this, "Message Sent !", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }
    }

}
