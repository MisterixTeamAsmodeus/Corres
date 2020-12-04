package com.itschool.buzuverov.corres_chat.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.itschool.buzuverov.corres_chat.R;

public class DialogDeleteMessage extends AppCompatDialogFragment {

    private boolean deleteAll = true;
    private DialogDeleteMessage.DialogListener listener;

    public DialogDeleteMessage(DialogDeleteMessage.DialogListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_layout, null);
        CheckBox checkBox = view.findViewById(R.id.dialog_delete_check);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                deleteAll = b;
            }
        });
        Button accept = view.findViewById(R.id.dialog_delete_accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.accept(deleteAll);
            }
        });
        Button cansel = view.findViewById(R.id.dialog_delete_cancel);
        cansel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        builder.setView(view);
        return builder.create();
    }

    public interface DialogListener {
        void accept(boolean b);
    }
}
