package tonyd.gti785dataclient;

import android.content.Context;
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

public class FileSyncsAdapter extends RecyclerView.Adapter<FileSyncsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView pathTextView;
        public TextView statusTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            pathTextView = (TextView) itemView.findViewById(R.id.path_text_view);
            statusTextView = (TextView) itemView.findViewById(R.id.status_text_view);
        }
    }

    private List<FileSync> fileSyncs;
    private Context mContext;

    public FileSyncsAdapter(Context context, List<FileSync> fileSyncs) {
        this.fileSyncs = fileSyncs;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public FileSyncsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_file_sync, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FileSyncsAdapter.ViewHolder viewHolder, int position) {
        FileSync fileSync = fileSyncs.get(position);
        // Set item views based on your views and data model
        TextView pathTextView = viewHolder.pathTextView;
        TextView statusTextView = viewHolder.statusTextView;
        pathTextView.setText(fileSync.getFullPath());
        statusTextView.setText(fileSync.getStatus().name());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return fileSyncs.size();
    }

}
