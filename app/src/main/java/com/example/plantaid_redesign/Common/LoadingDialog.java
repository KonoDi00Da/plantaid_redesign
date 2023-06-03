package com.example.plantaid_redesign.Common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import com.example.plantaid_redesign.R;

public class LoadingDialog {

    Context context;
    Dialog dialog;
    TextView label;
    public LoadingDialog(Context myContext){
        context = myContext;
    }

    public void startLoading(String loadingLabel) {
        dialog = new Dialog(context);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.custom_loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize the textview

        label = dialog.findViewById(R.id.textView26);
        label.setText(loadingLabel + "...");

        dialog.show();
    }

    public void stopLoading(){
        dialog.cancel();
        dialog.dismiss();
    }
    public void onProgress(String text){
        label.setText(text);
        dialog.show();
    }

}
