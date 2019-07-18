package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignupLoginActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    EditText edtUserName,edtPassword,edtDorP;
    Button btnSignUpLogIn,btnOneTimeLogin;
    RadioButton rdbPassenger,rdbDriver;
    State state;

    @Override
    public void onClick(View view) {

        if(edtDorP.getText().toString().equals("Driver") || edtDorP.getText().toString().equals("Passenger")){

            if(ParseUser.getCurrentUser() == null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(user != null && e==null){

                            Toast.makeText(SignupLoginActivity.this,"Anonymous User is logged in",Toast.LENGTH_SHORT).show();
                            user.put("as",edtDorP.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                }
                            });

                        }
                    }
                });
            }
        }
        else{
            Toast.makeText(SignupLoginActivity.this,"Are you a driver or passenger", Toast.LENGTH_LONG).show();
        }
    }

    enum State{
        Login,SignUp
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rdbPassenger=findViewById(R.id.rdbPassenger);
        rdbDriver=findViewById(R.id.rdbDriver);
        rdbDriver.setChecked(true);

        btnSignUpLogIn=findViewById(R.id.btnSignupLogIn);
        btnOneTimeLogin=findViewById(R.id.btnOneTimerLogin);

        edtUserName=findViewById(R.id.edtUserName);
        edtPassword=findViewById(R.id.edtPassword);
        edtDorP=findViewById(R.id.edtDovP);

        state=State.SignUp;

        btnOneTimeLogin.setOnClickListener(this);

        if(ParseUser.getCurrentUser()!=null){
           //ParseUser.getCurrentUser().logOut();
           transitionToPassengerActivity();
        }

        btnSignUpLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(state == State.SignUp){

                    String driverPassenger="";

                    if(edtPassword.getText().toString().equals("") || edtUserName.getText().toString().equals("")){
                        Toast.makeText(SignupLoginActivity.this,"Username and Password are required",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if(rdbDriver.isChecked()==false && rdbPassenger.isChecked()==false){
                        Toast.makeText(SignupLoginActivity.this,"Are you a driver or passenger", Toast.LENGTH_LONG).show();
                        return;
                    }
                    else if(rdbPassenger.isChecked()==true){
                        driverPassenger="Passenger";
                    }
                    else{
                        driverPassenger="Driver";
                    }

                    ParseUser appUser= new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    appUser.put("as",driverPassenger);

                    final ProgressDialog progressDialog=new ProgressDialog(SignupLoginActivity.this);
                    progressDialog.setMessage("Signing Up");
                    progressDialog.show();

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                FancyToast.makeText(SignupLoginActivity.this,ParseUser.getCurrentUser().getUsername()+" is saved successfully",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true).show();
                                progressDialog.dismiss();
                                transitionToPassengerActivity();
                                finish();
                            }
                            else{
                                FancyToast.makeText(SignupLoginActivity.this,"Unknown Error: "+e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }

                else if(state==State.Login){
                    if(edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")){
                        Toast.makeText(SignupLoginActivity.this,"Username and Password are required",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        final ProgressDialog progressDialog=new ProgressDialog(SignupLoginActivity.this);
                        progressDialog.setMessage("Signing Up");
                        progressDialog.show();

                        ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString()
                                , new LogInCallback() {
                                    @Override
                                    public void done(ParseUser user, ParseException e) {
                                        if(e==null && user!=null){
                                            FancyToast.makeText(SignupLoginActivity.this,ParseUser.getCurrentUser().getUsername()+" is logged in successfully",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true).show();
                                            progressDialog.dismiss();
                                            transitionToPassengerActivity();
                                            finish();
                                        }
                                        else{
                                            FancyToast.makeText(SignupLoginActivity.this,"Unknown Error: "+e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                    }

                }

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_signup_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logIn){

            if(state==State.Login){
                state=State.SignUp;
                rdbDriver.setVisibility(View.VISIBLE);
                rdbPassenger.setVisibility(View.VISIBLE);
                item.setTitle("Log In");
                btnSignUpLogIn.setText("SignUp");
            }
            else if(state==State.SignUp){
                state=State.Login;
                rdbDriver.setVisibility(View.GONE);
                rdbPassenger.setVisibility(View.GONE);
                item.setTitle("Sign Up");
                btnSignUpLogIn.setText("Log In");
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){

                Intent A=new Intent(SignupLoginActivity.this,PassengersActivity.class);
                startActivity(A);

            }
        }
    }
}
