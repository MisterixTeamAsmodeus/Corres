package com.itschool.buzuverov.corres_chat.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.itschool.buzuverov.corres_chat.R;

public class DialogTypeFile extends AppCompatDialogFragment {

    private DialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.layout_file_type_dialog, null);
        RelativeLayout file = view.findViewById(R.id.dialog_open_file),
        image = view.findViewById(R.id.dialog_open_image);

        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.getType("file");
                dismiss();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.getType("image");
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (DialogListener) context;
    }

    public interface DialogListener {
        void getType(String type);
    }
}
