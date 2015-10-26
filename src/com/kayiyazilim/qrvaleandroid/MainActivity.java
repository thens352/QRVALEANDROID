package com.kayiyazilim.qrvaleandroid;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private EditText kullaniciAdi, sifre;
	private Button giris;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		kullaniciAdi = (EditText) findViewById(R.id.etKAdi);
		sifre = (EditText) findViewById(R.id.etKSifre);
		giris = (Button) findViewById(R.id.bGiris);
		giris.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bGiris:
			new LongRunningGetIO()
					.execute("http://10.0.3.2:8080/QRVALE/rest/valeservice/login?kullaniciAdi="
							+ kullaniciAdi.getText().toString()
							+ "&sifre="
							+ sifre.getText().toString());
			break;

		default:
			break;
		}
	}

	private class LongRunningGetIO extends AsyncTask<String, Void, String> {

		protected String getASCIIContentFromEntity(HttpEntity entity)
				throws IllegalStateException, IOException {
			InputStream in = entity.getContent();
			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);
				if (n > 0)
					out.append(new String(b, 0, n));
			}
			return out.toString();
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(params[0]);
			String text = null;
			try {
				HttpResponse response = httpClient.execute(httpGet,
						localContext);
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
			return text;
		}

		protected void onPostExecute(String results) {
			if (results != null) {
				try {
					JSONObject object = new JSONObject(results);
					if (object.getBoolean("result")) {
						Intent anasayfa = new Intent(MainActivity.this,
								Anasayfa.class);
						anasayfa.putExtra("id", object.getString("id"));
						anasayfa.putExtra("adSoyad",
								object.getString("adSoyad"));
						startActivity(anasayfa);
					} else {
						Toast.makeText(getApplicationContext(),
								object.getString("error"), Toast.LENGTH_LONG)
								.show();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
}
