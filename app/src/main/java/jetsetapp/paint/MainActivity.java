package jetsetapp.paint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static int myBlack = Color.parseColor("#001A00");
    protected static ImageButton zoomOutButton;
    protected static ImageButton undoButton;
    protected static ImageButton redoButton;
    protected static ImageButton clearButton;
    protected static boolean fillFloodSelected = true;
    protected static ScaleGestureDetector mScaleGestureDetector;
    //button list below
    private static int[] btn_id = {R.id.rateMe, R.id.playMusic, R.id.addPicture, R.id.floodFill, R.id.erase, R.id.save};
    private static int currentLayout = R.layout.activity_cat_gallery;
    public static ImageButton[] btn = new ImageButton[btn_id.length];
    private static Bitmap newBitmap;
    private static String pictureName;
    protected CanvasView canvasView;
    int lastChosenColor = myBlack;
    ProgressDialog progressDialog;
    private Bitmap mBitmap;
    private ImageButton playMusicButton;
    private ImageView rectangle;
    private ImageButton btn_unfocus;
    private GradientDrawable shapeDrawable;
    Spinner spinner;
    private ArrayList<SpinnerItem> spinnerBrushList;

    HashMap<Integer, Class> classMap = new HashMap<Integer, Class>();

    public static String getPictureName() {
        return pictureName;
    }

    public static void setPictureName(String pictureName) {
        MainActivity.pictureName = pictureName;
    }

    public static Bitmap getNewBitmap() {
        return newBitmap;
    }

    public static boolean isFillFloodSelected() {
        return fillFloodSelected;
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

    public static Bitmap decodeSampledBitmapFromFile(String pathFile, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathFile, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathFile, options);
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

    public static void setCurrentLayout(int currentLayout) {
        MainActivity.currentLayout = currentLayout;
    }

    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, MusicService.class));


    }

    @Override
    public void onBackPressed() {
        new LoadViewTask().execute();
    }

    private void setFocus(ImageButton btn_unfocus, ImageButton btn_focus) {

        btn_focus.getBackground().clearColorFilter();

        RelativeLayout.LayoutParams focus_params = (RelativeLayout.LayoutParams) btn_focus.getLayoutParams();
        focus_params.height = (int) (focus_params.height * 1.1);
        focus_params.width = (int) (focus_params.width * 1.2);
        btn_focus.setLayoutParams(focus_params);

        int unfocus_height = btn_focus.getHeight();
        int unfocus_width = btn_focus.getWidth();


        RelativeLayout.LayoutParams unfocus_params = (RelativeLayout.LayoutParams) btn_unfocus.getLayoutParams();
        unfocus_params.height = unfocus_height;
        unfocus_params.width = unfocus_width;
        btn_unfocus.setLayoutParams(unfocus_params);
        btn_unfocus.getBackground().setColorFilter(0x90ffffff, PorterDuff.Mode.MULTIPLY);

        this.btn_unfocus = btn_focus;

    }

    @SuppressLint("InflateParams")
    @Override
    public void onClick(View v) {

        if (CanvasView.isZoomed()) {

            setFocus(btn_unfocus, (ImageButton) findViewById(R.id.floodFill));
            fillFloodSelected = true;
            canvasView.changeStroke(0);

            switch (v.getId()) {

                case R.id.addPicture:
                    Intent intentApp = new Intent(MainActivity.this,
                            classMap.get(currentLayout));
                    MainActivity.this.startActivity(intentApp);
                    MainActivity.setCurrentLayout(currentLayout);
                    setFocus(btn_unfocus, (ImageButton) findViewById(v.getId()));
                    fillFloodSelected = true;
                    saveFileToInternalStorage(v);
                    break;
                case R.id.floodFill:
                    setFocus(btn_unfocus, findViewById(v.getId()));
                    fillFloodSelected = true;
                    canvasView.changeStroke(0);
                    spinner.setSelection(3); //set spinner to default
                    break;
                case R.id.playMusic:
                    //check if music runs
                    AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                    if (manager != null) {
                        if (manager.isMusicActive()) {
                            Log.d("Music", "stop.");
                            stopService(new Intent(this, MusicService.class));
                            playMusicButton.setBackgroundResource(R.drawable.no_music);
                        } else {
                            Log.d("Music", "started.");
                            startService(new Intent(this, MusicService.class));
                            playMusicButton.setBackgroundResource(R.drawable.music);
                        }
                    }
                    break;

                case R.id.rateMe:
                    Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.prompt_rate_me);
                    dialog.setCanceledOnTouchOutside(true);
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    ImageButton ok_button2 = dialog.findViewById(R.id.rate_button);
                    ok_button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            // To count with Play market backstack, After pressing back button,
                            // to taken back to our application, we need to add following flags to intent.
                            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                            }
//                            ad2.dismiss();
                        }

                    });
                    dialog.show();

                    break;

            }
        } else {

            switch (v.getId()) {
                case R.id.erase:

                    setFocus(btn_unfocus, (ImageButton) findViewById(v.getId()));
//                    LayoutInflater layoutInflater = (LayoutInflater) this
//                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    View promptView = null;
//                    if (layoutInflater != null) {
//                        promptView = layoutInflater.inflate(R.layout.prompt_clear_all_colors, null);
//                    }
//                    final AlertDialog.Builder alertD = new AlertDialog.Builder(this);
//                    alertD.setView(promptView);
//                    final AlertDialog ad = alertD.show();
                    final Dialog dialog_erase = new Dialog(this, android.R.style.Theme_Dialog);
                    dialog_erase.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog_erase.setContentView(R.layout.prompt_clear_all_colors);
                    dialog_erase.setCanceledOnTouchOutside(true);
                    Objects.requireNonNull(dialog_erase.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    ImageButton ok_button = dialog_erase.findViewById(R.id.ok_button);
                    ImageButton no_button = dialog_erase.findViewById(R.id.no_button);

                    ok_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (pictureName.contains(Save.getNameOfOverwrittenFile())) {
                                pictureName = MainActivity.getPictureName();
                                pictureName = pictureName.substring(Save.getNameOfOverwrittenFile().length());
                                setPictureName(pictureName);
                                setCanvasViewBackground();
                                canvasView.invalidate();

                            }

//                            Log.e("pictureName","pictureName " + pictureName + ":");
//                             Log.e("pictureName","pictureNames " + Save.getNameOfOverwrittenFile());


                            canvasView.clearCanvasNoPrompt();
                            dialog_erase.dismiss();
                        }

                    });
                    no_button.setOnClickListener(v1 -> dialog_erase.dismiss());
                    dialog_erase.show();
                    break;

                case R.id.addPicture:
                    Intent intentApp = new Intent(MainActivity.this,
                            classMap.get(currentLayout));
                    MainActivity.this.startActivity(intentApp);
                    MainActivity.setCurrentLayout(currentLayout);
                    setFocus(btn_unfocus, findViewById(v.getId()));
                    fillFloodSelected = true;
                    saveFileToInternalStorage(v);
                    break;

                case R.id.save:
                    setFocus(btn_unfocus, findViewById(v.getId()));
                    saveFile(v);
                    break;

                case R.id.floodFill:
                    setFocus(btn_unfocus, findViewById(v.getId()));
                    fillFloodSelected = true;
                    canvasView.changeStroke(0);
                    spinner.setSelection(3);
                    break;

                case R.id.playMusic:
                    //check if music runs
                    AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                    if (manager != null) {
                        if (manager.isMusicActive()) {
                            Log.d("Music", "stop.");
                            stopService(new Intent(this, MusicService.class));
                            playMusicButton.setBackgroundResource(R.drawable.no_music);
                        } else {
                            Log.d("Music", "started.");
                            startService(new Intent(this, MusicService.class));
                            playMusicButton.setBackgroundResource(R.drawable.music);
                        }
                    }
                    break;

                case R.id.rateMe:
                    final Dialog dialog_rate = new Dialog(this, android.R.style.Theme_Dialog);
                    dialog_rate.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog_rate.setContentView(R.layout.prompt_rate_me);
                    dialog_rate.setCanceledOnTouchOutside(true);
                    Objects.requireNonNull(dialog_rate.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    ImageButton ok_button2 = dialog_rate.findViewById(R.id.rate_button);
                    ok_button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            // To count with Play market backstack, After pressing back button,
                            // to taken back to our application, we need to add following flags to intent.
                            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                            }
                        }

                    });
                    ImageButton close_rate_button = dialog_rate.findViewById(R.id.clear_rate_button);
                    close_rate_button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog_rate.dismiss();
                        }
                    });
                    dialog_rate.show();

                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            canvasView.buildDrawingCache();
            mBitmap = Bitmap.createBitmap(canvasView.getDrawingCache());
            Save savefile = new Save();
            savefile.SaveImage(this, mBitmap);
            canvasView.destroyDrawingCache();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasView = findViewById(R.id.canvas);
        canvasView.setDrawingCacheEnabled(true);

        zoomOutButton = findViewById(R.id.zoomOutButton);
        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        clearButton = findViewById(R.id.clearButton);

        //this map is used to display the proper/current View when clicking addPicture button
        classMap.put(R.layout.activity_princess_gallery, PrincessGallery.class);
        classMap.put(R.layout.activity_cat_gallery, CatGallery.class);
        classMap.put(R.layout.activity_dog_gallery, DogGallery.class);
        classMap.put(R.layout.activity_unicorn_gallery, UnicornGallery.class);

        View erase = findViewById(R.id.erase);
        erase.setOnClickListener(this);

        View saveFileButton = findViewById(R.id.save);
        saveFileButton.setOnClickListener(this);

        View addPictureButton = findViewById(R.id.addPicture);
        addPictureButton.setOnClickListener(this);

        View floodFillButton = findViewById(R.id.floodFill);
        floodFillButton.setOnClickListener(this);

        View horizontalPaintsView = findViewById(R.id.HorizontalScroll);
        horizontalPaintsView.setHorizontalScrollBarEnabled(false);

        playMusicButton = findViewById(R.id.playMusic);
        playMusicButton.setOnClickListener(this);

        spinner = findViewById(R.id.spinner_brushes);

        //get number of picture eg cat12 or overwrittencat12 to be used in setCanvasViewBackground or clear method
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
        pictureName = extras.getString("picture");
        }

        //pinchZoom
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener(canvasView));

        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            if (!manager.isMusicActive()) {
                playMusicButton.setBackgroundResource(R.drawable.no_music);
            } else {
                playMusicButton.setBackgroundResource(R.drawable.music);
            }
        }
        // Set background to all buttons // iterate loop thru all buttons
        setTransparentBackgroundToAllButtons();

        // Set transparent background for Spinner as well

        spinner.getBackground().setColorFilter(0x90ffffff, PorterDuff.Mode.MULTIPLY);

        //take the parameters - transparency etc from first button
        btn_unfocus = btn[0];

        //add new galleries here
        if (CatGallery.isPictureChosen() || UnicornGallery.isPictureChosen() || DogGallery.isPictureChosen() || PrincessGallery.isPictureChosen()) {
            setCanvasViewBackground();
        }

        rectangle = findViewById(R.id.circle);
        Drawable background = rectangle.getBackground();
        shapeDrawable = (GradientDrawable) background;
        shapeDrawable.setColor(Color.parseColor("#E6B0AA"));

        //dropDown Spinner of brush sizes
        initList();

        // Truncate the list
        SpinnerAdapter mAdapter = new SpinnerAdapter(this, spinnerBrushList) {
            @Override
            public int getCount() {
                return (spinnerBrushList.size() - 1); // Truncate the list
            }
        };
        spinner.setAdapter(mAdapter);
        spinner.setSelection(3);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                SpinnerItem clickedItem = (SpinnerItem) parent.getItemAtPosition(position);
                String clickedItemName = clickedItem.getIconName();

                //set transpaterent bg to all buttons when spinner is clicked
                setTransparentBackgroundToAllButtons();
                //adjust brush to selected brush size
                int whiteColorValue = Color.WHITE;
                switch (clickedItemName) {
                    case "small":
                        fillFloodSelected = false;
                        canvasView.changeStroke(3F);
                        if (canvasView.getColor() == whiteColorValue) {
                            int lastColor = lastChosenColor;
                            canvasView.changeColor(lastColor);
                        }
                        break;
                    case "medium":
                        fillFloodSelected = false;
                        canvasView.changeStroke(10F);
                        if (canvasView.getColor() == whiteColorValue) {
                            int lastColor = lastChosenColor;
                            canvasView.changeColor(lastColor);
                        }
                        break;
                    case "big":
                        fillFloodSelected = false;
                        canvasView.changeStroke(30F);
                        if (canvasView.getColor() == whiteColorValue) {
                            int lastColor = lastChosenColor;
                            canvasView.changeColor(lastColor);
                        }
                        break;
                    case "no_selection": // no selection is used to make floodFill the default option
//                        when opening the picture, the list needs to be then truncated by 1,
//                        so the "no selection" picture does not appear in drop down list
//                        fillFloodSelected = true;

                        break;

                }

            }


        });

    }

    public void setCanvasViewBackground() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        String file_path = Save.getFile_path() + "/" + pictureName + ".png";
        //        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save.getNameOfFolder() + "/" + "OverwrittenKidsPaintcat12.png";

        if (pictureName.contains(Save.getNameOfOverwrittenFile())) {
            newBitmap = decodeSampledBitmapFromFile(file_path, width, height);
            Log.e("pictureName", "pictureNameExist " + pictureName + ":");
        } else {
            newBitmap = decodeSampledBitmapFromResource(getResources(), getResources().getIdentifier(pictureName, "drawable", getPackageName()), width, height);
            Log.e("pictureName", "pictureNameNew " + pictureName + ":");
        }

        canvasView.setNewBitmap(newBitmap);
    }

    public void setCanvasColor(View v) {
        ImageButton x = (ImageButton) v;
        String buttonTag = String.valueOf(x.getTag());

        canvasView.changeColor(Color.parseColor(buttonTag));
        lastChosenColor = Color.parseColor(buttonTag);
        if (rectangle != null) {
            Drawable background = rectangle.getBackground();
            shapeDrawable = (GradientDrawable) background;
            shapeDrawable.setColor(canvasView.getColor());
        }
    }

    public void clearCanvas(View v) {
        canvasView.clearCanvas();
    }

    public void saveFile(View v) {

        if (canvasView != null) {
            zoomOut(v);
            canvasView.buildDrawingCache();
            mBitmap = Bitmap.createBitmap(canvasView.getDrawingCache());
        }

        Save savefile = new Save();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                savefile.SaveImage(this, mBitmap);
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                //                    return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            savefile.SaveImage(this, mBitmap);
        }

        canvasView.destroyDrawingCache();

    }

    public void saveFileToInternalStorage(View v) {

        if (canvasView != null) {
            zoomOut(v);
            canvasView.buildDrawingCache();
            mBitmap = Bitmap.createBitmap(canvasView.getDrawingCache());

        }

        Save savefile = new Save();

        savefile.writeFileOnInternalStorage(this, mBitmap, pictureName);

        canvasView.destroyDrawingCache();

    }

    public void zoomOut(View v) {
        canvasView.zoomOut();
        canvasView.invalidate();
    }

    // Undo  Draw
    public void undo(View v) {
        canvasView.undoLastDraw();
        canvasView.invalidate();
    }

    // Redo Draw
    public void redo(View v) {
        canvasView.redoLastDraw();
        canvasView.invalidate();
    }

    //initList is part of dropDown Spinner
    private void initList() {
        spinnerBrushList = new ArrayList<>();
        spinnerBrushList.add(new SpinnerItem("small", R.drawable.pencil));
        spinnerBrushList.add(new SpinnerItem("medium", R.drawable.marker));
        spinnerBrushList.add(new SpinnerItem("big", R.drawable.roll));
        spinnerBrushList.add(new SpinnerItem("no_selection", R.drawable.pencil));//this is only to cheat the spinner
        // as it selects the first item by default, I put o to override incorrect missing FloodFill selection
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadViewTask extends AsyncTask<Void, Integer, Void> {

        //Before running code in separate thread
        @Override
        protected void onPreExecute() {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            //            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //            //Display the progress dialog
            ProgressDialog.show(MainActivity.this, "", "Loading...");

        }

        @Override
        protected Void doInBackground(Void... params) {

            Intent mainGallery = new Intent(MainActivity.this, CatGallery.class);
            MainActivity.this.startActivity(mainGallery);
            return null;
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values) {
            //set the current progress of the progress dialog
            progressDialog.setProgress(values[0]);
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result) {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View
            setContentView(currentLayout);
        }
    }

    private void setTransparentBackgroundToAllButtons() {
        int mainButtonHeight = findViewById(btn_id[0]).getLayoutParams().height;
        int mainButtonWidth = findViewById(btn_id[0]).getLayoutParams().width;

        RelativeLayout.LayoutParams focus_params;
        for (int i = 0; i <= btn.length - 1; i++) {
            btn[i] = findViewById(btn_id[i]);
            btn[i].getBackground().setColorFilter(0x90ffffff, PorterDuff.Mode.MULTIPLY);
            focus_params = (RelativeLayout.LayoutParams) btn[i].getLayoutParams();
            focus_params.height = mainButtonHeight;
            focus_params.width = mainButtonWidth;
            btn[i].setLayoutParams(focus_params);
            btn[i].setOnClickListener(this);
        }
    }

}

