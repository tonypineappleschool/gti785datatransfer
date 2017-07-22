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

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private FolderContentActivity folderContentActivity;

    public FolderContentActivity getFolderContentActivity() {
        return folderContentActivity;
    }

    public void setFolderContentActivity(FolderContentActivity folderContentActivity) {
        this.folderContentActivity = folderContentActivity;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public Button statusButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.file_name);
            statusButton = (Button) itemView.findViewById(R.id.status_button);
        }
    }

    private List<File> files;
    private Context mContext;

    public FilesAdapter(Context context, List<File> files) {
        this.files = files;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public FilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_file, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FilesAdapter.ViewHolder viewHolder, int position) {
        File contact = files.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(contact.getName());
        Button button = viewHolder.statusButton;
        button.setText("DOWNLOAD");
        folderContentActivity.updateListeners();
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return files.size();
    }

    public void swapItems(List<File> files) {
        // compute diffs
        final FileDiffCallback diffCallback = new FileDiffCallback(this.files, files);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // clear contacts and add
        this.files.clear();
        this.files.addAll(files);

        diffResult.dispatchUpdatesTo(this); // calls adapter's notify methods after diff is computed
    }
}
