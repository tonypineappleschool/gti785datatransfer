package tonyd.gti785dataclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tonyd on 7/22/2017.
 */

public class UpdatesActivity extends Activity{
    private RecyclerView rvPairs;
    private RecyclerView rvFileSync;
    PairsUpdatesAdapter pairsUpdatesAdapter;
    FileSyncsAdapter fileSyncsAdapter;
    HashMap<Pair, ArrayList<FileSync>> hashMap;
    ArrayList<FileSync> fileSyncs;
    ArrayList<Pair> pairs;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_updates);
        pairs = new ArrayList<>();
        fileSyncs = new ArrayList<>();
        hashMap =  (HashMap<Pair, ArrayList<FileSync>>) intent.getSerializableExtra(Command.HASHMAPSYNC);
        Iterator it = hashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            pairs.add((Pair) pair.getKey());
            it.remove(); // avoids a ConcurrentModificationException
        }
        if (pairs.size() > 0){
            fileSyncs = hashMap.get(pairs.get(0));
        }
        pairsUpdatesAdapter = new PairsUpdatesAdapter(this, pairs);
        rvPairs = (RecyclerView) findViewById(R.id.rvPairs);
        rvPairs.setAdapter(pairsUpdatesAdapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPairs.setLayoutManager(layoutManager);

        rvPairs.postDelayed(() -> {
            for (int i = 0 ; i < pairsUpdatesAdapter.getItemCount() ; i++){
                PairsUpdatesAdapter.ViewHolder vh = (PairsUpdatesAdapter.ViewHolder) rvPairs.findViewHolderForAdapterPosition(i);
                if (vh != null){
                    Pair pair = pairs.get(i);
                    vh.nameButton.setOnClickListener(v -> {
                        fileSyncs = hashMap.get(pair);
                        pairsUpdatesAdapter.notifyDataSetChanged();
                    });
                }
            }
        },2000);
        fileSyncsAdapter = new FileSyncsAdapter(this, fileSyncs);
        rvFileSync = (RecyclerView) findViewById(R.id.rvFileSyncs);
        rvFileSync.setAdapter(fileSyncsAdapter);
        rvPairs.setLayoutManager(new LinearLayoutManager(this));
    }
}
