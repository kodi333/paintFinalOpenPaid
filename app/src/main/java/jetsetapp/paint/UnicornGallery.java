package jetsetapp.paint;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class UnicornGallery extends AppCompatActivity implements View.OnClickListener {

    private static boolean pictureChosen = false;
    ImageView dogs;
    ImageView cats;
    ImageView princess;
    private ImageButton playMusicUnicornGalleryButton;

    public static boolean isPictureChosen() {
        return pictureChosen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unicorn_gallery);

        dogs = (ImageButton) findViewById(R.id.dogs);
        cats = (ImageButton) findViewById(R.id.cats);
        princess = (ImageButton) findViewById(R.id.princess);

        dogs.setOnClickListener(this);
        cats.setOnClickListener(this);
        princess.setOnClickListener(this);

        playMusicUnicornGalleryButton = findViewById(R.id.playMusicUnicornGallery);
        playMusicUnicornGalleryButton.setOnClickListener(this);

        for (int i = 1; i <= 12; i++) {

            String overwrittenImageName = Save.getNameOfOverwrittenFile() + "uni" + i;
            String orgImageName = "uni" + i;

            Log.i("orgImageName", "orgImageName " + orgImageName);
            Log.i("overwrittenImageName", "overwrittenImageName " + overwrittenImageName);

            File file = new File(Save.getFile_path() + "/" + overwrittenImageName + ".png");
            if (file.exists()) {

                int imageId = getResources().getIdentifier(orgImageName, "id", getPackageName());

                ImageView thumbPicture = findViewById(imageId);

                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path() + "/" + overwrittenImageName + ".png"));
//        }

                Log.i("uniIdInt", "uniIdInt " + imageId);
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cats:
//                finish();
                Intent intentApp = new Intent(UnicornGallery.this,
                        CatGallery.class);
//                finishAffinity();
//                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                UnicornGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_cat_gallery);
                Log.v("TAG", "catsStart");
                break;

            case R.id.princess:
//                finish();
                intentApp = new Intent(UnicornGallery.this,
                        PrincessGallery.class);
//                finishAffinity();
                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                UnicornGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_princess_gallery);
                Log.v("TAG", "princessStart");
                break;

            case R.id.dogs:
//                finish();
                intentApp = new Intent(UnicornGallery.this,
                        DogGallery.class);
//                finishAffinity();
                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                UnicornGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_dog_gallery);
                Log.v("TAG", "dogsStart");
                break;

            case R.id.playMusicUnicornGallery:
                //check if music runs
                AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                if (manager != null) {
                    if (manager.isMusicActive()) {
                        Log.d("Music", "stop.");
                        stopService(new Intent(this, MusicService.class));
                        playMusicUnicornGalleryButton.setBackgroundResource(R.drawable.no_music);
                    } else {
                        Log.d("Music", "started.");
                        startService(new Intent(this, MusicService.class));
                        playMusicUnicornGalleryButton.setBackgroundResource(R.drawable.music);
                    }
                }

        }


    }


    public void gotoPaidApp(View v) {

        pictureChosen = true;

        Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.prompt_buy_app);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageButton ok_button2 = dialog.findViewById(R.id.buy_app_button);
        ok_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=jetsetappfull.paint");
//                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
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
                            Uri.parse("market://details?id=jetsetappfull.paint")));
//                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }
//                            ad2.dismiss();
            }

        });
        ImageButton close_rate_button = dialog.findViewById(R.id.clear_buy_app_button);
        close_rate_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

//        Uri uri = Uri.parse("market://details?id=jetsetappfull.paint");
//        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//        // To count with Play market backstack, After pressing back button,
//        // to taken back to our application, we need to add following flags to intent.
//        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
//                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
//                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        try {
//            startActivity(goToMarket);
//        } catch (ActivityNotFoundException e) {
//            startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("http://play.google.com/store/apps/details?id=jetsetappfull.paint")));
//
//        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MusicService.class)); // this NEEDS to be here without it when you slide
//        away (destroy) the app the music still plays

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }
}
