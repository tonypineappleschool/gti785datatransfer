package tonyd.gti785datatransfer;

import android.widget.Button;
import android.widget.TextView;

/**
 * Created by tonyd on 7/12/2017.
 */

class PairUI {
    private TextView textViewName;
    private TextView textViewDistance;
    private Button button;

    public PairUI(TextView textViewName, TextView textViewDistance, Button button) {
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
}
