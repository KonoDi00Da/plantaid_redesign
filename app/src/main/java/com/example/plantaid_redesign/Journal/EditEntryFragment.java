package com.example.plantaid_redesign.Journal;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.example.plantaid_redesign.Utilities.DateUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class EditEntryFragment extends Fragment implements BackpressedListener {
    private static final String TAG = "Journal";
    private RelativeLayout statusBar;
    private ImageButton btnBack;
    private ImageView imgJournalPhoto;
    private TextInputLayout txtTitleLayout, txtContentLayout;
    private TextInputEditText txtTitle, txtContent;
    private FloatingActionButton fabOpenGallery, fabTakePicture;
    private Button btnSave;
    private ExtendedFloatingActionButton extFabUpload;
    private TextView txtTake, txtOpen;
    Boolean isAllFabsVisible;

    private NavController navController;
    private NavOptions navOptions;

    private Uri contentURI, photoURI;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private LoadingDialog dialog;

    private String id, mode;
    private String photoLink = "";
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int IMAGE_PICK_CODE = 1000;
    private final int REQUEST_WRITE_STORAGE = 2;
    private String currentPhotoPath;
    private String imageFileName;

    private DateUtils dateUtils;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            statusBar = view.findViewById(R.id.statusBar);
            btnBack = view.findViewById(R.id.btnBack);
            imgJournalPhoto = view.findViewById(R.id.imgJournalContent);
            txtTitleLayout = view.findViewById(R.id.txtTitleLayout);
            txtContentLayout = view.findViewById(R.id.txtContentLayout);
            txtTitle = view.findViewById(R.id.txtTitle);
            txtContent = view.findViewById(R.id.txtContent);
            extFabUpload = view.findViewById(R.id.fabUpload);
            btnSave = view.findViewById(R.id.btnSave);
            fabOpenGallery = view.findViewById(R.id.fabOpenGallery);
            fabTakePicture = view.findViewById(R.id.fabTakePicture);
            txtOpen = view.findViewById(R.id.txtOpen);
            txtTake = view.findViewById(R.id.txtTake);
            navController = Navigation.findNavController(view);

            dateUtils = new DateUtils();

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            reference = database.getReference().child("journal").child(currentUser.getUid());

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference().child(currentUser.getUid()).child("images");

            dialog = new LoadingDialog(getActivity());

            bundle();
            extendedFab();
            back();
            save();
        } catch (Exception e){
            Log.e(TAG, "onViewCreated: ", e);
            toast("Something went wrong");
        }
    }

    private void bundle() {
        Bundle args = getArguments();
        if (args != null && args.getString("id") != null) {
            id = args.getString("id");
            showEntry(id);
        }

        if(args != null && args.getString("mode") != null){
            mode = args.getString("mode");
            switch (mode){
                case "new entry":
                    //newEntry();
                    btnSave.setText("Save");
                    break;
                case "update entry":
                    //updateEntry();
                    btnSave.setText("Update");
                    break;
            }
        }
    }

    private void extendedFab() {
        try {
            isAllFabsVisible = false;
            extFabUpload.shrink();

            extFabUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isAllFabsVisible) {
                        // when isAllFabsVisible becomes true make all the action name texts and FABs VISIBLE.
                        fabTakePicture.show();
                        fabOpenGallery.show();
                        txtTake.setVisibility(View.VISIBLE);
                        txtOpen.setVisibility(View.VISIBLE);
                        // Now extend the parent FAB, as user clicks on the shrinked parent FAB
                        extFabUpload.extend();
                        // make the boolean variable true as we have set the sub FABs visibility to GONE
                        isAllFabsVisible = true;
                    } else {
                        // when isAllFabsVisible becomes true make all the action name texts and FABs GONE.
                        fabTakePicture.hide();
                        fabOpenGallery.hide();
                        txtTake.setVisibility(View.GONE);
                        txtOpen.setVisibility(View.GONE);
                        // Set the FAB to shrink after user closes all the sub FABs
                        extFabUpload.shrink();
                        // make the boolean variable false as we have set the sub FABs visibility to GONE
                        isAllFabsVisible = false;
                    }
                }
            });

            fabOpenGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(checkGalleryPermissions()){
                            openGallery();
                        }else{
                            requestGalleryPermission();
                        }
                    } catch (Exception e){
                        Log.e(TAG, "onClick: ", e);
                        toast("Something went wrong");
                    }
                }
            });

            fabTakePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(checkCamPermissions()){
                            takePicture();
                        }else{
                            requestCamPermission();
                        }
                    } catch (Exception e){
                        Log.e(TAG, "onClick: ", e);
                        toast("Something went wrong");
                    }
                }
            });
        } catch (Exception e){
            Log.e(TAG, "extendeFab: ", e);
            toast("Something went wrong");
        }
    }

    private void save() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String content = txtContent.getText().toString();
                    if (txtTitle.getText().toString().isEmpty() || txtContent.getText().toString().isEmpty()) {
                        txtTitleLayout.setError("Required");
                        txtContentLayout.setError("Required");
                    } else if(contentURI == null) {
                        txtTitleLayout.setError(null);
                        txtContentLayout.setError(null);
                        toast("No Image Uploaded");
                    } else {
                        txtTitleLayout.setError(null);
                        txtContentLayout.setError(null);

                        switch (mode){
                            case "new entry":
                                newEntry();
                                break;
                            case "update entry":
                                updateEntry();
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onClick: ", e);
                    toast("Something went wrong");
                }
            }
        });
    }

    private void newEntry(){
        try {
            if(contentURI != null){
                dialog.startLoading("Uploading...");
                imageFileName = UUID.randomUUID().toString() + "." + getFileExtension(contentURI);
                StorageReference ref = storageReference.child(imageFileName);
                ref.putFile(contentURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        toast("Image Uploaded");
                        dialog.stopLoading();
                        String content = txtContent.getText().toString();

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String id3 = reference.push().getKey();
                                Journal journal = new Journal(
                                        id3,
                                        txtTitle.getText().toString(),
                                        txtContent.getText().toString(),
                                        uri.toString(),
                                        "unlike",
                                        dateUtils.getCurrentDate(),
                                        dateUtils.getCurrentTime()
                                );

                                reference.child(id3).setValue(journal);
                                txtTitle.setText("");
                                txtContent.setText("");
                                contentURI = null;
                                //imgJournalPhoto.setBackgroundResource(R.drawable.splash_bg);

                                id = id3;

                                Bundle bundle = new Bundle();
                                bundle.putString("id", id);
                                bundle.putBoolean("save_entry", true);
                                bundle.putString("user_msg", content);
                                navController.navigate(R.id.action_editEntryFragment_to_viewEntryFragment, bundle);

                                //((HomeActivity) getActivity()).sendMessage(content);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Image not uploaded", Toast.LENGTH_SHORT).show();
                        dialog.stopLoading();
                        Log.e("Upload image", "Error ", e);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                        double progress
                                = (100.0
                                * taskSnapshot.getBytesTransferred()
                                / taskSnapshot.getTotalByteCount());
                        dialog.onProgress("Uploaded " + (int)progress + "%");
                    }
                });
            } else {
                toast("No photo included");
            }
        } catch (Exception e){
            Log.e("upload_error", "Message: ", e);
            toast("Please wait...");
        }
    }

    private void updateEntry() {
        DatabaseReference reference1 = reference.child(id);
        try {
            if(contentURI != null){
                dialog.startLoading("Uploading...");
                imageFileName = UUID.randomUUID().toString() + "." +getFileExtension(contentURI);
                StorageReference ref = storageReference.child(imageFileName);
                ref.putFile(contentURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        toast("Image Uploaded");
                        dialog.stopLoading();
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Journal journal = new Journal(
                                        id,
                                        txtTitle.getText().toString(),
                                        txtContent.getText().toString(),
                                        uri.toString(),
                                        "unlike",
                                        dateUtils.getCurrentDate(),
                                        dateUtils.getCurrentTime()
                                );

                                reference1.setValue(journal);
                                txtTitle.setText("");
                                txtContent.setText("");
                                contentURI = null;
                                //imgJournalPhoto.setBackgroundResource(R.drawable.splash_bg);

                                Bundle bundle = new Bundle();
                                bundle.putString("id", id);
                                navController.navigate(R.id.action_editEntryFragment_to_viewEntryFragment, bundle);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Image not uploaded", Toast.LENGTH_SHORT).show();
                        dialog.stopLoading();
                        Log.e("Upload image", "Error ", e);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress
                                = (100.0
                                * taskSnapshot.getBytesTransferred()
                                / taskSnapshot.getTotalByteCount());
                        dialog.onProgress("Uploaded " + (int)progress + "%");
                    }
                });
            } else {
                toast("No photo included");
            }
        } catch (Exception e){
            Log.e("upload_error", "Message: ", e);
            toast("Please wait...");
        }
    }

    private void showEntry(String id) {
        try {
            DatabaseReference reference1 = reference.child(id);
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Journal journal = dataSnapshot.getValue(Journal.class);
                    if (journal != null){
                        photoLink = journal.photo;
                        txtTitle.setText(journal.title);
                        txtContent.setText(journal.content);
                        if(getActivity() != null){
                            Glide.with(getActivity()).load(photoLink).into(imgJournalPhoto);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            Log.e(TAG, "showEntry: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK
                    && data != null && data.getData() != null){
                contentURI = data.getData();
                Glide.with(EditEntryFragment.this).load(contentURI).into(imgJournalPhoto);
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                contentURI = photoURI;
                //imgJournalPhoto.setImageBitmap(setPic());
                //Picasso.get().load(contentURI).into(uploadImage);
                Glide.with(EditEntryFragment.this).load(contentURI).into(imgJournalPhoto);
            }

            extFabUpload.shrink();
            fabTakePicture.hide();
            fabOpenGallery.hide();
            txtTake.setVisibility(View.GONE);
            txtOpen.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: ", e);
            toast("Something went wrong, please try again");
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraPermission){
                        toast("Permissions Granted..");
                        takePicture();
                    }else{
                        toast("Permissions denied..");
                    }
                }
            }
            case IMAGE_PICK_CODE: {
                if (grantResults.length > 0) {
                    boolean galleryPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(galleryPermission){
                        toast("Permissions Granted..");
                        openGallery();
                    }else{
                        toast("Permissions denied..");
                    }
                }
            }
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0) {
                    boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storagePermission){
                        toast("Permissions Granted..");
                    }else{
                        toast("Permissions denied..");
                    }
                }
            }
        }
    }

    private void takePicture() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if(checkStoragePermissions()){
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    File compressedFile = null;
                    try {
                        photoFile = createImageFile();
                        //compressedFile = new Compressor(getActivity()).compressToFile(photoFile);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.example.plantaid_redesign",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
            else{
                requestStoragePermission();
            }
        } catch (Exception e){
            Log.e(TAG, "takePicture: ", e);
            toast("Something went wrong");
        }
    }

    private boolean checkGalleryPermissions(){
        int camPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return camPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void requestGalleryPermission(){
        int PERMISSION_CODE = 1001;
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    private boolean checkCamPermissions(){
        int camPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        return camPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCamPermission(){
        int PERMISSION_CODE = 200;
        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
    }

    private boolean checkStoragePermissions(){
        int camPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return camPermission == PackageManager.PERMISSION_GRANTED;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGES_" + timeStamp + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void requestStoragePermission(){
        int PERMISSION_CODE = 201;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    private Bitmap setPic() {
        // Get the dimensions of the View
        int targetW = imgJournalPhoto.getWidth();
        int targetH = imgJournalPhoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        return bitmap;
    }

    private void back() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navBack();
            }
        });
    }

    private void navBack() {
        try {
            switch (mode){
                case "new entry":
                    navController.navigate(R.id.action_editEntryFragment_to_journalFragment, null);
                    break;
                case "update entry":
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    navController.navigate(R.id.action_editEntryFragment_to_viewEntryFragment, bundle);
                    break;
            }

        } catch (Exception e){
            Log.e(TAG, "onClick: ", e);
            toast("Something went wrong");
        }
    }

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        navBack();
    }

    public static BackpressedListener backpressedlistener;

    @Override
    public void onPause() {
        backpressedlistener = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        backpressedlistener = this;
    }
}