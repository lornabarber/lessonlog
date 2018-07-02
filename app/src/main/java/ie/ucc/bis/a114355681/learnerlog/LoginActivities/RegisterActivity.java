package ie.ucc.bis.a114355681.learnerlog.LoginActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ie.ucc.bis.a114355681.learnerlog.R;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etEmail, etPassword, txtName, txtAddress, txtPhone;

    //Declare FirebaseAuth listener object
    private FirebaseAuth mAuth;

    DatabaseReference studentRef, userRef;

    //progress bar to show user they are being registered
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); //set the layout for the activity

        //cast variables to edit text
        etPassword = (EditText) findViewById(R.id.etPassword);
        etEmail = (EditText) findViewById(R.id.etEmail);
        txtName = (EditText) findViewById(R.id.txtName);
        txtAddress = (EditText) findViewById(R.id.txtAddress);
        txtPhone = (EditText)findViewById(R.id.txtPhone);


        findViewById(R.id.btnRegister).setOnClickListener(this);

        //Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        //initalize progressDialog
        progressDialog = new ProgressDialog(this);

        studentRef = FirebaseDatabase.getInstance().getReference().child("students");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

    }

    private void registerUser(){

        //create username and password variables
        String userEmail = etEmail.getText().toString().trim();
        String userPass = etPassword.getText().toString().trim();

        //checks if email has been entered
        if(userEmail.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Please enter your email address."
            , Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        //validates email address
        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            etEmail.setError("Please enter a valid email.");
            etEmail.requestFocus();
            return;
        }

        //checks if password has been entered
        if(userPass.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Please enter your password."
                    , Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        //minimum password for Firebase Auth is 6 characters, validates this
        if(userPass.length()<6){
            etPassword.setError("Password must be greater than 6 characters.");
            etPassword.requestFocus();
            return;
        }

        /*in each of the above errors, .requestFocus() will bring the cursor to the required
        text field. 'return;' ensures the app doesn't continue. */

        //set the message for progress dialog and show it to the user
        progressDialog.setMessage("Creating your account...");
        progressDialog.show();

        //call the create user method
        mAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //check success
                        if(task.isSuccessful()){
                            //create user node in database
                            Map<String, String> userData = new HashMap<String, String>(); //hash map to store details

                            String userid = mAuth.getCurrentUser().getUid(); //retrieve unique userid generated by firebase authentications

                            String name = txtName.getText().toString(); //get name
                            String email = etEmail.getText().toString(); //get email
                            String phone = txtPhone.getText().toString(); //get phone number
                            String address = txtAddress.getText().toString(); //get address

                            //add each detail to the hash map
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("type", "student");
                            userData.put("phone", phone);
                            userData.put("address", address);

                            //set the value in the database with the user id as the database key
                            userRef.child(userid).setValue(userData);

                            //create student node in database
                            Map<String, String> studentData = new HashMap<String, String>(); //hash map to store details

                            String studentid = mAuth.getCurrentUser().getUid(); //retrieve unique userid generated by firebase authentications

                            String student_name = txtName.getText().toString(); //get name

                            //add the students name
                            studentData.put("name", student_name);

                            //set the value in the database with the user id as the database key
                            studentRef.child(studentid).setValue(userData);

                            //toast message displaying that the account has successfully been created
                            Toast.makeText(RegisterActivity.this, "Your account has been created.", Toast.LENGTH_SHORT).show();
                            //Brings you to the login activity
                            Intent intent = new Intent (RegisterActivity.this, MainActivity.class);
                            //clear all open activities on the stack and open the new one
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //start the new activity
                            startActivity(intent);
                        } else {
                            //checks if email already exists
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                               Toast.makeText(RegisterActivity.this, "You are already registered!", Toast.LENGTH_SHORT).show();
                            } else {
                                //other exception errors
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


    }



    //set register button onClickListener
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnRegister:
                registerUser(); //call registerUser() method to insert user details.
                break;
        }

    }


}
