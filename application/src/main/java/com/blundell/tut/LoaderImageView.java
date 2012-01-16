package com.blundell.tut;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import roboguice.RoboGuice;
import roboguice.util.Ln;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Free for anyone to use, just say thanks and share :-)
 * @author Blundell
 *
 */
public class LoaderImageView extends LinearLayout{

	private static final int COMPLETE = 0;
	private static final int FAILED = 1;
	private static final int SPIN = 2;

	private Context mContext;
	private Drawable mDrawable;
	private ProgressBar mSpinner;
	private ImageView mImage;
	private Integer errorResource;
	
	/**
	 * This is used when creating the view in XML
	 * To have an image load in XML use the tag 'image="http://developer.android.com/images/dialog_buttons.png"'
	 * To specify the image to display in case of failure, use 'error="R.drawable.something"'
	 * Replacing the url with your desired image
	 * Once you have instantiated the XML view you can call
	 * setImageDrawable(url) to change the image
	 * @param context
	 * @param attrSet
	 */
	public LoaderImageView(final Context context, final AttributeSet attrSet) {
		super(context, attrSet);
		RoboGuice.getInjector(context).injectMembers(this);
		final String url = attrSet.getAttributeValue(null, "image");
		errorResource = attrSet.getAttributeResourceValue(null, "error", 0);
		if(errorResource == 0) errorResource = null;
		if(url != null){
			instantiate(context, url);
		} else {
			instantiate(context, null);
		}
	}
	
	/**
	 * This is used when creating the view programatically
	 * Once you have instantiated the view you can call
	 * setImageDrawable(url) to change the image
	 * @param context the Activity context
	 * @param imageUrl the Image URL you wish to load
	 */
	public LoaderImageView(final Context context, final String imageUrl) {
		super(context);
		instantiate(context, imageUrl);		
	}

	/**
	 *  First time loading of the LoaderImageView
	 *  Sets up the LayoutParams of the view, you can change these to
	 *  get the required effects you want
	 */
	private void instantiate(final Context context, final String imageUrl) {
		mContext = context;
		
		mImage = new ImageView(mContext);
		
		mImage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		mSpinner = new ProgressBar(mContext);
		mSpinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			
		mSpinner.setIndeterminate(true);
		
		addView(mSpinner);
		addView(mImage);
		
		if(imageUrl == null){
			spin();
		}else{
			setImageDrawable(imageUrl);
		}
	}
	
	/**
	 * Show the spinner
	 */
	public void spin(){
		imageLoadedHandler.sendEmptyMessage(SPIN);
	}

	/**
	 * Set's the view's drawable, this uses the Internet to retrieve the image
	 * don't forget to add the correct permissions to your manifest
	 * @param imageUrl the url of the image you wish to load
	 */
	public void setImageDrawable(final String imageUrl) {
		mDrawable = null;
		mSpinner.setVisibility(View.VISIBLE);
		mImage.setVisibility(View.GONE);
		new Thread(){
			public void run() {
				try {
					if(imageUrl==null){
						imageLoadedHandler.sendEmptyMessage(FAILED);
					}else{
						mDrawable = getDrawableFromUrl(imageUrl);
						imageLoadedHandler.sendEmptyMessage(COMPLETE);
					}
				} catch (MalformedURLException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (IOException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				}
			};
		}.start();
	}
	
	public void setImageResource(int resId){
		mDrawable = getResources().getDrawable(resId);
		imageLoadedHandler.sendEmptyMessage(COMPLETE);
	}
	
	public void setErrorResource(int resId){
		this.errorResource = resId;
	}
	
	/**
	 * Callback that is received once the image has been downloaded
	 */
	private final Handler imageLoadedHandler = new Handler(new Callback() {
		public boolean handleMessage(Message msg) {
			Ln.v("Message is: %s", msg.what);
			switch (msg.what) {
			case COMPLETE:
				mImage.setImageDrawable(mDrawable);
				mImage.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			case FAILED:
				if(errorResource == null){
					mImage.setVisibility(View.GONE);
				}else{
					mImage.setImageResource(errorResource);
					mImage.setVisibility(View.VISIBLE);
				}
				mSpinner.setVisibility(View.GONE);
				break;
			case SPIN:
				mImage.setVisibility(View.GONE);
				mSpinner.setVisibility(View.VISIBLE);
				break;
			}
			//the image could be of any dimensions. Set the dimensions of the image to the same dimensions as the LoaderImageView so the LoaderImageView doesn't suddenly drastically grow/shrink when the image loads.
			mImage.setLayoutParams(new LayoutParams(getMeasuredWidth(), getMeasuredHeight()));
			return true;
		}		
	});

	/**
	 * Pass in an image url to get a drawable object
	 * @return a drawable object
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private Drawable getDrawableFromUrl(final String url) throws IOException, MalformedURLException {
		final InputStream inputStream = new URL(url).openStream();
		try{
			return Drawable.createFromStream(inputStream, "name");
		}finally{
			inputStream.close();
		}
	}
}