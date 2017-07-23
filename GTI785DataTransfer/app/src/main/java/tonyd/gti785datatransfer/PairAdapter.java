package tonyd.gti785datatransfer;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by stefaniekoy on 2017-07-20.
 */

public class PairAdapter extends RecyclerView.Adapter<PairAdapter.ViewHolder> {

    List <Pair> pairs;

    // Pass in the contact array into the constructor
    public PairAdapter(List <Pair> pairs) {
        this.pairs = pairs;
    }

    @Override
    public PairAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View pairView = inflater.inflate(R.layout.pair_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(pairView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PairAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Pair pair = pairs.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(pair.getName());
        if(pair.isOnline())
            textView.setTextColor(Color.GREEN);
        else
            textView.setTextColor(Color.RED);

        viewHolder.pair = pair;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return pairs.size();
    }

    // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView nameTextView;
            public Pair pair;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                nameTextView = (TextView) itemView.findViewById(R.id.pair_name);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "qqch" + pair.getId(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
}
