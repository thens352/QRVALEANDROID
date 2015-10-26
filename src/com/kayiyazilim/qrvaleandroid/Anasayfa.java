package com.kayiyazilim.qrvaleandroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Anasayfa extends Activity implements OnClickListener,
		OnItemClickListener {
	// Deðiþken tanýmlamalarý
	private TextView view;
	private ListView bildirimler;
	private Button menu;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Fullscreen yapma
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Layout baðlama
		setContentView(R.layout.activity_anasayfa);
		// Deðiþkenlerle layoutu baðlama
		view = (TextView) findViewById(R.id.twAdSoyad);
		menu = (Button) findViewById(R.id.bMenu);
		menu.setOnClickListener(this);
		try {
			view.setText(getIntent().getStringExtra("adSoyad"));
			bildirimler = (ListView) findViewById(R.id.listView1);
			bildirimler.setOnItemClickListener(this);
			// Þuan bir teslim olup olmadýðýný kontrol etme
			suankiTeslim();
			// Serverdan her 10 snde bir bildirimleri güncelleme
			final Handler handler = new Handler();
			timer = new Timer();
			TimerTask doAsynchronousTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							try {
								class BildirimRestful extends RestfulErisim {
									@Override
									protected void onPostExecute(String results) {
										super.onPostExecute(results);
										if (results != null) {
											try {
												JSONObject result = new JSONObject(
														results);

												JSONArray bildirimlerJSON = result
														.getJSONArray("bildirimler");

												List<String> bildirimList = new ArrayList<String>();

												for (int i = 0; i < bildirimlerJSON
														.length(); i++) {
													JSONObject bildirim = bildirimlerJSON
															.getJSONObject(i);
													String bildirimS = bildirim
															.getString("hareketID")
															+ ". numaralý hareket bilgileri\n"
															+ bildirim
																	.getString("plaka")
															+ " "
															+ bildirim
																	.getString("marka")
															+ " "
															+ bildirim
																	.getString("model")
															+ " "
															+ bildirim
																	.getString("renk")
															+ "\nTeslim almak için dokunun.";

													bildirimList.add(bildirimS);
												}

												try {
													ArrayAdapter<String> adapter = new ArrayAdapter<String>(
															getApplicationContext(),
															android.R.layout.simple_list_item_2,
															android.R.id.text1,
															bildirimList);
													bildirimler
															.setAdapter(adapter);
												} catch (Exception e) {
													e.printStackTrace();
												}

											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									}
								}
								;
								new BildirimRestful()
										.execute("http://10.0.3.2:8080/QRVALE/rest/hareketservice/bildirimler");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			};
			timer.schedule(doAsynchronousTask, 0, 10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		timer.cancel();
		finish();
	}

	public void suankiTeslim() {
		class SuankiRestful extends RestfulErisim {
			@Override
			protected void onPostExecute(String results) {
				super.onPostExecute(results);
				Button suanki = (Button) findViewById(R.id.bSuanki);
				try {
					final JSONObject result = new JSONObject(results);
					// Eðer þuan bir teslim iþlemi varsa
					if (result.getBoolean("result")) {
						suanki.setText("Teslim almýþ olduðunuz bir hareket bulunmaktadýr.");
						suanki.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent ayrintilar = new Intent(
										getApplicationContext(),
										HareketAyrintilari.class);
								try {
									ayrintilar.putExtra("hareketID",
											result.getString("hareketID"));
									startActivity(ayrintilar);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
					} else {// yoksa
						suanki.setClickable(false);
						suanki.setText("Suanda teslim alýnmýþ bir hareket bulunmamaktadýr.");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		;
		new SuankiRestful()
				.execute("http://10.0.3.2:8080/QRVALE/rest/hareketservice/suanki?id="
						+ getIntent().getStringExtra("id"));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bMenu:
			openOptionsMenu();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ilk_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.cikis:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String value = ((String) parent.getItemAtPosition(position));
		final String hareketID = value.substring(0, value.indexOf(". n"));
		class TeslimOnay extends RestfulErisim {
			@Override
			protected void onPostExecute(String results) {
				super.onPostExecute(results);
				try {
					JSONObject result = new JSONObject(results);
					if (result.getBoolean("result")) {
						Intent ayrinti = new Intent(getApplicationContext(),
								HareketAyrintilari.class);
						ayrinti.putExtra("hareketID", hareketID);
						Button suanki = (Button) findViewById(R.id.bSuanki);
						suanki.setText("Teslim almýþ olduðunuz bir hareket bulunmaktadýr.");
						suanki.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent ayrintilar = new Intent(
										getApplicationContext(),
										HareketAyrintilari.class);

								ayrintilar.putExtra("hareketID", hareketID);
								startActivity(ayrintilar);
							}
						});
						startActivity(ayrinti);
					} else
						Toast.makeText(getApplicationContext(),
								result.getString("error"), Toast.LENGTH_SHORT)
								.show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		;
		new TeslimOnay()
				.execute("http://10.0.3.2:8080/QRVALE/rest/hareketservice/teslimAlindi?hareketID="
						+ hareketID
						+ "&verenValeID="
						+ getIntent().getStringExtra("id"));
	}
}
