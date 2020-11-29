package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OwnerScanSelect extends AppCompatActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_scan_select);
		
		Button handOverBtn = findViewById(R.id.Owner_HandOverBtn);
		Button receiveBtn = findViewById(R.id.Owner_ReceivingBtn);
		
		handOverBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent handOverScan = new Intent(getApplicationContext(), barcodeScanner.class);
				handOverScan.putExtra("ScanType", "OwnerHandOver");
				startActivity(handOverScan);
				finish();
			}
		});
		receiveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent receiveScan = new Intent(getApplicationContext(), barcodeScanner.class);
				receiveScan.putExtra("ScanType", "OwnerReceive");
				startActivity(receiveScan);
				finish();
			}
		});
	}
	
	
}