package com.nari.notify2calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nari.notify2calendar.dbHandler.DBHandler;
import com.nari.notify2calendar.util.utilToast;

public class StrValueAdd extends AppCompatActivity {

    EditText editStrValue ;
    Button btnSave, btnDelte ;
    String _id = "" ;
    String extraMode = "" ;
    String TAG = "StrValueAdd" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_str_value_add);
        editStrValue = findViewById(R.id.editStrValue) ;

        Intent intent = new Intent(this.getIntent()) ;
        extraMode = intent.getStringExtra("mode");
        if ("update".equals(extraMode)) {
            _id = intent.getStringExtra("Id") ;
            editStrValue.setText(intent.getStringExtra("StrValue"));
            Log.i(TAG, "id=" + _id) ;
            Log.i(TAG, "strValue=" + editStrValue.getText().toString());
        }
        btnSave = findViewById(R.id.btnSave) ;
        btnDelte = findViewById(R.id.btnDelete) ;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Btn Clicked...");
                AlertDialog.Builder builder = new AlertDialog.Builder(StrValueAdd.this);
                builder.setTitle(getString(R.string.lableSave))
                        .setMessage(getString(R.string.dialogSave))
                        .setPositiveButton(getString(R.string.lableSave), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DBHandler dbHandler = DBHandler.open(StrValueAdd.this) ;
                                if ("update".equals(extraMode)) {
                                    dbHandler.updateStrValue(Integer.parseInt(_id), editStrValue.getText().toString(), "Y") ;
                                } else {
                                    long _id = dbHandler.insertStrValue(editStrValue.getText().toString());
                                }
                                dbHandler.close();

                                editStrValue.setText("");
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.lableCancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();

            }
        });

        btnDelte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnDelte Clicked...");
                AlertDialog.Builder builder = new AlertDialog.Builder(StrValueAdd.this);
                builder.setTitle(getString(R.string.lableDelete))
                        .setMessage(getString(R.string.dialogDelete))
                        .setPositiveButton(getString(R.string.lableDelete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DBHandler dbHandler = DBHandler.open(StrValueAdd.this) ;
                                if ("update".equals(extraMode)) {
                                    dbHandler.updateStrValue(Integer.parseInt(_id), editStrValue.getText().toString(), "N") ;
                                } else {
                                    utilToast.makeToast(StrValueAdd.this, getString(R.string.msgNoDelete), Toast.LENGTH_LONG).show();
                                }
                                dbHandler.close();

                                editStrValue.setText("");
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.lableCancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        });
    }
}
