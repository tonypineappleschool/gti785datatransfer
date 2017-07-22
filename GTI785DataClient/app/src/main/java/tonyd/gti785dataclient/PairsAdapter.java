package tonyd.gti785dataclient;

import android.content.Context;
import android.media.Image;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class PairsAdapter extends RecyclerView.Adapter<PairsAdapter.ViewHolder> {

    MainActivity activity;

    public MainActivity getActivity() {
        return activity;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView distanceTextView;
        public Button statusButton;
        public ImageView deleteImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.pair_name);
            distanceTextView = (TextView) itemView.findViewById(R.id.pair_distance);
            statusButton = (Button) itemView.findViewById(R.id.status_button);
            deleteImageView = (ImageView) itemView.findViewById(R.id.delete);
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
        Pair pair = pairs.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(pair.getName());
        TextView distanceTextView = viewHolder.distanceTextView;
        float distance = activity.getDistance(pair);
        if (distance != -1){
            distanceTextView.setText(Float.toString(distance) + "m");
        } else {
            distanceTextView.setText("UNKNOWN");
        }
        Button button = viewHolder.statusButton;
        if (pair.isStatus()){
            button.setEnabled(true);
            button.setText("BROWSE");
        } else {
            button.setEnabled(false);
            button.setText("OFFLINE");
        }
        ImageView deleteImageView = viewHolder.deleteImageView;
        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deletePair(pair);
            }
        });

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return pairs.size();
    }

    public void swapItems(List<Pair> newPairs) {
        // compute diffs
        final PairDiffCallback diffCallback = new PairDiffCallback(this.pairs, newPairs);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // clear contacts and add
        this.pairs.clear();
        this.pairs.addAll(newPairs);

        diffResult.dispatchUpdatesTo(this); // calls adapter's notify methods after diff is computed
    }
}
