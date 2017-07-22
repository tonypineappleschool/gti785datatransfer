package tonyd.gti785dataclient;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyd on 7/21/2017.
 */

public class PairsUpdatesAdapter extends RecyclerView.Adapter<PairsUpdatesAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button nameButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameButton = (Button) itemView.findViewById(R.id.name_button);
        }
    }

    private List<Pair> pairs;
    private Context mContext;

    public PairsUpdatesAdapter(Context context, ArrayList<Pair> pairs) {
        this.pairs = pairs;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public PairsUpdatesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_pair_update, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PairsUpdatesAdapter.ViewHolder viewHolder, int position) {
        Pair pair = pairs.get(position);
        // Set item views based on your views and data model
        Button button = viewHolder.nameButton;
        button.setText(pair.getName());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return pairs.size();
    }

}
