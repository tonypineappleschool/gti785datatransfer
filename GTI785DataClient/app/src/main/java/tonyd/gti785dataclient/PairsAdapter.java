package tonyd.gti785dataclient;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class PairsAdapter extends RecyclerView.Adapter<PairsAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public Button statusButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.pair_name);
            statusButton = (Button) itemView.findViewById(R.id.status_button);
        }
    }

    private List<Pair> pairs;
    private Context mContext;

    public PairsAdapter(Context context, List<Pair> pairs) {
        this.pairs = pairs;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public PairsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_pair, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PairsAdapter.ViewHolder viewHolder, int position) {
        Pair contact = pairs.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(contact.getName());
        Button button = viewHolder.statusButton;
        button.setText("");
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return pairs.size();
    }

    public void swapItems(List<Pair> pairs) {
        // compute diffs
        final PairDiffCallback diffCallback = new PairDiffCallback(this.pairs, pairs);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // clear contacts and add
        this.pairs.clear();
        this.pairs.addAll(pairs);

        diffResult.dispatchUpdatesTo(this); // calls adapter's notify methods after diff is computed
    }
}
