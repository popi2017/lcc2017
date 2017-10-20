package popi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import neg.gui.MainActivity;
import neg.gui.R;


public class DatosNegociacionActivity extends Activity  {
	
	// VALORES
	private Spinner spinnerTipoComida;
	private Spinner spinnerPrecio;
	private Spinner spinnerEstacionamiento;
	private Spinner spinnerBarrio;
	
	
	private RadioButton rbTipoComidaCompartir;
	private RadioButton rbTipoComidaPreferenteNo;
	private RadioButton rbTipoComidaNunca;

	private RadioButton rbBarrioCompartir;
	private RadioButton rbBarrioPreferenteNo;
	private RadioButton rbBarrioNunca;

	private RadioButton rbEstacionamientoCompartir;
	private RadioButton rbEstacionamientoPreferenteNo;
	private RadioButton rbEstacionamientoNunca;


	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {     
		setContentView(R.layout.datos_negociacion_activity);
		super.onCreate(savedInstanceState);
		
		spinnerTipoComida =  buildSpinnerCustom("spinnerTipoComida");
		spinnerPrecio =  buildSpinnerCustom("spinnerPrecio");
		spinnerEstacionamiento =  buildSpinnerCustom("spinnerEstacionamiento");
		spinnerBarrio= buildSpinnerCustom("spinnerBarrio");
		
		rbEstacionamientoCompartir= (RadioButton) findViewById(R.id.rdEstacionamiento_01);
		rbEstacionamientoPreferenteNo=(RadioButton) findViewById(R.id.rdEstacionamiento_02);
		rbEstacionamientoNunca=(RadioButton) findViewById(R.id.rdEstacionamiento_03);
		rbTipoComidaCompartir= (RadioButton) findViewById(R.id.rdTipoComida_01);
		rbTipoComidaPreferenteNo=(RadioButton) findViewById(R.id.rdTipoComida_02);
		rbTipoComidaNunca=(RadioButton) findViewById(R.id.rdTipoComida_03);
		rbBarrioCompartir= (RadioButton) findViewById(R.id.rdBarrio_01);
		rbBarrioPreferenteNo=(RadioButton) findViewById(R.id.rdBarrio_02);
		rbBarrioNunca=(RadioButton) findViewById(R.id.rdBarrio_03);
		
		
		//Estas 2 lineas las uso para cambiar el color de la barra por default
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2196F3")));
	}
	
	public void DoContinue(View v){
	
						
				Long valorSpPrecio=	spinnerPrecio.getSelectedItemId();
				Long valorSpTipoComida=	spinnerTipoComida.getSelectedItemId();
				Long valorSpEstacionamiento= spinnerEstacionamiento.getSelectedItemId();
				Long valorSpBarrio=	spinnerBarrio.getSelectedItemId();

				SharedPreferences shared = getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				
					editor.putString("COTAMAXIMA", valorSpPrecio+"");
					editor.putString("TIPOCOMIDA", valorSpTipoComida+"");
					editor.putString("ESTACIONAMIENTO", valorSpEstacionamiento+"");
					editor.putString("BARRIO", valorSpBarrio+"");
	
					
				    if(rbTipoComidaCompartir.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "1");
				    }
				    if(rbTipoComidaPreferenteNo.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "0");
				    }
				    if(rbTipoComidaNunca.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "-1");
				    }

				    if(rbBarrioCompartir.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "1");
				    }
				    if(rbBarrioPreferenteNo.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "0");
				    }
				    if(rbBarrioNunca.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "-1");
				    }				    
				    
				    
				    if(rbEstacionamientoCompartir.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "1");
				    }
				    if(rbEstacionamientoPreferenteNo.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "0");
				    }
				    if(rbEstacionamientoNunca.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "-1");
				    }

			    	editor.putString("BANDERATERMINACION", "0");
			    
					editor.commit();
		
			    
			//Relacionado con el popUp si hay algun dato que no completo
			 
			    if(valorSpPrecio==0 || valorSpTipoComida==0 || valorSpEstacionamiento==0 || valorSpBarrio==0){
			    	DialogCustom alert = new DialogCustom();
			    	alert.showDialog(this, "Hay Datos que No Proporciono.\n Continuar de todas formas?");
			    }
			    else{
			    	Intent intent = new Intent(this, MainActivity.class);
					startActivity(intent);
			    }		
	}	

//
	public class DialogCustom {

	    public void showDialog(Activity activity, String msg){
	        final Dialog dialog = new Dialog(activity);
	        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        dialog.setCancelable(false);
	        dialog.setContentView(R.layout.custom_dialog);

	        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
	        text.setText(msg);

	        Button dialogButtonSi = (Button) dialog.findViewById(R.id.btn_dialog_si);
	        dialogButtonSi.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dialog.dismiss();
	            	Intent intent = new Intent(getApplicationContext(), neg.gui.MainActivity.class);
	    			startActivity(intent);
	            }
	        });
	        
	        Button dialogButtonNo = (Button) dialog.findViewById(R.id.btn_dialog_no);      
	        dialogButtonNo.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dialog.dismiss();
	            }
	        });
	        
	        dialog.show();

	    }

	}
	
	
	//Spinner que genero
	
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
	// FinDelSpinner que genero

//	
}