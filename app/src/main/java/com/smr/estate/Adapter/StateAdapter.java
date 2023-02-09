package com.smr.estate.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.smr.estate.Model.State;
import com.smr.estate.R;

import java.util.ArrayList;

import wiadevelopers.com.library.DivarUtils;

public class StateAdapter extends BaseAdapter implements Filterable
{
    private Context context;
    private ArrayList<State> states;
    private final ArrayList<State> tmpStates;
    private LayoutInflater inflater = null;

    public StateAdapter(Context context, ArrayList<State> states)
    {
        this.states = states;
        this.tmpStates = states;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        return states.size();
    }

    public State getItem(int position)
    {
        return states.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();
                ArrayList<State> myResult = new ArrayList<>();
                for (int i = 0; i < tmpStates.size(); i++)
                {
                    if (charSequence.length() > 0)
                    {
                        if (tmpStates.get(i).getTitle().contains(charSequence.toString()))
                            myResult.add(tmpStates.get(i));
                    } else
                        myResult.add(tmpStates.get(i));
                }
                results.count = myResult.size();
                results.values = myResult;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                states = (ArrayList<State>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public static class ViewHolder
    {
        public TextView txtText;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        ViewHolder holder;

        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.item_state, null);
            holder = new ViewHolder();
            holder.txtText = vi.findViewById(R.id.itemStateTxtText);
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        holder.txtText.setTypeface(DivarUtils.faceLight);
        if (states.size() != 0)
            holder.txtText.setText(states.get(position).getTitle());
        else
            holder.txtText.setText("آیتمی یافت نشد.");
        return vi;
    }
}
