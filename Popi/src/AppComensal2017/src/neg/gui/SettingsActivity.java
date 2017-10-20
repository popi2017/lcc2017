/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package neg.gui;


import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import neg.gui.R;
import jade.util.leap.Properties;



public class SettingsActivity extends Activity {
	Properties properties;
	EditText hostField;
	EditText portField;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getSharedPreferences("jadeNegPrefsFile",
				0);

		String host = settings.getString("defaultHost", "");
		String port = settings.getString("defaultPort", "");

		setContentView(R.layout.settings);

		hostField = (EditText) findViewById(R.id.edit_host);
		hostField.setText(host);

		portField = (EditText) findViewById(R.id.edit_port);
		portField.setText(port);

		Button button = (Button) findViewById(R.id.button_use);
		button.setOnClickListener(buttonUseListener);
		
		//Estas 2 lineas las uso para cambiar el color de la barra por default
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2196F3")));
	}

	private OnClickListener buttonUseListener = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences settings = getSharedPreferences(
					"jadeNegPrefsFile", 0);

			SharedPreferences.Editor editor = settings.edit();
			editor.putString("defaultHost", hostField.getText().toString());
			editor.putString("defaultPort", portField.getText().toString());
			editor.commit();

			finish();
		}
	};
}
