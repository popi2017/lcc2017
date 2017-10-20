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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import agent.RestoAgent;
import neg.gui.R;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class MainActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	
	private ServiceConnection serviceConnection;

	static final int CHAT_NEG_REQUEST = 0;
	static final int SETTINGS_REQUEST = 1;

	private MyReceiver myReceiver;
	private MyHandler myHandler;

	private TextView infoTextView;

	private String nickname;
	
    private JsonObject objeto = new JsonObject();
	private String datosEnviar="";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myReceiver = new MyReceiver();

		IntentFilter killFilter = new IntentFilter();
		killFilter.addAction("jade.menu.KILL");
		registerReceiver(myReceiver, killFilter);

		IntentFilter showNegFilter = new IntentFilter();
		showNegFilter.addAction("jade.menu.SHOW_CHAT_NEG");
		registerReceiver(myReceiver, showNegFilter);

		myHandler = new MyHandler();

		setContentView(R.layout.main);
		//Estas 2 lineas las uso para cambiar el color de la barra por default
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e91e63")));
				
		Button button = (Button) findViewById(R.id.button_chat);
		button.setOnClickListener(buttonNegListener);

		infoTextView = (TextView) findViewById(R.id.infoTextView);
		infoTextView.setText("");
		
		try {			

			objeto.addProperty("MENUVEGET",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("MENUVEGET", "666"));
			objeto.addProperty("MENUCARNE",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("MENUCARNE", "666"));
			objeto.addProperty("MENUPIZZA",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("MENUPIZZA", "666"));
			objeto.addProperty("MENUPASTA",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("MENUPASTA", "666"));

			objeto.addProperty("ESTACIONAMIENTO",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("ESTACIONAMIENTO", "666"));
			
			objeto.addProperty("DATOSOBREMENU",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREMENU", "666"));
			objeto.addProperty("DATOSOBREBARRIO",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREBARRIO", "666"));
			objeto.addProperty("DATOSOBREESTACIONAMIENTO",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREESTACIONAMIENTO", "666"));
					
			objeto.addProperty("BARRIO",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("BARRIO", "666"));
			objeto.addProperty("ULTPRECIO",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("ULTPRECIO", "666"));
			objeto.addProperty("ULTESTAC",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("ULTESTAC", "666"));
			

			objeto.addProperty("PRECIOMENU",getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("PRECIOMENU", "666"));
			
			datosEnviar=objeto.toString();
			
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
		logger.log(Level.INFO, "Destroy activity!");
	}

	private static boolean checkName(String name) {
		if (name == null || name.trim().equals("")) {
			return false;
		}
		return true;
	}

	private OnClickListener buttonNegListener = new OnClickListener() {
		public void onClick(View v) {
			
			final EditText nameField = (EditText) findViewById(R.id.edit_nickname);
			nickname = nameField.getText().toString();
			if (!checkName(nickname)) {
				logger.log(Level.INFO, "Invalid nickname!");
				myHandler.postError(getString(R.string.msg_nickname_not_valid));
			} else {
				try {
					SharedPreferences settings = getSharedPreferences(
							"jadeNegPrefsFile", 0);
					String host = settings.getString("defaultHost", "");
					String port = settings.getString("defaultPort", "");
					infoTextView.setText(getString(R.string.msg_connecting_to)
							+ " " + host + ":" + port + "...");
					startNeg(nickname, host, port, agentStartupCallback);
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
					infoTextView.setText(getString(R.string.msg_unexpected));
				}
			}
			
		}
	};

	public boolean auxChequeoNombre(String nickname){
			if (!checkName(nickname)) {
			logger.log(Level.INFO, "Invalid nickname!");
			myHandler.postError(getString(R.string.msg_nickname_not_valid));
		} else {
			return true;
		}
		return false;	
		}
		
	

	
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_neg_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent showSettings = new Intent(MainActivity.this,
					SettingsActivity.class);
			MainActivity.this.startActivityForResult(showSettings,
					SETTINGS_REQUEST);
			return true;
		case R.id.menu_exit:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHAT_NEG_REQUEST) {
			if (resultCode == RESULT_CANCELED) {
				infoTextView.setText("");
				logger.log(Level.INFO, "Stopping Jade...");
				microRuntimeServiceBinder
						.stopAgentContainer(new RuntimeCallback<Void>() {
							@Override
							public void onSuccess(Void thisIsNull) {
							}

							@Override
							public void onFailure(Throwable throwable) {
								logger.log(Level.SEVERE, "Failed to stop the "
										+ RestoAgent.class.getName()
										+ "...");
								agentStartupCallback.onFailure(throwable);
							}
						});
			}
		}
	}

	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.log(Level.INFO, "Nickname already in use!");
			myHandler.postError(getString(R.string.msg_nickname_in_use));
		}
	};

	public void ShowDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("jade.menu.KILL")) {
				finish();
			}
			if (action.equalsIgnoreCase("jade.menu.SHOW_CHAT_NEG")) {
				Intent showChat = new Intent(MainActivity.this,
						RestoActivity.class);
				showChat.putExtra("nickname", nickname);
				MainActivity.this
						.startActivityForResult(showChat, CHAT_NEG_REQUEST);
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle.containsKey("error")) {
				infoTextView.setText("");
				String message = bundle.getString("error");
				ShowDialog(message);
			}
		}

		public void postError(String error) {
			Message msg = obtainMessage();
			Bundle b = new Bundle();
			b.putString("error", error);
			msg.setData(b);
			sendMessage(msg);
		}
	}

	
	public void startNeg(final String nickname, final String host,
			final String port,
			final RuntimeCallback<AgentController> agentStartupCallback) {

		final Properties profile = new Properties();
		profile.setProperty(Profile.MAIN_HOST, host);
		profile.setProperty(Profile.MAIN_PORT, port);
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		} else {
			profile.setProperty(Profile.LOCAL_HOST,
					AndroidHelper.getLocalIPAddress());
		}
		// Emulator: this is not really needed on a real device
		profile.setProperty(Profile.LOCAL_PORT, "2000");

		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
					startContainer(nickname, profile, agentStartupCallback);
				};

				public void onServiceDisconnected(ComponentName className) {
					microRuntimeServiceBinder = null;
					logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
				}
			};
			logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
			bindService(new Intent(getApplicationContext(),
					MicroRuntimeService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
			startContainer(nickname, profile, agentStartupCallback);
		}
	}

	private void startContainer(final String nickname, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(nickname, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(nickname, agentStartupCallback);
		}
	}

	
//ACA Hago el linkeo Con La Clase del Agente que defini en el paquete agent	con el archivo RestoAgent.java
	private void startAgent(final String nickname,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(nickname,
				RestoAgent.class.getName(),
				new Object[] { getApplicationContext(),datosEnviar },
				new RuntimeCallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						//logger.log(Level.INFO, "Successfully start of the "
							//	+ RestoAgent.class.getName() + "...");
						try {
							agentStartupCallback.onSuccess(MicroRuntime
									.getAgent(nickname));
						} catch (ControllerException e) {
							// Should never happen
							agentStartupCallback.onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable throwable) {
						//logger.log(Level.SEVERE, "Failed to start the "
							//	+ RestoAgent.class.getName() + "...");
						agentStartupCallback.onFailure(throwable);
					}
				});
	}

}
