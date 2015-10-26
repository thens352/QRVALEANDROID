package com.kayiyazilim.qrvaleandroid;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HareketAyrintilari extends FragmentActivity implements
		OnClickListener, LocationListener {

	private TextView plaka, marka, model, renk;
	private GoogleMap map;
	protected String latLon[];
	private Button tamamlandi;
	protected MarkerOptions arac;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_hareket_ayrintilari);

		plaka = (TextView) findViewById(R.id.tvPlaka);
		marka = (TextView) findViewById(R.id.tvMarka);
		model = (TextView) findViewById(R.id.tvModel);
		renk = (TextView) findViewById(R.id.tvRenk);
		tamamlandi = (Button) findViewById(R.id.bTamamlandi);
		tamamlandi.setOnClickListener(this);

		class HareketRestful extends RestfulErisim {
			@Override
			protected void onPostExecute(String results) {
				// TODO Auto-generated method stub
				super.onPostExecute(results);
				try {
					// Araç bilgileri dolduruluyor
					JSONObject result = new JSONObject(results);
					if (result.getBoolean("result")) {
						plaka.append(result.getString("plaka"));
						marka.append(result.getString("marka"));
						model.append(result.getString("model"));
						renk.append(result.getString("renk"));
						HareketAyrintilari.this.latLon = (result
								.getString("koordinat")).split("/");
						HareketAyrintilari.this.arac = new MarkerOptions();
						HareketAyrintilari.this.arac.title("Hedef");
						HareketAyrintilari.this.arac.position(new LatLng(Double.valueOf(latLon[0]),
								Double.valueOf(latLon[1])));
						map.addMarker(HareketAyrintilari.this.arac);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		;
		// Geçici sýnýf kullanýlarak restful servise eriþiliyor
		new HareketRestful()
				.execute("http://10.0.3.2:8080/QRVALE/rest/hareketservice/hareket?id="
						+ getIntent().getStringExtra("hareketID"));

		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) {
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();

		} else {
			if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragment1, new Fragment()).commit();
				MapFragment fragment = (MapFragment) getFragmentManager()
						.findFragmentById(R.id.fragment1);

				map = fragment.getMap();
				map.setMyLocationEnabled(true);
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				String provider = locationManager.getBestProvider(criteria,
						true);
				Location location = locationManager
						.getLastKnownLocation(provider);
				if (location != null)
					onLocationChanged(location);
				locationManager
						.requestLocationUpdates(provider, 20000, 0, this);
			}
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bTamamlandi:
			class TamamlandiRestful extends RestfulErisim {
				@Override
				protected void onPostExecute(String results) {
					// TODO Auto-generated method stub
					super.onPostExecute(results);

					JSONObject result;
					try {
						result = new JSONObject(results);
						if (result.getBoolean("result")) {
							Intent anasayfa = new Intent(
									getApplicationContext(), Anasayfa.class);
							startActivity(anasayfa);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			;
			new TamamlandiRestful()
					.execute("http://10.0.3.2:8080/QRVALE/rest/hareketservice/tamamlandi?id="
							+ getIntent().getStringExtra("hareketID"));
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ilk_menu, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		// Showing the current location in Google Map
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		map.animateCamera(CameraUpdateFactory.zoomTo(15));

		try {
			//Hedefe yol çizdirme
//			new GoogleMapsLineDraw(makeURL(latitude, longitude,
//					Double.valueOf(latLon[0]), Double.valueOf(latLon[1])), map)
//					.execute();
			map.addMarker(HareketAyrintilari.this.arac);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public String makeURL(double sourcelat, double sourcelog, double destlat,
//			double destlog) {
//		StringBuilder urlString = new StringBuilder();
//		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
//		urlString.append("?origin=");// from
//		urlString.append(Double.toString(sourcelat));
//		urlString.append(",");
//		urlString.append(Double.toString(sourcelog));
//		urlString.append("&destination=");// to
//		urlString.append(Double.toString(destlat));
//		urlString.append(",");
//		urlString.append(Double.toString(destlog));
//		urlString.append("&sensor=false&mode=driving&alternatives=true");
//		return urlString.toString();
//	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
}
