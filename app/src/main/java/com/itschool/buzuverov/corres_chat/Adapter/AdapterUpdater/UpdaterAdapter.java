package com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater;


import androidx.recyclerview.widget.DiffUtil;

public class UpdaterAdapter<T extends UpdaterAdapterInterface> extends DiffUtil.Callback{

    private final T[] oldList;
    private final T[] newList;

    public UpdaterAdapter(T[] oldList, T[] newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.length;
    }

    @Override
    public int getNewListSize() {
        return newList.length;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        try {
            return (oldList[oldItemPosition].getId().equals(newList[newItemPosition].getId()));
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        try {
            return oldList[oldItemPosition].getDataToCheck() == newList[newItemPosition].getDataToCheck();
        } catch (Exception e){
            return false;
        }
    }
}