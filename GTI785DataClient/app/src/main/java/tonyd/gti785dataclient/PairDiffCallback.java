package tonyd.gti785dataclient;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class PairDiffCallback extends DiffUtil.Callback {
    private List<Pair> mOldList;
    private List<Pair> mNewList;

    public PairDiffCallback(List<Pair> oldList, List<Pair> newList) {
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
        return mOldList.get(oldItemPosition).getId() == mNewList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Pair oldPair = mOldList.get(oldItemPosition);
        Pair newPair = mNewList.get(newItemPosition);

        if (oldPair.getName() == newPair.getName() && oldPair.isStatus() == newPair.isStatus()) {
            return true;
        }
        return false;
    }
}
