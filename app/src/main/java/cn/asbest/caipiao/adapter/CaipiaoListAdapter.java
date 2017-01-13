package cn.asbest.caipiao.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.asbest.caipiao.R;
import cn.asbest.model.Data;

/**
 * Created by chenyanlan on 2016/11/11.
 */

public class CaipiaoListAdapter extends RecyclerView.Adapter<CaipiaoListAdapter.ViewHolder> {

    private List<Data> data;

    public void setData(List<Data> data){
        this.data = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.expect)
        TextView expect;
        @BindView(R.id.code)
        TextView opencode;
        @BindView(R.id.time)
        TextView opentime;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public CaipiaoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.caipiao_item, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(CaipiaoListAdapter.ViewHolder holder, int position) {
        holder.expect.setText(data.get(position).getExpect());
        holder.opencode.setText(data.get(position).getOpencode());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date(data.get(position).getOpentimestamp()*1000));
//        Log.d("cgd", String.format("date %d = %d =  %s", position,data.get(position).getOpentimestamp()*1000, date));
        holder.opentime.setText(date);
    }

    @Override
    public int getItemCount() {
        return this.data == null?0:this.data.size();
    }
}
