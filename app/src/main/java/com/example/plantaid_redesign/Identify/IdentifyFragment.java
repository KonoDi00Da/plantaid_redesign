package com.example.plantaid_redesign.Identify;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.PlantIdentifiedModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IdentifyFragment extends Fragment implements BackpressedListener {
    private static final String TAG = "Identify";
    public static final String IMG_URL = "img_url";
    private LoadingDialog loadingDialog;
    private Dialog dialog;
    private NavController navController;
    private ImageView imgSearch;
    private CardView cardOptions;
    private ConstraintLayout darkOverlay;


    private Button btnGallery, btnCamera, btnHistory;
    private ImageView imgView;
    private Bitmap imageBitmap;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int IMAGE_PICK_CODE = 1000;
    private final int REQUEST_WRITE_STORAGE = 2;
    public String u_plantID; // unique plant id
    private Uri contentUri;
    private String url;
    private String currentPhotoPath;
    private Uri photoURI;
    private Bitmap galleryImage;

    private Handler handler;
    private Runnable cancelHandler, cancelHandler2, cancelHandler3;

    private Uri imageURI;
    private Bitmap bitmap, bitmapReduced;
    String imageFileName;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;
    DatabaseReference userRef;
    String currentTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((Home)getActivity()).updateStatusBarColor("#485E0D");
        return inflater.inflate(R.layout.fragment_identify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnHistory = view.findViewById(R.id.btnHistory);
        imgView = view.findViewById(R.id.img_plant);
        handler = new Handler();
        cardOptions = view.findViewById(R.id.cardOptions);
        darkOverlay = view.findViewById(R.id.darkOverlay);

        loadingDialog = new LoadingDialog(getActivity());
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.card_identify_organ);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        firebaseStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users").child(currentUser.getUid());

        try{
            btnHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Home)getActivity()).hideBottomNavigation(true);
                    navController.navigate(R.id.action_identifyFragment_to_identifyHistoryFragment3);
                }
            });

            btnCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkCamPermissions()){
                        takePicture();
                    }else{
                        requestCamPermission();
                    }
                }
            });

            btnGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkGalleryPermissions()){
                        openGallery();
                    }else{
                        requestGalleryPermission();
                    }
                }
            });


//            btnIdentify.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    identifyPlant();
//                }
//            });
        }catch (Exception e){
            Log.e(TAG, "tab", e);
        }
    }

    public void identifyOrgan(){



        ImageView imageView3 = dialog.findViewById(R.id.imageView3);
        Button btnLeaf, btnFlower, btnFruit, btnBark;
        btnLeaf = dialog.findViewById(R.id.btnLeaf);
        btnFlower = dialog.findViewById(R.id.btnFlower);
        btnFruit = dialog.findViewById(R.id.btnFruit);
        btnBark = dialog.findViewById(R.id.btnBark);

        FloatingActionButton btn_back = dialog.findViewById(R.id.btn_back);
        imageView3.setImageURI(contentUri);
        imageView3.setImageBitmap(setPic());
        imageView3.setImageBitmap(galleryImage);


        String value = contentUri.toString();

        IdentifyResultsFragment identifyResultsFragment = new IdentifyResultsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("pushKey", u_plantID);
        bundle.putString("serviceUrl", url);
        bundle.putString("imgPic", value);
        bundle.putString("imagePath", imageFileName);
        //identifyResultsFragment.setArguments(bundle);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImageFromStorage(imageFileName);
                dialog.dismiss();
            }
        });

        btnLeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("plantOrgan", "leaf");
                navController.navigate(R.id.action_identifyFragment_to_identifyResultsFragment,bundle);
                ((Home)getActivity()).hideBottomNavigation(true);
                dialog.dismiss();

            }
        });

        btnFlower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("plantOrgan", "flower");
                navController.navigate(R.id.action_identifyFragment_to_identifyResultsFragment,bundle);
                ((Home)getActivity()).hideBottomNavigation(true);
                dialog.dismiss();

            }
        });

        btnFruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("plantOrgan", "fruit");
                navController.navigate(R.id.action_identifyFragment_to_identifyResultsFragment,bundle);
                ((Home)getActivity()).hideBottomNavigation(true);
                dialog.dismiss();

            }
        });

        btnBark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("plantOrgan", "bark");
                navController.navigate(R.id.action_identifyFragment_to_identifyResultsFragment,bundle);
                ((Home)getActivity()).hideBottomNavigation(true);
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    public void deleteImageFromStorage(String name) {
        // Create a reference to the image file
        String userID = userRef.toString();
        StorageReference reference = firebaseStorage.getReference().child(userID).child(name);

        // Delete the image file
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Image deleted successfully
                        toast("Plant identification cancelled");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while deleting the image
                        toast("Failed to delete image: " + e.getMessage());
                    }
                });
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

    private void requestStoragePermission(){
        int PERMISSION_CODE = 201;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    private boolean checkGalleryPermissions(){
        int camPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return camPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGalleryPermission(){
        int PERMISSION_CODE = 1001;
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    @Override
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

    // when allowed app permissions
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            File f = new File(currentPhotoPath);
            contentUri = photoURI;
            try {
                galleryImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            imgView.setImageBitmap(setPic());
//            saveImage(setPic());
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "IMAGE_" + timeStamp + "." + getFileExt(contentUri);

            uploadImageToFirebase(imageFileName, contentUri);
            //imgView.setImageURI(Uri.fromFile(f));

        }
        if(requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK){
            contentUri = data.getData();
//            imgView.setImageURI(contentUri);
            try {
                galleryImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "IMAGE_" + timeStamp + "." + getFileExt(contentUri);
            uploadImageToFirebase(imageFileName, contentUri);

        }
        if(requestCode == REQUEST_WRITE_STORAGE && resultCode == Activity.RESULT_OK){
            saveImage(imageBitmap);
        }
    }

    //for file extension
    private String getFileExt(Uri contentUri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(contentUri));
    }

    //upload to firebase
    private void uploadImageToFirebase(String name, Uri uri){
        try{
            loadingDialog.startLoading("Please wait");
            String userID = userRef.toString();
            StorageReference reference = firebaseStorage.getReference().child(userID).child(name);

            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            PlantIdentifiedModel model = new PlantIdentifiedModel();
                            model.setIdImage(uri.toString()+currentTime);
                            u_plantID = "1001";
                            userRef.child("plantIdentification").child(u_plantID).setValue(model);
                            url = model.getIdImage();
                            loadingDialog.stopLoading();
//                            toast("Plant added successfully");
                                identifyOrgan();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.stopLoading();
                            toast("Add item fail");
                        }
                    });
                }
            });
        }catch(Exception e){
            Log.e("IdentifyPlant", "uploadingImage", e);
        }

    }

    // creating root img file  (only for the application and cannot be seen in gallery)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGES_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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

    //camera intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if(checkStoragePermissions()){
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
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
    }

    // the bitmap displayed in the imageView
    private Bitmap setPic() {
        // Get the dimensions of the View
        int targetW = imgView.getWidth();
        int targetH = imgView.getHeight();

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

    //optional but save image in gallery (madodoble if ginamit tho)
    private void saveImage(Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentValues values = contentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, getActivity().getContentResolver().openOutputStream(uri));
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    getActivity().getContentResolver().update(uri, values, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));

            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA , file.getAbsolutePath());
                getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    // used with saveImage
    private ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }

    // used with saveImage
    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void takePicture() {
        dispatchTakePictureIntent();
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void toast(String msg ){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        ((Home) getActivity()).home();
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