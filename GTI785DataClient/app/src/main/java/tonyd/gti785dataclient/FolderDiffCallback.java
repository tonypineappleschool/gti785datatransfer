package tonyd.gti785dataclient;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class FolderDiffCallback extends DiffUtil.Callback {
    private List<Folder> mOldList;
    private List<Folder> mNewList;

    public FolderDiffCallback(List<Folder> oldList, List<Folder> newList) {
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
        Folder oldFolder = mOldList.get(oldItemPosition);
        Folder newFolder = mNewList.get(newItemPosition);

        if (oldFolder.getName() == newFolder.getName() && oldFolder.getDate() == newFolder.getDate()) {
            return true;
        }
        return false;
    }
}
