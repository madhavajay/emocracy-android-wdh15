package wdh15.emocracy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by madhavajay on 20/06/15.
 */
public class ChannelListAdapter extends ArrayAdapter<ChannelModel> {

    private final static String TAG = "ChannelListAdapter";
    private ArrayList<ChannelModel> items;
    private Context context;

    public ChannelListAdapter(Context context, int textViewResourceId, ArrayList<ChannelModel> values) {
        super(context, textViewResourceId, values);
        this.items = values;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v(TAG, "drawing a view inside channel list adapter");
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.channel, null);
        }

        ChannelModel channelModel = items.get(position);

        if (channelModel != null) {
            TextView labelChannelName = (TextView) v.findViewById(R.id.label_channel_name);
            labelChannelName.setText(channelModel.name);

            TextView labelYes = (TextView) v.findViewById(R.id.label_channel_yes);
            labelYes.setText("" + channelModel.yes);

            TextView labelNo = (TextView) v.findViewById(R.id.label_channel_no);
            labelNo.setText("" + channelModel.no);
        }
        return v;
    }

}