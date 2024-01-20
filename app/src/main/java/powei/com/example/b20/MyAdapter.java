package powei.com.example.b20;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Contact[] healths;
    public MyAdapter(Context context, Contact[] contacts){
        inflater=LayoutInflater.from(context);
        this.healths = contacts;
    }

    @Override
    public int getCount() {
        if(healths==null||healths.length==0)
            return 0;
        return healths.length;
    }

    @Override
    public Object getItem(int position ) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {
        converView=inflater.inflate(R.layout.activity_item,null);

        TextView txvdate=(TextView) converView.findViewById(R.id.txvdate);
        TextView txvdata=(TextView) converView.findViewById(R.id.txvdata);
        TextView txvtype=(TextView) converView.findViewById(R.id.txvtype);
        TextView txvtime=(TextView) converView.findViewById(R.id.txvtime);

        txvdate.setText(String.valueOf(healths[position].date));
        txvdata.setText(String.valueOf(healths[position].data));
        txvtype.setText(String.valueOf(healths[position].types));
        txvtime.setText(String.valueOf(healths[position].time));

        return converView;
    }
}
