package com.examen.pablo.pruebafirebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    LoginButton loginButton;
    Button btnLogOut;
    TextView txt;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Inicializa el SDK de Facebook. Importante hacerlo antes de setContentView.
         */
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        txt = (TextView)findViewById(R.id.textView);
        btnLogOut = (Button)findViewById(R.id.btnLogout);

        // Comprueba si la sesión ya está iniciada o no
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null){
            Toast.makeText(getApplicationContext(), "Ya estás logueado", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Debes logearte", Toast.LENGTH_SHORT).show();
        }

        // FACEBOOK LOGIN
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(new String[]{"user_location", "public_profile", "user_friends", "email"}); // Permisos necesarios para obtener la info que queremos en este ejemplo

        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Login correcto", Toast.LENGTH_SHORT).show();
                loginButton.setVisibility(View.GONE);
                btnLogOut.setVisibility(View.VISIBLE);

                // Obtener info del usuario de su perfil de Facebook con una llamada a la Api Graph
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        if (response.getError() != null) {
                            // handle error
                        } else {
                            String id = me.optString("id");
                            String firstname = me.optString("first_name");
                            String lastname = me.optString("last_name");
                            String email = me.optString("email");
                            String location = null;
                            String urlAvatar = null;
                            try {
                                urlAvatar = me.getJSONObject("picture").getJSONObject("data").getString("url");
                                location = me.getJSONObject("location").getString("name").split(",")[0]; // El objeto name viene con formato "Málaga, Spain"
                            }
                            catch (Exception e) {e.printStackTrace();}

                            // Muestra los datos en este TextView de ejemplo
                            txt.setText(id + "\n" + firstname + "\n" + lastname + "\n" + email + "\n" + urlAvatar + "\n"+location);
                        }
                    }
                });
                Bundle parameters = new Bundle();
                // Campos que vamos a pedir: id, nombre, apellidos, email, imagen de avatar tamaño grande, género, localización
                parameters.putString("fields", "id,first_name,last_name,email, picture.type(large),gender,location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Cancelado!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), "Error!!", Toast.LENGTH_SHORT).show();
            }
        });

        // Cerrar sesión con nuestro propio botón
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hacer Log out mediante programación.
                LoginManager.getInstance().logOut();
                // El LoginButton automáticamente al iniciar sesión cambia su texto a "Cerrar sesión" y proporciona eso mismo.
                // Pero si queremos hacer log out desde nuestro propio botón esta es la forma

                loginButton.setVisibility(View.VISIBLE);
                btnLogOut.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Importante: Sin esto el diálogo de Facebook no devuelve nada a nuestra Activity
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
