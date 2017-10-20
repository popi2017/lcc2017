package popi;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import neg.gui.MainActivity;
import neg.gui.R;

public class DatosNegociacionActivity extends Activity  {
	
	// VALORES
	private Spinner spinnerPrecio;
	private Spinner spinnerEstacionamiento;
	private Spinner spinnerBarrio;
	private EditText etPrecioMenu;

	private CheckBox cbVegetariano;
	private CheckBox cbCarne;
	private CheckBox cbPizza;
	private CheckBox cbPasta;
	
	
	private RadioButton rbTipoComidaOblig;
	private RadioButton rbTipoComidaPreferente;
	private RadioButton rbTipoComidaIndiferente;

	private RadioButton rbBarrioOblig;
	private RadioButton rbBarrioPreferente;
	private RadioButton rbBarrioIndiferente;

	private RadioButton rbEstacionamientoOblig;
	private RadioButton rbEstacionamientoPreferente;
	private RadioButton rbEstacionamientoIndiferente;
	
	private RadioButton rbEstacionamientoGratuitoSi;
	private RadioButton rbEstacionamientoGratuitoNo;
	
	private int mHour;
	private int mMinute;

	private Button buttonTime; 
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {     
		setContentView(R.layout.datos_negociacion_activity);
		super.onCreate(savedInstanceState);
		
		spinnerPrecio =  buildSpinnerCustom("spinnerDescPrecio");
		spinnerEstacionamiento =  buildSpinnerCustom("spinnerEstacionamiento");
		spinnerBarrio= buildSpinnerCustom("spinnerSucursalPromo");
		
		cbVegetariano=(CheckBox) findViewById(R.id.checkBoxVegetariano);
		cbCarne= (CheckBox) findViewById(R.id.checkBoxCarne);
		cbPizza=(CheckBox) findViewById(R.id.checkBoxPizza);
		cbPasta= (CheckBox) findViewById(R.id.checkBoxPasta);
		etPrecioMenu = (EditText) findViewById(R.id.etPrecioMenu);
		
				
		rbEstacionamientoOblig= (RadioButton) findViewById(R.id.rdEstacionamiento_01);
		rbEstacionamientoPreferente=(RadioButton) findViewById(R.id.rdEstacionamiento_02);
		rbEstacionamientoIndiferente=(RadioButton) findViewById(R.id.rdEstacionamiento_03);
		rbTipoComidaOblig= (RadioButton) findViewById(R.id.rdTipoComida_01);
		rbTipoComidaPreferente=(RadioButton) findViewById(R.id.rdTipoComida_02);
		rbTipoComidaIndiferente=(RadioButton) findViewById(R.id.rdTipoComida_03);
		rbBarrioOblig= (RadioButton) findViewById(R.id.rdBarrio_01);
		rbBarrioPreferente=(RadioButton) findViewById(R.id.rdBarrio_02);
		rbBarrioIndiferente=(RadioButton) findViewById(R.id.rdBarrio_03);
		
		rbEstacionamientoGratuitoSi= (RadioButton) findViewById(R.id.rdEstacionmientoFree_01);
		rbEstacionamientoGratuitoNo= (RadioButton) findViewById(R.id.rdEstacionmientoFree_00);
		
		
		//Estas 2 lineas las uso para cambiar el color de la barra por default
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e91e63")));
		
	}
	
	public void DoContinue(View v){
			 
			if(etPrecioMenu.getText().toString().trim().equals("")  ){
			    	DialogCustom alert = new DialogCustom();
			    	alert.showDialog(this, "Se olvido de completar el precio del menu, \n O seleccionar al menos un tipo de menú para ofrecer");
			    }
			    else{
			    	
			    	/* Aca Guardo todos los datos a pasar*/
			    	
					SharedPreferences shared = getSharedPreferences("jadeNegPrefsFile", MODE_PRIVATE);
					SharedPreferences.Editor editor = shared.edit();
					
					Long valorSpPrecioMenu=	spinnerPrecio.getSelectedItemId();
					Long valorSpSucursal=	spinnerBarrio.getSelectedItemId();
					Long valorSpEstacionamiento= spinnerEstacionamiento.getSelectedItemId();

					editor.putString("ULTPRECIO", valorSpPrecioMenu+"");
					editor.putString("BARRIO", valorSpSucursal+"");
					editor.putString("ULTESTAC", valorSpEstacionamiento+"");

					
					if(cbVegetariano.isChecked()){
						editor.putString("MENUVEGET", "1");
					}
					else {
						editor.putString("MENUVEGET", "0");
					}
					
					if(cbCarne.isChecked()){
						editor.putString("MENUCARNE", "1");
					}
					else{
						editor.putString("MENUCARNE", "0");
					}
					
					if(cbPizza.isChecked()){
						editor.putString("MENUPIZZA", "1");
					}
					else{
						editor.putString("MENUPIZZA", "0");
					}
					
					if(cbPasta.isChecked()){
						editor.putString("MENUPASTA", "1");
					}
					else{
						editor.putString("MENUPASTA", "0");
					}
					
			    	
				    if(rbTipoComidaOblig.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "-1");
				    }
				    if(rbTipoComidaPreferente.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "0");
				    }
				    if(rbTipoComidaIndiferente.isChecked()) {
				    	editor.putString("DATOSOBREMENU", "1");
				    }

				    if(rbBarrioOblig.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "-1");
				    }
				    if(rbBarrioPreferente.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "0");
				    }
				    if(rbBarrioIndiferente.isChecked()) {
				    	editor.putString("DATOSOBREBARRIO", "1");
				    }				    
				    
				    
				    if(rbEstacionamientoOblig.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "-1");
				    }
				    if(rbEstacionamientoPreferente.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "0");
				    }
				    if(rbEstacionamientoIndiferente.isChecked()) {
				    	editor.putString("DATOSOBREESTACIONAMIENTO", "1");
				    }
				    
				    if (rbEstacionamientoGratuitoSi.isChecked()){
				    	editor.putString("ESTACIONAMIENTO", "2");
				    }
				    else {editor.putString("ESTACIONAMIENTO", "1");}
				    
				    editor.putString("PRECIOMENU", etPrecioMenu.getText().toString().trim());
				    
				    editor.commit();

				    
			    	/*Hasta aca*/
			    	Intent intent = new Intent(this, MainActivity.class);
					startActivity(intent);
			    }		
	}	


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
	            	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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