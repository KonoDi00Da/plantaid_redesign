package com.example.plantaid_redesign.Identify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

public class IdentifyMoreInfoFragment_Identified extends Fragment implements BackpressedListener {
    public static final String TAG = "Identify";
    private NavController navController;
    private FloatingActionButton btn_back;
    private TextView sci_plant, txtFamily, txtGenus, txtScore, txtCommonNames, txtInfoLink;
    private Button btnConfirm;
    private ImageView img1, img2, img3, img4, img5;

    String image1, image2, image3, image4,image5;
    String plantCommonName,plantScore,plantSciName,plantFamily,plantGenus;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_identify_more_info__identified, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        btn_back = view.findViewById(R.id.btn_back);
        sci_plant = view.findViewById(R.id.sci_plant);
        txtFamily = view.findViewById(R.id.txtFamily);
        txtGenus = view.findViewById(R.id.txtGenus);
        txtScore = view.findViewById(R.id.txtScore);
        txtCommonNames = view.findViewById(R.id.txtCommonNames);
        txtInfoLink = view.findViewById(R.id.txtInfoLink);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        img1 = view.findViewById(R.id.img1);
        img2 = view.findViewById(R.id.img2);
        img3 = view.findViewById(R.id.img3);
        img4 = view.findViewById(R.id.img4);
        img5 = view.findViewById(R.id.img5);

        try {
            setArguments();
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    private void setArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            image1 = bundle.getString("img1");
            image2 = bundle.getString("img2");
            image3 = bundle.getString("img3");
            image4 = bundle.getString("img4");
            image5 = bundle.getString("img5");
            plantCommonName = bundle.getString("txtCommonName");
            plantScore = bundle.getString("txtScore");
            plantSciName = bundle.getString("txtScientificName");
            plantFamily = bundle.getString("txtFamily");
            plantGenus = bundle.getString("txtGenus");

            Picasso.get().load(image1).placeholder(R.mipmap.plantaid_logo).into(img1);
            Picasso.get().load(image2).placeholder(R.mipmap.plantaid_logo).into(img2);
            Picasso.get().load(image3).placeholder(R.mipmap.plantaid_logo).into(img3);
            Picasso.get().load(image4).placeholder(R.mipmap.plantaid_logo).into(img4);
            Picasso.get().load(image5).placeholder(R.mipmap.plantaid_logo).into(img5);

            String searchUrl = "https://www.google.com/search?q=" + Uri.encode(plantSciName);

            txtInfoLink.setText(searchUrl);
            txtCommonNames.setText(plantCommonName);
            txtScore.setText(plantScore);
            sci_plant.setText(plantSciName);
            txtFamily.setText(plantFamily);
            txtGenus.setText(plantGenus);

            txtInfoLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open the search link in a browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
                    getActivity().startActivity(browserIntent);
                }
            });

        }

    }


    @Override
    public void onBackPressed() {
        navController.popBackStack();

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
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}