package tonyd.gti785datatransfer;

import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import java.util.Comparator;

/**
 * Created by tonyd on 7/12/2017.
 */

public class PairUI {
    private Pair pair;
    private TextView textViewName;
    private TextView textViewDistance;
    private Button button;

    public PairUI(Pair pair, TextView textViewName, TextView textViewDistance, Button button) {
        this.pair = pair;
        this.textViewName = textViewName;
        this.textViewDistance = textViewDistance;
        this.button = button;
    }

    public TextView getTextViewName() {
        return textViewName;
    }

    public void setTextViewName(TextView textViewName) {
        this.textViewName = textViewName;
    }

    public TextView getTextViewDistance() {
        return textViewDistance;
    }

    public void setTextViewDistance(TextView textViewDistance) {
        this.textViewDistance = textViewDistance;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public static Comparator<PairUI> COMPARE_BY_NAME = new Comparator<PairUI>() {

        @Override
        public int compare(PairUI o1, PairUI o2) {
            return o1.textViewName.getText().toString().compareTo(o2.textViewName.getText().toString());
        }
    };

    public static Comparator<PairUI> COMPARE_BY_DISTANCE = new Comparator<PairUI>() {

        @Override
        public int compare(PairUI o1, PairUI o2) {
            int returnVal = 0;
            int o1Distance = Integer.parseInt(o1.textViewDistance.getText().toString());
            int o2Distance = Integer.parseInt(o2.textViewDistance.getText().toString());

            if (o1Distance < o2Distance) {
                returnVal = -1;
            } else if (o1Distance > o2Distance) {
                returnVal = 1;
            } else if (o1Distance == o2Distance) {
                returnVal = 0;
            }
            return returnVal;
        }
    };

    public Pair getPair() {
        return pair;
    }

    public void setPair(Pair pair) {
        this.pair = pair;
    }
}
