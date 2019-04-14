package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        FriendlyMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            photoImageView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
           StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            //StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/friendlychat-5f2b1.appspot.com/o/chat_photos%2F148449?alt=media&token=83b47e04-19b4-4e5e-8ff8-0500b2b25889");
            Log.i("infoinadapter","going to add using glide");
// ImageView in your Activity



// Download directly from StorageReference using Glide
// (See MyAppGlideModule for Loader registration)

            Glide.with(photoImageView.getContext())
                    .load(storageReference)
                    .into(photoImageView);
            Log.i("infoinadapter","executed glide...");
          /* Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);*/
            //Picasso.with(photoImageView.getContext()).load(message.getPhotoUrl()).into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        return convertView;
    }
}
