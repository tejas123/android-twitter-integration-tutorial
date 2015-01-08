package com.twitter.blog;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Button btnAuthorizeTwitter;
	private SocialAuthAdapter socialAuthAdapter;
	public static final String PATH = Utils.getPath();
	private final int SELECT_IMAGE_FROM_GALLERY = 0, CAPTURE_IMAGE = 1;
	private ImageView ivUploadImage;
	private EditText etMessage;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setWidgetReference();
		bindWidgetEvents();
	}

	// this method is set reference of widgets from xml files to java
	private void setWidgetReference() {
		btnAuthorizeTwitter = (Button) findViewById(R.id.btnTwitterAuthorize);
		ivUploadImage = (ImageView) findViewById(R.id.ivTwitterImageChoose);
		etMessage = (EditText) findViewById(R.id.etTwitterAuthorize);
	}

	// this method binding the event's of widgets like click
	private void bindWidgetEvents() {
		btnAuthorizeTwitter.setOnClickListener(this);
		ivUploadImage.setOnClickListener(this);
	}

	private void showImageChooseDialog() {
		final CharSequence[] items = { "Choose from Gallery", "Cancel" };
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Choose from Gallery")) {
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent, "Select File"),
							SELECT_IMAGE_FROM_GALLERY);
				}
			}
		});
		builder.show();
	}

	private void initSocialAdapter() {
		// Utils.isOnline method check the internet connection
		if (Utils.isOnline(getApplicationContext())) {
			// Initialize the socialAuthAdapter with ResponseListener
			pd = ProgressDialog.show(MainActivity.this, null, null);
			socialAuthAdapter = new SocialAuthAdapter(new ResponseListener(
					((BitmapDrawable) ivUploadImage.getDrawable()).getBitmap(),
					etMessage.getText().toString()));
			// Add Twitter to set as provider to post on twitter
			socialAuthAdapter.addProvider(Provider.TWITTER, R.drawable.twitter);
			// this line is for Authorize start
			socialAuthAdapter.authorize(MainActivity.this, Provider.TWITTER);
		} else {
			// showing message when internet connection is not available
			Toast.makeText(getApplicationContext(),
					"Check your internet connection..", Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnAuthorizeTwitter) {
			initSocialAdapter();
		} else if (v == ivUploadImage) {
			showImageChooseDialog();
		}
	}

	// this ResponseListener class is for getting responce of post uploading
	private class ResponseListener implements DialogListener {
		Bitmap bitmap;
		String message;

		public ResponseListener(Bitmap bitmap, String message) {
			this.bitmap = bitmap;
			this.message = message;
		}

		@Override
		public void onComplete(final Bundle values) {
			// this method is call when successfull authorization is done
			try {
				socialAuthAdapter.uploadImageAsync(message, "The AppGuruz.png",
						bitmap, 100, new UploadImageListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onError(SocialAuthError error) {
			// this method is call when error is occured in authorization
			if (pd != null && pd.isShowing())
				pd.dismiss();
			Log.d("ShareTwitter", "Authentication Error: " + error.getMessage());
		}

		@Override
		public void onCancel() {
			// this method is call when user cancel Authentication
			if (pd != null && pd.isShowing())
				pd.dismiss();
			Log.d("ShareTwitter", "Authentication Cancelled");
		}

		@Override
		public void onBack() {
			// this method is call when user backpressed from dialog
			if (pd != null && pd.isShowing())
				pd.dismiss();
			Log.d("ShareTwitter", "Dialog Closed by pressing Back Key");
		}
	}

	private final class UploadImageListener implements
			SocialAuthListener<Integer> {

		@Override
		public void onError(SocialAuthError e) {
		}

		@Override
		public void onExecute(String arg0, Integer arg1) {
			Integer status = arg1;
			try {
				if (status.intValue() == 200 || status.intValue() == 201
						|| status.intValue() == 204) {
					if (pd != null && pd.isShowing())
						pd.dismiss();
					Toast.makeText(MainActivity.this, "Image Uploaded",
							Toast.LENGTH_SHORT).show();
				} else {
					if (pd != null && pd.isShowing())
						pd.dismiss();
					Toast.makeText(MainActivity.this, "Image not Uploaded",
							Toast.LENGTH_SHORT).show();
				}

			} catch (NullPointerException e) {
				if (pd != null && pd.isShowing())
					pd.dismiss();
				Toast.makeText(MainActivity.this, "Image not Uploaded",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE_FROM_GALLERY) {
			ivUploadImage.setImageBitmap(BitmapFactory.decodeFile(Utils
					.getImagePath(data, MainActivity.this)));
		}
	}
}