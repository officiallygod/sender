package com.allenbenny.sender;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter (List<Messages> userMessagesList){

        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public MaterialTextView senderMessageText, receiverMessageText, senderMessageTextTime, receiverMessageTextTime;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public RelativeLayout messageSenderPictureBox;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText =(MaterialTextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText =(MaterialTextView) itemView.findViewById(R.id.receiver_message_text);
            senderMessageTextTime =(MaterialTextView) itemView.findViewById(R.id.sender_message_text_time);
            receiverMessageTextTime =(MaterialTextView) itemView.findViewById(R.id.receiver_message_text_time);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            messageSenderPictureBox = (RelativeLayout) itemView.findViewById(R.id.message_sender_image_view_box);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);

        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        final String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String messageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")) {

                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.round_person_black_24).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.senderMessageTextTime.setVisibility(View.GONE);
        holder.receiverMessageTextTime.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageSenderPictureBox.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (messageType.equals("text")) {

            if(fromUserID.equals(messageSenderId)){

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageTextTime.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageTextTime.setText(messages.getTime());
            }
            else {

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageTextTime.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverMessageTextTime.setText(messages.getTime());
            }
        }
        else if (messageType.equals("image")) {

            if(fromUserID.equals(messageSenderId)){
                holder.messageSenderPictureBox.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                //holder.senderMessageTextTime.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
                //holder.senderMessageTextTime.setText(messages.getTime());

            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                //holder.receiverMessageTextTime.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
                //holder.receiverMessageTextTime.setText(messages.getTime());

            }
        }
        else {

            if(fromUserID.equals(messageSenderId)){

                holder.messageSenderPictureBox.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/sender-messaging.appspot.com/o/Image%20Files%2Finterface.png?alt=media&token=934ba0de-1517-4a23-a88a-092d843f116d").into(holder.messageSenderPicture);

            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/sender-messaging.appspot.com/o/Image%20Files%2Finterface.png?alt=media&token=934ba0de-1517-4a23-a88a-092d843f116d").into(holder.messageReceiverPicture);


            }

            holder.receiverMessageText.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.GONE);
            holder.senderMessageText.setVisibility(View.GONE);
            holder.senderMessageTextTime.setVisibility(View.GONE);
            holder.receiverMessageTextTime.setVisibility(View.GONE);
            holder.messageSenderPictureBox.setVisibility(View.GONE);
            holder.messageSenderPicture.setVisibility(View.GONE);
            holder.messageReceiverPicture.setVisibility(View.GONE);
        }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


            if (messages.getFrom().equals(messageSenderId)){

                if((userMessagesList.get(position).getType().equals("pdf")) || (userMessagesList.get(position).getType().equals("docx"))){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "Download and View This Document",
                            "Delete for Everyone",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteSentMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1){

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 2){
                                DeleteMessagesForEveryone(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });

                    builder.show();
                }
                else if(userMessagesList.get(position).getType().equals("text")){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "Delete for Everyone",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteSentMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1){
                                DeleteMessagesForEveryone(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });

                    builder.show();
                }

                else if(userMessagesList.get(position).getType().equals("image")){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "View this Image",
                            "Delete for Everyone",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteSentMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1){

                                Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                intent.putExtra("url", userMessagesList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 2){
                                DeleteMessagesForEveryone(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });

                    builder.show();
                }
            }

            else {

                if((userMessagesList.get(position).getType().equals("pdf")) || (userMessagesList.get(position).getType().equals("docx"))){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "Download and View This Document",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteReceiveMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1){

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                holder.itemView.getContext().startActivity(intent);
                            }


                        }
                    });

                    builder.show();
                }
                else if(userMessagesList.get(position).getType().equals("text")){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteReceiveMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }

                        }
                    });

                    builder.show();
                }

                else if(userMessagesList.get(position).getType().equals("image")){

                    CharSequence options[] = new CharSequence[]{

                            "Delete for Me",
                            "View this Image",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message ?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){
                                DeleteReceiveMessages(position, holder);
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1){

                                Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                intent.putExtra("url", userMessagesList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            }

                        }
                    });

                    builder.show();
                }
            }
                }
            });
        }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void DeleteSentMessages(final int position, final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(holder.itemView.getContext(), "Error Deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteReceiveMessages(final int position, final MessageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(holder.itemView.getContext(), "Error Deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteMessagesForEveryone(final int position, final MessageViewHolder holder){

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully for Everyone", Toast.LENGTH_SHORT).show();
                            }
                            else {

                                Toast.makeText(holder.itemView.getContext(), "Error Deleting", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {

                    Toast.makeText(holder.itemView.getContext(), "Error Deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
