package com.itschool.buzuverov.corres_chat.Model;

import androidx.fragment.app.Fragment;

public abstract class myFragment extends Fragment {
    public abstract void onSearchViewTextChangeListener(String newText);
    public abstract void onCloseSearchViewListener();
}