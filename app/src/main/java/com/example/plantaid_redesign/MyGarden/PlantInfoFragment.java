package com.example.plantaid_redesign.MyGarden;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Model.AllPlantsList;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlantInfoFragment extends Fragment {
    public String key;
    private Button btnRemove;
    private TextView commonNameTextView, sciNameTextView, descriptionTextView, txtWater, txtHarvest, txtCare, txtPestsDiseases;
    private WebView webView;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private String VideoEmbededAdress;
    private final String mimeType = "text/html";
    private final String encoding = "UTF-8";//"base64";

    private String userKey;
    private String plantKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plant_info, container, false);
        UserMyGardenPlantsActivity activity = (UserMyGardenPlantsActivity) getActivity();
        plantKey = activity.getPlantKey();
        userKey = activity.getUserKey();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {

            btnRemove = view.findViewById(R.id.btnRemove);
            BounceView.addAnimTo(btnRemove);
            commonNameTextView = view.findViewById(R.id.com_plant);
            sciNameTextView = view.findViewById(R.id.sci_plant);
            descriptionTextView = view.findViewById(R.id.plant_desc);
            txtWater = view.findViewById(R.id.txtWaterDays);
            txtHarvest = view.findViewById(R.id.txtGrowthDays);
            txtCare = view.findViewById(R.id.txtSeasonDays);
            txtPestsDiseases = view.findViewById(R.id.txtPestsDisease);
            webView = view.findViewById(R.id.ytLink);

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });

            DatabaseReference reference = database.getReference("Plants").child(plantKey);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    AllPlantsList model = snapshot.getValue(AllPlantsList.class);
                    if (model != null){
                        commonNameTextView.setText(model.getCommonName());
                        sciNameTextView.setText(model.getSciName());
                        descriptionTextView.setText(model.getDescription());
                        txtWater.setText(model.getWater());
                        txtHarvest.setText(model.getHarvest());
                        txtCare.setText(model.getCare());
                        txtPestsDiseases.setText(model.getPestsDiseases());
                        youtubeView(model.getYtLink());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } catch (Exception e) {
            Log.v("TEST", "Error: " + e.toString());
        }
    }
    private void showDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.cardview_remove_plant);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlant();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                Toast.makeText(MainActivity.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        BounceView.addAnimTo(dialog);
        dialog.show();
    }

    private void deletePlant() {
        DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String plantID = "myGarden";
                userRef.child(plantID).child(userKey).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        toast("Plant Deleted");

        Intent intent = new Intent(getActivity(), Home.class);
        intent.putExtra("fragment_id", 1);
        startActivity(intent);
    }

    private void youtubeView(String s){
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getActivity()))
                .build();

        String ytEmbedKey = s;
        VideoEmbededAdress = "<iframe width=\"350\" height=\"225\" src=\"https://www.youtube.com/embed/"+ ytEmbedKey +"\" title=\"YouTube video player\"allow=\"autoplay;\" allowfullscreen></iframe>";

        webView.setWebViewClient(new WebViewClient() {
            private WebView view;
            private WebResourceRequest request;

            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              WebResourceRequest request) {
                this.view = view;
                this.request = request;
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//            webView.getSettings().setUserAgentString(USERAGENT);//Important to auto play video
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(VideoEmbededAdress);
        webView.loadDataWithBaseURL("", VideoEmbededAdress, mimeType, encoding, "");

    }
    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
