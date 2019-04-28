package codingnojutsu.clubhub;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText mTextUsername;
    EditText mTextPassword;
    EditText mTextCnfPassword;
    Button mButtonRegister;
    TextView mTextViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        mTextUsername = (EditText) findViewById(R.id.editText_username);
        mTextPassword = (EditText) findViewById(R.id.editText_password);
        mTextCnfPassword = (EditText) findViewById(R.id.editText_cnf_password);
        mButtonRegister = (Button) findViewById(R.id.button_createAccount);
        mTextViewLogin = (TextView) findViewById(R.id.textView_register);
        mTextViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(LoginIntent);
            }
        });
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = mTextUsername.getText().toString().trim();
                String pwd = mTextPassword.getText().toString().trim();
                String cnf_pwd = mTextCnfPassword.getText().toString().trim();

                if(pwd.equals(cnf_pwd)) {
                    long val = db.addUser(user,pwd);
                    if (val > 0 && user.length() > 2 && user.length() < 12 && pwd.length() > 5 && !user.contains(" ")) {
                        Toast.makeText(RegisterActivity.this, "Successfully Created Account", Toast.LENGTH_SHORT).show();
                        Intent moveToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(moveToLogin);
                    } else if (!(user.length() > 2)) {
                        Toast.makeText(RegisterActivity.this, "Username must be at least 3 characters long", Toast.LENGTH_SHORT).show();
                    } else if (!(user.length() < 12)) {
                        Toast.makeText(RegisterActivity.this, "Username can't be greater than 11 characters long", Toast.LENGTH_SHORT).show();
                    } else if (user.contains(" ")) {
                        Toast.makeText(RegisterActivity.this, "Username can't contain spaces", Toast.LENGTH_SHORT).show();
                    } else if (!(pwd.length() > 5)) {
                        Toast.makeText(RegisterActivity.this, "Password must be 6 characters or greater", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}