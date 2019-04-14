/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    //rc sign-in assigned an arbitrary code
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER =  2;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;
//firebase instances
    private FirebaseDatabase mFirebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mMessagesDatabaseReference=mFirebaseDatabase.getReference("message");
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

   // private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);


        mUsername = "VARSHINI";

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
       mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter

        mMessageEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        final List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send

        mMessageEditText.addTextChangedListener(new TextWatcher() {

            //@Override

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                return false;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        //FriendlyMessage friendlyMessage;
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input boxs
                if (mMessageEditText.length() > 0) {
                    mMessageEditText.getText().clear();
                }

            }
        });

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
     startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                //onActivityResult(RC_PHOTO_PICKER,RESULT_OK,Intent.createChooser(intent, "Complete action using"));

            }
        });

mAuthStateListener=new FirebaseAuth.AuthStateListener() {
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
FirebaseUser user = firebaseAuth.getCurrentUser();
if(user!=null)
{
    //signed in
    Toast.makeText(MainActivity.this,"Your now signed in.Welcome to Friendly chat!!",Toast.LENGTH_SHORT).show();
    onSignedInInitialize(user.getDisplayName());

}
else{
    //signed out
    onSignedOutCleanup();
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),

            new AuthUI.IdpConfig.GoogleBuilder().build());




// Create and launch sign-in intent
    startActivityForResult(

            AuthUI.getInstance()

                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
            RC_SIGN_IN);



}
    }
};
    }
@Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(MainActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
            finish();
        } }else if(requestCode==RC_PHOTO_PICKER&&resultCode==RESULT_OK){
            Uri SelectedImageUri =data.getData();
            StorageReference photoRef=mChatPhotosStorageReference.child(SelectedImageUri.getLastPathSegment());
            //upload to storage
            photoRef.putFile(SelectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   // StorageReference taskSnapshotStorage = taskSnapshot.getStorage();
                   // Uri downloadUrl = taskSnapshotStorage.getDownloadUrl().getResult();
                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    FriendlyMessage friendlyMessage = new FriendlyMessage(null,mUsername,downloadUrl.toString());
                    mMessagesDatabaseReference.push().setValue(friendlyMessage);
                    //mMessageAdapter.
                   // ImageView photoImageView = (ImageView) View.findViewById(R.id.photoImageView);

Log.i("infoinmain","finished till push in main");
                    // Create a storage reference from our app
                   // StorageReference storageRef = storage.getReference();

// Create a reference with an initial file path and name
                   // StorageReference pathReference = storageRef.child("images/stars.jpg");

// Create a reference to a file from a Google Cloud Storage URI
                   // StorageReference gsReference = storage.getReferenceFromUrl();

// Create a reference from an HTTPS URL
// Note that in the URL, characters are URL escaped!
                    //FirebaseStorage storage = new
                  // StorageReference httpsReference = mFirebaseStorage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/friendlychat-5f2b1.appspot.com/o/chat_photos%2F148449?alt=media&token=83b47e04-19b4-4e5e-8ff8-0500b2b25889");



            }});

        }
    }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()){
                case R.id.sign_out_menu :
                //sign out
                    AuthUI.getInstance().signOut(this);
                             return true;
                default:
                    return super.onOptionsItemSelected(item);
        }

        }

        private void onSignedInInitialize(String username)
        {
            mUsername=username;
            attachDatabaseReadListener();
        }

        private void onSignedOutCleanup()
        {
            mUsername=ANONYMOUS;
            mMessageAdapter.clear();

        }
        private void attachDatabaseReadListener() {
            if (mChildEventListener == null) {
                mChildEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                        mMessageAdapter.add(friendlyMessage);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
                };

                mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
            }
        }
        private void detachDatabaseReadListener()
        {
            if(mChildEventListener!=null) {
                mMessagesDatabaseReference.removeEventListener(mChildEventListener);
                mChildEventListener = null;
            }
        }
    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

          @Override
    protected void onResume() {
        super.onResume();
        if(mAuthStateListener!=null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
detachDatabaseReadListener();
        mMessageAdapter.clear();
          }


}

 class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}