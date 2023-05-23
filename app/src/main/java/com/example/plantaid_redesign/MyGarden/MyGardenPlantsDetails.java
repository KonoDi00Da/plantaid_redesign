package com.example.plantaid_redesign.MyGarden;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.webkit.WebViewAssetLoader;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Model.User_Plants;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MyGardenPlantsDetails extends Fragment implements BackpressedListener {
    public static final String TAG = "MyGarden";
    public String key;
    private FloatingActionButton btn_back;
    private NavController navController;
    private ImageView imgPlant;
    private TextView commonNameTextView, sciNameTextView, descriptionTextView, txtWater, txtHarvest, txtCare, txtPestsDiseases;
    private WebView webView;
    private String comPlant, sciPlant, image;

    private Button addToGardenBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private String VideoEmbededAdress, ytEmbedKey;
    private final String mimeType = "text/html";
    private final String encoding = "UTF-8";//"base64";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_garden_plants_details, container, false);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_back = view.findViewById(R.id.btn_back);
        navController = Navigation.findNavController(view);
        imgPlant = view.findViewById(R.id.imgPlant);
        commonNameTextView = view.findViewById(R.id.com_plant);
        sciNameTextView = view.findViewById(R.id.sci_plant);
        descriptionTextView = view.findViewById(R.id.plant_desc);
        txtWater = view.findViewById(R.id.txtWaterDays);
        txtHarvest = view.findViewById(R.id.txtGrowthDays);
        txtCare = view.findViewById(R.id.txtSeasonDays);
        txtPestsDiseases = view.findViewById(R.id.txtPestsDisease);
        webView = view.findViewById(R.id.ytLink);
        addToGardenBtn = view.findViewById(R.id.addToGardenBtn);
        BounceView.addAnimTo(addToGardenBtn);
        try {
            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();

            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_myGardenPlantsDetails_to_myGardenAllPlantsFragment);
                }
            });
            setBundleArgs();
            addPlantToFirebase();

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    private void addPlantToFirebase() {
        addToGardenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());
                userRef.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get date added
                        String plantID = "myGarden";
                        String userPlantKey = userRef.push().getKey();
                        User_Plants userPlants = new User_Plants(comPlant, sciPlant, image, key, userPlantKey);
                        userRef.child(plantID).child(userPlantKey).setValue(userPlants);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                toast("Plant added successfully");

                navController.navigate(R.id.action_myGardenPlantsDetails_to_myGardenFragment);
                ((Home)getActivity()).hideBottomNavigation(false);
            }
        });

    }

    public void setBundleArgs(){
        Bundle bundle = getArguments();
        if (bundle != null) {
            Picasso.get().load(bundle.getString("plant_image")).into(imgPlant);

            commonNameTextView.setText(bundle.getString("com_plant"));
            sciNameTextView.setText(bundle.getString("sci_plant"));
            descriptionTextView.setText(bundle.getString("plant_desc"));
            txtWater.setText(bundle.getString("txtWater"));
            txtHarvest.setText(bundle.getString("txtHarvest"));
            txtCare.setText(bundle.getString("txtCare"));
            txtPestsDiseases.setText(bundle.getString("txtPestsDisease"));

            key = bundle.getString("key");
            comPlant = bundle.getString("com_plant");
            sciPlant = bundle.getString("sci_plant");
            image = bundle.getString("plant_image");

            Log.e("comname", bundle.getString("com_plant"));
            ytEmbedKey = bundle.getString("ytLink");

        }

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getActivity()))
                .build();

        //webView.loadUrl(getIntent().getStringExtra("ytLink"));
        VideoEmbededAdress = "<iframe width=\"350\" height=\"225\" src=\"https://www.youtube.com/embed/"+ ytEmbedKey +"\" title=\"YouTube video player\"allow=\"autoplay;\" allowfullscreen></iframe>";

        webView.setWebViewClient(new WebViewClient() {
            private WebView view;
            private WebResourceRequest request;

            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              WebResourceRequest request) {
                Log.d("AddPlant", "shouldOverrideUrlLoading: Url = [" + request.getUrl()+"]");
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

    @Override
    public void onBackPressed() {
        navController.navigate(R.id.action_myGardenPlantsDetails_to_myGardenAllPlantsFragment);
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

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}