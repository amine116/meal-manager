package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class Animate extends AppCompatActivity {

    private ImageView imgAnimate;
    private final int sleepTime = 71;
    private int numberOfCirculation = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animate);
        initialize();
    }

    private void initialize(){
        imgAnimate = findViewById(R.id.imgAnimate);
        Thread thread = animate();
        thread.start();
    }

    private Thread animate(){
        final Handler handler = new Handler(getApplicationContext().getMainLooper());
        final int imageWidthInPixel = 150, imageHeightInPixels = 150;

        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (numberOfCirculation > 0){

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            /*
                            imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                            R.drawable.i1, imageWidthInPixel, imageHeightInPixels));
                            Log.i("test", "Image: 1");*/

                            imgAnimate.setImageResource(R.drawable.i1);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i2, imageWidthInPixel, imageHeightInPixels));
                           // Log.i("test", "Image: 2");*/
                            imgAnimate.setImageResource(R.drawable.i2);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i3, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 3");*/
                            imgAnimate.setImageResource(R.drawable.i3);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i4, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 4");*/
                            imgAnimate.setImageResource(R.drawable.i4);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i5, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 5");*/
                            imgAnimate.setImageResource(R.drawable.i5);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i6, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 6");*/
                            imgAnimate.setImageResource(R.drawable.i6);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i7, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 7");*/
                            imgAnimate.setImageResource(R.drawable.i7);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i8, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 8");*/
                            imgAnimate.setImageResource(R.drawable.i8);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i9, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 9");*/
                            imgAnimate.setImageResource(R.drawable.i9);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i10, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 10");*/
                            imgAnimate.setImageResource(R.drawable.i10);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i11, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 11");*/
                            imgAnimate.setImageResource(R.drawable.i11);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i12, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 12");*/
                            imgAnimate.setImageResource(R.drawable.i12);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i13, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 13");*/
                            imgAnimate.setImageResource(R.drawable.i13);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i14, imageWidthInPixel, imageHeightInPixels));
                           // Log.i("test", "Image: 14");*/
                            imgAnimate.setImageResource(R.drawable.i14);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(Animate.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    numberOfCirculation--;
                }

                gotoMain();
            }
        });
    }

    private void gotoMain(){
        Intent intent = new Intent(Animate.this, MainActivity.class);
        intent.putExtra("ANIMATED", "YES");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}