package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import filesscan.codingexercise.R;
import model.Header;
import model.MetaData;

/**
 * Created by sunil on 9/11/17.
 */

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> mALDataSet;
    private Context mContext;
    private final int HEADER = 0, DATA = 1;


    public FileAdapter(Context context, ArrayList<Object> mGridData) {
        mContext = context;
        mALDataSet = mGridData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HEADER:
                View viewChat = inflater.inflate(R.layout.header, parent, false);
                viewHolder = new HeaderViewholder(viewChat);
                break;
            case DATA:
                View contactView = inflater.inflate(R.layout.listitem, parent, false);
                viewHolder = new DataHolder(contactView);
                break;

            default:

                View defaultView = inflater.inflate(R.layout.listitem, parent, false);
                viewHolder = new DataHolder(defaultView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {

            case HEADER:
                HeaderViewholder headerViewholder = (HeaderViewholder) holder;
                Header chat = (Header) mALDataSet.get(position);
                headerViewholder.mTvHeader.setText(chat.getHeader().toString());
                break;
            case DATA:
                DataHolder dataHolder = (DataHolder) holder;
                MetaData data = (MetaData) mALDataSet.get(position);
                dataHolder.filename.setText(data.getName());
                dataHolder.size.setText("" + data.getFileSize());

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mALDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mALDataSet.get(position) instanceof Header) {
            return HEADER;
        } else {
            return DATA;
        }
    }

    public static class HeaderViewholder extends RecyclerView.ViewHolder {
        public TextView mTvHeader;

        public HeaderViewholder(View itemView) {
            super(itemView);
            mTvHeader = (TextView) itemView.findViewById(R.id.tv_header);
        }
    }

    public static class DataHolder extends RecyclerView.ViewHolder {

        public TextView filename, size;


        public DataHolder(View itemView) {
            super(itemView);
            filename = (TextView) itemView.findViewById(R.id.tv_name);
            size = (TextView) itemView.findViewById(R.id.tv_size);

        }
    }
}
