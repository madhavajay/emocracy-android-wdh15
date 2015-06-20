package wdh15.emocracy;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by madhavajay on 20/06/15.
 */
public class ChannelFragment extends Fragment {

    private static final String TAG = "ChannelFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.channel, container, false);
    }

    public void updateView(ChannelModel channelModel) {
        this.setChannelName(channelModel.name);
        this.setYes(channelModel.yes);
        this.setNo(channelModel.no);
    }

    public void setChannelName(String channelName) {
        TextView labelChannelName = (TextView) getView().findViewById(R.id.label_channel_name);
        labelChannelName.setText(channelName);
    }

    public void setYes(int yes) {
        TextView labelYes = (TextView) getView().findViewById(R.id.label_channel_yes);
        labelYes.setText("" + yes);
    }

    public void setNo(int no) {
        TextView labelNo = (TextView) getView().findViewById(R.id.label_channel_no);
        labelNo.setText("" + no);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        RelativeLayout background = (RelativeLayout) getView().findViewById(R.id.background);
        if (background != null) {
            background.setOnClickListener(listener);
        } else {
            Log.v(TAG, "background not available yet?");
        }

    }

}