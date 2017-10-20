package popi;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import neg.gui.MainActivity;
import neg.gui.R;
import neg.gui.R.layout;

/**
 * Created by Alejandro on 11/10/2016.
 */
public class SplashActivity  extends Activity  {

    private final String PREFS_NAME = "Primera_Ejecucion";
    private final int DURACION = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

		//Estas 2 lineas las uso para cambiar el color de la barra por default
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e91e63")));
        
        new Handler().postDelayed(new Runnable(){
            public void run() {
                Intent intent;
                intent = new Intent(SplashActivity.this, DatosNegociacionActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURACION);

    }
}