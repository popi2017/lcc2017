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

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import agent.ComensalAgentInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import jade.core.MicroRuntime;
import jade.util.Logger;
import jade.wrapper.ControllerException;
import jade.wrapper.O2AException;
import jade.wrapper.StaleProxyException;
import neg.gui.R;




public class ComensalActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	static final int PARTICIPANTS_REQUEST = 0;

	private MyReceiver myReceiver;

	private String nickname;
	
	private ComensalAgentInterface negClientInterface;
	/////
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;
	private Button buttonDate; 
	private Button buttonTime; 
	/////
	
	private Spinner spinnerTipoComida;
	private Spinner spinnerPrecio;
	private Spinner spinnerEstacionamiento;
	private Spinner spinnerBarrio;

	private TextView tvTipoComida;
	private TextView tvPrecio;
	private TextView tvEstacionamiento;
	private TextView tvBarrio;
	
	private TextView tvMasInfo;
	
	private ImageButton btSiguiente;
	
	private JsonObject objeto = new JsonObject();
	private String datosEnviar="";
	
    JsonObject objRecibido = new JsonObject();
    JsonParser parserRecibido = new JsonParser();

    int finalizacionNeg;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			nickname = extras.getString("nickname");
		}

		try {
			negClientInterface = MicroRuntime.getAgent(nickname)
					.getO2AInterface(ComensalAgentInterface.class);
		} catch (StaleProxyException e) {
			showAlertDialog(getString(R.string.msg_interface_exc), true);
		} catch (ControllerException e) {
			showAlertDialog(getString(R.string.msg_controller_exc), true);
		}

		myReceiver = new MyReceiver();

		IntentFilter refreshChatFilter = new IntentFilter();
		refreshChatFilter.addAction("jade.menu.REFRESH_CHAT_NEG");
		registerReceiver(myReceiver, refreshChatFilter);

		IntentFilter clearChatFilter = new IntentFilter();
		clearChatFilter.addAction("jade.menu.CLEAR_CHAT_NEG");
		registerReceiver(myReceiver, clearChatFilter);

		setContentView(R.layout.neg_chat);

		Button button = (Button) findViewById(R.id.button_send);
		//button.setOnClickListener(buttonSendListener);
		
		btSiguiente = (ImageButton) findViewById(R.id.btSiguiente);
		
		//////////////
		Calendar calendar = Calendar.getInstance();
		// Current date
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		// Current time
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMinute = calendar.get(Calendar.MINUTE);

		buttonDate = (Button) findViewById(R.id.btDate);
		if(buttonDate != null) buttonDate.setText(String.format("%02d/%02d/%d",mDay,mMonth + 1,mYear));

		buttonTime = (Button) findViewById(R.id.btTime);
		if(buttonTime != null) buttonTime.setText(String.format("%02d:%02d",mHour,mMinute));
		

		//////////////
		
		
		spinnerTipoComida =  buildSpinnerCustom("spinnerTipoComida");
		spinnerPrecio =  buildSpinnerCustom("spinnerPrecio");
		spinnerEstacionamiento =  buildSpinnerCustom("spinnerEstacionamiento");
		spinnerBarrio= buildSpinnerCustom("spinnerBarrio");
		
		tvTipoComida=(TextView) findViewById(R.id.tvTipoComida);
		tvPrecio=(TextView) findViewById(R.id.tvPrecio);
		tvEstacionamiento=(TextView) findViewById(R.id.tvEstacionamiento);
		tvBarrio=(TextView) findViewById(R.id.tvBarrio);
		tvMasInfo=(TextView) findViewById(R.id.tvMasInfo);
		
		finalizacionNeg =0;
		//finalizacionNeg =Integer.parseInt(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("BANDERATERMINACION", "0"));

	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.log(Level.INFO, "Destroy activity!");
	}

	/*
	//QVendemos La Utilidad de esto esta deprecada YA que FUE UTILIZADO para debugear BORRAR LUEGO
	private OnClickListener buttonSendListener = new OnClickListener() {
		public void onClick(View v) {
			final EditText messageField = (EditText) findViewById(R.id.edit_message);
			String message = messageField.getText().toString();
			if (message != null && !message.equals("")) {
				try {
					negClientInterface.handleSpoken(message+"El pedido de su cita es para el "+mDay+"/"+mMonth+"/"+mYear+"A partir de las:"+mHour+":"+mMinute);
					messageField.setText("");
				} catch (O2AException e) {
					showAlertDialog(e.getMessage(), false);
				}
			}

		}
	};
	//HASTA ACA
	*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.neg_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_clear:
			final TextView chatField = (TextView) findViewById(R.id.chatTextView);
			chatField.setText("");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PARTICIPANTS_REQUEST) {
			if (resultCode == RESULT_OK) {
			}
		}
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("jade.menu.REFRESH_CHAT_NEG")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				
				String pedidoMasInfo ="RW_masDatos";
				String contraOferta="RW_contraOferta";
				String tratoOriginalAceptado="RW_ACEPTADO";
				String restoSeRetira="RW_WITHDRAW";
				
				int noNegociablePrice=Integer.valueOf(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("COTAMAXIMA", ""));
				int noNegociableEstac=Integer.valueOf(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("ESTACIONAMIENTO", ""));

				//Este if es relacionado a que se acepto de una la propuesta original
				if ((intent.getExtras().getString("sentence").toLowerCase().indexOf(tratoOriginalAceptado.toLowerCase()) != -1 )){
					setearBanderaFinalizacion();
					
					new AlertDialog.Builder(context)
				    .setTitle("Fin De Negociacion SATISFACTORARIAMENTE")
				    .setMessage("Hubo acuerdo en una propuesta original sin necesidad de contraOferta")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				     })
				    .setIcon(android.R.drawable.ic_dialog_alert)
				     .show();
					//Hasta Aca es el mensaje del Alert
				}
				

				if ((intent.getExtras().getString("sentence").toLowerCase().indexOf(restoSeRetira.toLowerCase()) != -1 )){
					try {
						 negClientInterface.autoMensaje("\n \n Se ha retirado un Resto de la negociaci�n porque no fue cedida alguna informaci�n o no hay compatibilidad entre sus ofertas y mis deseos \n\n ");
						} catch (O2AException e) {
							showAlertDialog(e.getMessage(), false);
						}
				}
				
				if ((intent.getExtras().getString("sentence").toLowerCase().indexOf(contraOferta.toLowerCase()) != -1 )){
					String tmpContraOfertaMsg=(intent.getExtras().getString("sentence").split("--")[1]);
					JsonObject precioNuevoJson = new JsonObject();
					precioNuevoJson = parserRecibido.parse(tmpContraOfertaMsg).getAsJsonObject();
					int nuevoPrecioOfrecido= precioNuevoJson.get("ultOferta").getAsInt();
					int nuevoEstacionam=precioNuevoJson.get("ultEstacionamiento").getAsInt();
					String remitenteContraOferta=precioNuevoJson.get("remitenteContraOferta").getAsString();
					
					String tituloEncabezado="FIN DE LA NEGOCIACION";
					String tituloMensaje="";
					String respuestaContraOferta="";
					String tmpStringRespuestaContraOferta="Rechazada";//LO USO unicamente para el mensaje que le envio a los resto para no dar informacion extra 
					
					String tmpEstaciMsg="";
					if(nuevoEstacionam>1) {
						tmpEstaciMsg="gratuito";
					}
					else {
						tmpEstaciMsg="pago";
					}
					
					if (finalizacionNeg==0) {
					
					if (nuevoPrecioOfrecido>noNegociablePrice || nuevoEstacionam<noNegociableEstac ){
						tituloMensaje="NO HUBO ACUERDO -(";
						respuestaContraOferta="RW_ResponsecontraOferta--"+"\n\n En relaci�n a la nueva oferta recibida por parte de " +remitenteContraOferta+ " a un precio de $"+nuevoPrecioOfrecido+", donde el estacionamiento es "+tmpEstaciMsg+"  . La Oferta es Rechazada ya que no es compatible con los requisitos deseados!!!\n\n";
						tmpStringRespuestaContraOferta="Rechazada";
						
					}
					else {
						tituloMensaje="FINALMENTE HUBO ACUERDO -), con el Resto "+remitenteContraOferta;
						
						respuestaContraOferta="RW_ResponsecontraOferta--"+"\n\n En relaci�n a la nueva oferta recibida por parte de " +remitenteContraOferta+ " a un precio de $"+nuevoPrecioOfrecido+", donde el estacionamiento es "+tmpEstaciMsg+"  . La Oferta fue aceptada -)!!!\n\n";
						tmpStringRespuestaContraOferta="Aceptada";

						setearBanderaFinalizacion();
						
						new AlertDialog.Builder(context)
					    .setTitle(tituloEncabezado)
					    .setMessage(tituloMensaje)
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     })
					    .setIcon(android.R.drawable.ic_dialog_alert)
					     .show();
		
					}
					
					
					}
					
					else {
						
						respuestaContraOferta="RW_ResponsecontraOferta--"+"\n\n En relaci�n a la nueva oferta recibida por un precio de $"+nuevoPrecioOfrecido+", y en relaci�n al estacionamiento "+tmpEstaciMsg+" la Oferta es Rechazada debido a que ya se habia aceptado otra oferta!!!";
					}
					try {
						negClientInterface.autoMensaje(respuestaContraOferta);
						negClientInterface.handleSpoken("\n\n La ContraOferta de "+remitenteContraOferta+" fue: "+tmpStringRespuestaContraOferta+" por parte del agente " +nickname+"\n\n");
						
						} catch (O2AException e) {
							showAlertDialog(e.getMessage(), false);
						}
				}

				
				
				if ((intent.getExtras().getString("sentence").toLowerCase().indexOf(pedidoMasInfo.toLowerCase()) != -1 )){
					
					String tmpAux=intent.getExtras().getString("sentence").split("--")[1];
					objRecibido = parserRecibido.parse(tmpAux).getAsJsonObject();
					
					int datoSobreMenu =Integer.parseInt(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREMENU", "0"));
					int datoSobreBarrio = Integer.parseInt(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREBARRIO", "0"));
					int datoSobreEstacionamiento = Integer.parseInt(getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("DATOSOBREESTACIONAMIENTO", "0"));

					String valorTipoComidaNew="666";
					String valorBarrioNew="666";
					String valorEstacionamientoNew="666";
							
							
					if (datoSobreMenu>-1){
						valorTipoComidaNew=getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("TIPOCOMIDA", "666");	
					}
					else{
						valorTipoComidaNew="0";
					}

					
					if (datoSobreBarrio>-1){
						valorBarrioNew=getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("BARRIO", "666");	
					}
					else{
						valorBarrioNew="0";
					}

					
					if (datoSobreEstacionamiento>-1){
						valorEstacionamientoNew=getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE).getString("ESTACIONAMIENTO", "666");	
					}
					else{
						valorEstacionamientoNew="0";
					}
	
					try {			
						objeto.addProperty("TIPOCOMIDA",valorTipoComidaNew);
		
						objeto.addProperty("BARRIO",valorBarrioNew);						
						objeto.addProperty("ESTACIONAMIENTO",valorEstacionamientoNew);
						datosEnviar=objeto.toString();

						try {
						//Esta Linea de aca abajo la agrego para mayor claridad de las cosas
						negClientInterface.autoMensaje("\n\n Se ha recibido un pedido de solicitud de mas informaci�n\n\n");
						negClientInterface.handleSpoken("A continuaci�n se env�an/procesan los datos que se esta dispuesto a ceder RW_ResponseMasDatos--"+datosEnviar);
						} catch (O2AException e) {
							showAlertDialog(e.getMessage(), false);
						}
					} catch (JsonIOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				else{
				chatField.append(intent.getExtras().getString("sentence"));
				scrollDown();
				}
			}
			if (action.equalsIgnoreCase("jade.menu.CLEAR_CHAT_NEG")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.setText("");
			}
		}
	}

	private void setearBanderaFinalizacion() {
		/*ASI GUARDO EL VALOR DE TERMINACION en el sharedPreferences ademas de en la variable por si se vuelve a crear el Activity*/
		SharedPreferences shared = getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putString("BANDERATERMINACION", "1");
		editor.commit();
		
		finalizacionNeg=1;
		/**/

	}
	
	private void scrollDown() {
		final ScrollView scroller = (ScrollView) findViewById(R.id.scroller);
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		scroller.smoothScrollTo(0, chatField.getBottom());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		savedInstanceState.putString("chatField", chatField.getText()
				.toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		chatField.setText(savedInstanceState.getString("chatField"));
	}

	private void showAlertDialog(String message, final boolean fatal) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ComensalActivity.this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								dialog.cancel();
								if(fatal) finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();		
	}
	
	
	///////////////////
	
	/**
	 * 
	 * @param v
	 */
	public void DoSetDate(View v){
		new DatePickerDialog(this,	new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mYear = year;
				mMonth = monthOfYear;
				mDay = dayOfMonth;
				//
				buttonDate.setText(String.format("%02d/%02d/%d",dayOfMonth,monthOfYear + 1,year));
				buttonTime.setError(null);
			}
		}, mYear, mMonth, mDay).show();
	}

	public void DoContinue(View v){
	
				

    	//Intent intent = new Intent(this, DireccionActivity.class);
		//startActivity(intent);

		
	}	

	
	/**
	 * 
	 * @param v
	 */
	public void DoSetTime(View v){
		new TimePickerDialog(this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mHour = hourOfDay;
				mMinute = minute;
				buttonTime.setText(String.format("%02d:%02d",hourOfDay,minute));
				buttonTime.setError(null);
			}
		}, mHour, mMinute, true).show();
	}



	public Date getInputDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, mHour);
		calendar.set(Calendar.MINUTE, mMinute);
		calendar.set(Calendar.YEAR, mYear);
		calendar.set(Calendar.MONTH, mMonth);
		calendar.set(Calendar.DAY_OF_MONTH, mDay);

		return calendar.getTime();
	}

	///////////////////
	
	protected Spinner buildSpinnerCustom(String baseName){
		int spinnerId = getResources().getIdentifier(baseName, "id", this.getPackageName());
		Spinner spinner = (Spinner) findViewById(spinnerId);

		int valuesId = getResources().getIdentifier(baseName+"Values", "array", this.getPackageName());

		String[] values = getResources().getStringArray(valuesId);
		spinner.setAdapter(new ItemAdapter(this,  R.layout.spinner_item_customizado, values));

		return spinner;
	}
	
	private class ItemAdapter extends ArrayAdapter<String> {
		public ItemAdapter(Context context, int textViewResourceId, String[] values) {
			super(context, textViewResourceId, values);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView holder;
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(), R.layout.spinner_item_customizado, null);
				holder = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(holder);
			} else {
				holder = (TextView) convertView.getTag();
			}
			holder.setText(this.getItem(position).split("\\|")[1]);
			return convertView;
		}
		@Override
		public View getDropDownView(int position, View convertView,	ViewGroup parent) {
			TextView holder;
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(), R.layout.spinner_item_customizado, null);
				holder = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(holder);
			} else {
				holder = (TextView) convertView.getTag();
			}
			holder.setText(this.getItem(position).split("\\|")[1].trim());
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			String id = item.split("\\|")[0].trim();
			return Long.valueOf(id);
		}
		
	}
	// FinDelSpinner mio

}