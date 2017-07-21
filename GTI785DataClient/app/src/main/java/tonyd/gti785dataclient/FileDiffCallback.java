package tonyd.gti785dataclient;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class FileDiffCallback extends DiffUtil.Callback {
    private List<File> mOldList;
    private List<File> mNewList;

    public FileDiffCallback(List<File> oldList, List<File> newList) {
        this.mOldList = oldList;
        this.mNewList = newList;
    }
    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // add a unique ID property on Contact and expose a getId() method
        return mOldList.get(oldItemPosition).getName() == mNewList.get(newItemPosition).getName();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        File oldFile = mOldList.get(oldItemPosition);
        File newFile = mNewList.get(newItemPosition);

        if (oldFile.getName() == newFile.getName() && oldFile.getSize() == newFile.getSize()) {
            return true;
        }
        return false;
    }
}
