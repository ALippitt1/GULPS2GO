package savingPackageNew;

import java.util.List;

import com.bioimpedance.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Used to display the session information. Display format is:
 * 
 * Session Num
 * 	Date: dd/mm/yyyy Time: hh:mm:ss						Type of Swallow
 * 
 * @author ajl157
 *
 */
public class ListAdapter extends ArrayAdapter<String[]>{

	 private Context c;
    private int id;
    private List<String[]> items;
	    
	public ListAdapter(Context context, int textViewResourceId, List<String[]> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
           View v = convertView;
           if (v == null) {
               LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               v = vi.inflate(id, null);
           }
           final String[] o = items.get(position);
           if (o != null) {
        	   TextView t1 = (TextView) v.findViewById(R.id.TextView01);
               TextView t2 = (TextView) v.findViewById(R.id.TextView02);
               TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);
               ImageView i1 = (ImageView) v.findViewById(R.id.fd_Icon1);
               
               i1.setVisibility(View.GONE);
               
               if(t1!=null)
                   t1.setText("Session " + (position+1));
               if(t2!=null)
                   t2.setText(o[0] + " " + o[1]);
               if(t3!=null)
            	   t3.setText(o[2]);
               
           }
           return v;
	}

}
