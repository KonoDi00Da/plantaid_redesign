package com.example.plantaid_redesign.Identify;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.plantaid_redesign.Adapter.PlantIdResultsAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.LoginRegister.LoginRegisterActivity;
import com.example.plantaid_redesign.LoginRegister.SplashScreen;
import com.example.plantaid_redesign.Model.PlantIdentifiedModel;
import com.example.plantaid_redesign.Model.PlantIdentifyModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class IdentifyResultsFragment extends Fragment implements BackpressedListener {
    public static final String TAG = "Identify";

    private NavController navController;
    private ImageView imgPlant;
    private FloatingActionButton btn_back;
    Bundle bundle;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;
    DatabaseReference userRef;
    LoadingDialog loadingDialog;
    ArrayList<PlantIdentifyModel> list = new ArrayList<>();
    private PlantIdResultsAdapter cAdapter;
    PlantIdentifyModel model;


    private RecyclerView recyclerView;

    private String pushKey, imgUrl, organ;
    private TextView txtError;
    private boolean isClicked = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_identify_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        imgPlant = view.findViewById(R.id.imgPlant);
        btn_back = view.findViewById(R.id.btn_back);
        loadingDialog = new LoadingDialog(getActivity());
        txtError = view.findViewById(R.id.txtError);

        firebaseStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users").child(currentUser.getUid());
        recyclerView = view.findViewById(R.id.identifyResults);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        cAdapter = new PlantIdResultsAdapter(list, getActivity(), navController);
        recyclerView.setAdapter(cAdapter);

        try {
            setBundleArgs();
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            showResults();

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }

    }

    public void setBundleArgs() {
        bundle = getArguments();
        if (bundle != null) {
            Picasso.get().load(bundle.getString("imgPic")).into(imgPlant);
            pushKey = bundle.getString("pushKey");
            imgUrl = bundle.getString("serviceUrl");
            organ = bundle.getString("plantOrgan");

        }
    }

    private void showDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.cardview_cancel_identification);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle = getArguments();
                deleteImageFromStorage(bundle.getString("imagePath"));
                dialog.dismiss();

            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        BounceView.addAnimTo(dialog);
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
                        toast("Identification cancelled");
                        if(getActivity() != null){
                            isClicked = true;
                            ((Home)getActivity()).hideBottomNavigation(false);
                            navController.navigate(R.id.action_identifyResultsFragment_to_identifyFragment);
                        }
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

    private void showResults() {
        try {
            loadingDialog.startLoading("Fetching Results");

            String afterEncode = URLEncoder.encode(imgUrl, "UTF-8");
            String api_key = "2b10UhsM38YnFCRm7pHO8zK";
            String JSON_URL = "https://my-api.plantnet.org/v2/identify/all?api-key="+ api_key +"&images="+ afterEncode +"&organs="+ organ+"&include-related-images=true";
            //txtPlantID.setText(afterEncode);

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, JSON_URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loadingDialog.stopLoading();
                    try {
                        //for results
                        JSONArray results = response.getJSONArray("results");
                        if(results != null){
                            for(int i = 0; i < results.length(); i++){
                                JSONObject resultObject = results.getJSONObject(i);
                                String score = String.valueOf(resultObject.getDouble("score"));
                                model = new PlantIdentifyModel();

                                // scientific name of plant
                                JSONObject species = resultObject.getJSONObject("species");
                                JSONObject genus = species.getJSONObject("genus");
                                String scientificName = species.getString("scientificNameWithoutAuthor");
//                            String scientificName = genus.getString("scientificName");

                                //genus
                                String scientificNameGenus = genus.getString("scientificName");


                                // family of plant
                                JSONObject familyObj = species.getJSONObject("family");
                                String family = familyObj.getString("scientificName");

//                            // common names of plant
                                JSONArray commonName = species.getJSONArray("commonNames");

                                List<String> commonNameList = new ArrayList<String>();
                                for (int j = 0; j < commonName.length(); j++) {
                                    commonNameList.add(commonName.getString(j));
                                }

                                int size = commonNameList.size();
                                String[] stringArray
                                        = commonNameList.toArray(new String[size]);
                                model.setComName(TextUtils.join(", ",stringArray));



                                // images of plants
                                JSONArray images = resultObject.getJSONArray("images");

                                List<String> plantImages = new ArrayList<String>();
                                for (int j = 0; j < images.length(); j++) {
                                    JSONObject imagesObj = images.getJSONObject(j);
                                    JSONObject imgURL = imagesObj.getJSONObject("url");
                                    plantImages.add(imgURL.getString("s"));
                                }

                                int len = plantImages.size();
                                String[] stringImages
                                        = plantImages.toArray(new String[size]);

//                            txtPlantID.setText(String.valueOf(len));

                                if (len >= 1){
                                    model.setImg1(stringImages[0]);
                                    if(len >= 2 ){
                                        model.setImg2(stringImages[1]);
                                        if(len >= 3){
                                            model.setImg3(stringImages[2]);
                                            if(len >= 4){
                                                model.setImg4(stringImages[3]);
                                                if(len >= 5){
                                                    model.setImg5(stringImages[4]);
                                                }
                                            }
                                        }
                                    }
                                }
                                model.setSciName(scientificName);
                                model.setFamily(family);
                                model.setScore(score);
                                model.setGenus(scientificNameGenus);
                                list.add(model);

                                cAdapter.notifyDataSetChanged();
                            }
                        }else{
                            txtError.setVisibility(View.VISIBLE);
                            txtError.setText("No matching results");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, error -> {
                loadingDialog.stopLoading();

                try {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        // Access the data field
                        byte[] responseData = error.networkResponse.data;
                        String responseBody = new String( responseData, "utf-8" );
                        JSONObject jsonObject = new JSONObject( responseBody );
                        Log.d("response", "Response Check :" + responseBody);

                        String statusCode = jsonObject.getString("statusCode");
                        String errorTxt = jsonObject.getString("error");
                        String message = jsonObject.getString("message");
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText(statusCode + " " + errorTxt + " " + message);
                    } else {
                        Log.d("JSON", "Error");
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("No matching results");
                        // Handle the case when networkResponse or data is null
                        // Show an error message or take appropriate action
                    }
                } catch ( JSONException e ) {
                    Log.d("JSON", e.getMessage());
                } catch (UnsupportedEncodingException error2){
                    Log.d("JSON", error2.getMessage());
                }
            });

            requestQueue.add(jsonObjectRequest);

        } catch (Exception e){
            Log.e("Shop", "exception", e);
        }
    }

    @Override
    public void onBackPressed() {
        if(!isClicked){
            showDialog();
        } else {
            toast("Please wait");
        }

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

    private void toast(String msg ){
        if(getContext() != null){
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        }

    }
}