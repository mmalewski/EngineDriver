/*Copyright (C) 2010 Jason M'Sadoques
  jlyonm@gmail.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package jmri.enginedriver;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class turnouts extends Activity {

	private threaded_application mainapp;  // hold pointer to mainapp
	
	ArrayList<HashMap<String, String> > turnouts_list;
	private SimpleAdapter turnouts_list_adapter;

	  public class turnout_item implements AdapterView.OnItemClickListener	  {

		  //When a turnout  is clicked, send command to toggle it
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)	    {
	    	ViewGroup vg = (ViewGroup)v; //convert to viewgroup for clicked row
	    	ViewGroup rl = (ViewGroup) vg.getChildAt(0);  //get relativelayout
	    	TextView snv = (TextView) rl.getChildAt(1); // get systemname text from 2nd box
	    	String systemname = (String) snv.getText();
	    	TextView csv = (TextView) vg.getChildAt(1);  //get currentstate textview
	    	String currentstate = (String) csv.getText();
	        Message msg=Message.obtain();  
        	msg.what=message_type.TURNOUT;
/*        	if (currentstate.equals("Thrown")) {  //TODO: replace with hash lookup 
        		msg.arg1=4; //toggle
        	} else {
        		msg.arg1=2; //toggle
        	}
*/
msg.arg1=2; // 2 = toggle???  //TODO: find out from Brett if this is broken        	
        	msg.arg2=0; 
            msg.obj=new String(systemname);    // load system name for turnout into message
            mainapp.comm_msg_handler.sendMessage(msg);
	    };
	  }	  

	private void refresh_turnout_list() {

		//clear and rebuild
       turnouts_list.clear();
	  if (mainapp.to_user_names != null) {
			int pos = 0;
		    for (String username : mainapp.to_user_names) {
		    	if (!username.equals(""))  {  //skip turnouts without usernames
		    		//get values from global array
		    		String systemname = mainapp.to_system_names[pos];
		    		String currentstate = mainapp.to_states[pos];
		    		String currentstatedesc = mainapp.to_state_names.get(currentstate);
		    		if (currentstatedesc == null) {
		    			currentstatedesc = "unknown";
		    		}
		    		
		    		//put values into temp hashmap
		            HashMap<String, String> hm=new HashMap<String, String>();
		            hm.put("to_user_name", username);
		            hm.put("to_system_name", systemname);
		            hm.put("to_current_state_desc", currentstatedesc);

		            //add temp hashmap to list which view is hooked to
		            turnouts_list.add(hm);
			//
		    	}
		    	pos++;
		    }  //if username blank
		    turnouts_list_adapter.notifyDataSetChanged();
		 }  //end for loop
	  }
	  
	  //Handle messages from the communication thread back to this thread (responses from withrottle)
	  class turnouts_handler extends Handler {

		public void handleMessage(Message msg) {
	      switch(msg.what) {
	      case message_type.RESPONSE: {
	        	String response_str = msg.obj.toString();
	        	if (response_str.substring(0,3).equals("PTA")) {  //refresh turnouts if any have changed
	        		refresh_turnout_list();
	        	}
	        }
	        break;
	    };
		}
	  }

  @Override
  public void onStart() {
    super.onStart();

    //put pointer to this activity's handler in main app's shared variable (If needed)
    if (mainapp.turnouts_msg_handler == null){
  	  mainapp.turnouts_msg_handler=new turnouts_handler();
    }

    //update turnout list
    refresh_turnout_list();
  }

	  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.turnouts);
    
    mainapp=(threaded_application)getApplication();
    //put pointer to this activity's handler in main app's shared variable
//    mainapp.turnouts_handler=new turnouts_handler();
    
    //Set up a list adapter to allow adding the list of recent connections to the UI.
    turnouts_list=new ArrayList<HashMap<String, String> >();
    turnouts_list_adapter=new SimpleAdapter(this, turnouts_list, R.layout.turnouts_item, new String[] {"to_user_name", "to_system_name", "to_current_state_desc"},
            new int[] {R.id.to_user_name, R.id.to_system_name, R.id.to_current_state_desc});
    ListView turnouts_lv=(ListView)findViewById(R.id.turnouts_list);
    turnouts_lv.setAdapter(turnouts_list_adapter);
    turnouts_lv.setOnItemClickListener(new turnout_item());

  };
}
