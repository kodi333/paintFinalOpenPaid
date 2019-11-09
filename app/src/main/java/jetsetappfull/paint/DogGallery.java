package jetsetappfull.paint;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class DogGallery extends AppCompatActivity implements View.OnClickListener {

    private static boolean pictureChosen = false;
    private ImageButton playMusicDogGalleryButton;

    public static boolean isPictureChosen() {
        return pictureChosen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_gallery);

        ImageButton princess = findViewById(R.id.princess);
        ImageButton cats = findViewById(R.id.cats);
        ImageButton unicorn = findViewById(R.id.unicorn);

        princess.setOnClickListener(this);
        cats.setOnClickListener(this);
        unicorn.setOnClickListener(this);

        playMusicDogGalleryButton = findViewById(R.id.playMusicDogGallery);
        playMusicDogGalleryButton.setOnClickListener(this);

        for (int i = 1; i <= 13; i++) {

            String overwrittenImageName = Save.getNameOfOverwrittenFile() + "dog" + i;
            String orgImageName = "dog" + i;

            Log.i("orgImageName", "orgImageName " + orgImageName);
            Log.i("overwrittenImageName", "overwrittenImageName " + overwrittenImageName);

            File file = new File(Save.getFile_path() + "/" + overwrittenImageName + ".png");
            if (file.exists()) {

                int imageId = getResources().getIdentifier(orgImageName, "id", getPackageName());

                ImageView thumbPicture = findViewById(imageId);

                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path() + "/" + overwrittenImageName + ".png"));
//        }

                Log.i("dogIdInt", "dogIdInt " + imageId);
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cats:
//                finish();
                Intent intentApp = new Intent(DogGallery.this,
                        CatGallery.class);
//                finishAffinity();
//                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                DogGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_cat_gallery);
                Log.v("TAG", "catsStart");
                break;

            case R.id.princess:
//                finish();
                intentApp = new Intent(DogGallery.this,
                        PrincessGallery.class);
//                finishAffinity();
                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                DogGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_princess_gallery);
                Log.v("TAG", "princessStart");
                break;

            case R.id.unicorn:
//                finish();
                intentApp = new Intent(DogGallery.this,
                        UnicornGallery.class);
//                finishAffinity();
                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                DogGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_unicorn_gallery);
                Log.v("TAG", "unicornStart");
                break;

            case R.id.playMusicDogGallery:
                //check if music runs
                AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                if (manager != null) {
                    if (manager.isMusicActive()) {
                        Log.d("Music", "stop.");
                        stopService(new Intent(this, MusicService.class));
                        playMusicDogGalleryButton.setBackgroundResource(R.drawable.no_music);
                    } else {
                        Log.d("Music", "started.");
                        startService(new Intent(this, MusicService.class));
                        playMusicDogGalleryButton.setBackgroundResource(R.drawable.music);
                    }
                }

        }


    }

    public void setBackground(View v) {
        pictureChosen = true;
        ImageView x = (ImageView) v;
        String buttonId = String.valueOf(x.getTag());
//        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save.getNameOfFolder();

        Intent mainActivity = new Intent(DogGallery.this, MainActivity.class);
//        jesli istnieje OverwrittenKidsPaint + buttonid wtedy putExtra("picture", "Overwritten" + buttoin
        File file = new File(Save.getFile_path(), Save.getNameOfOverwrittenFile() + buttonId + ".png");

        if (file.exists()) {
            mainActivity.putExtra("picture", Save.getNameOfOverwrittenFile() + buttonId);
            Log.i("Found", "File found : Overwritten" + buttonId);
        } else {
            mainActivity.putExtra("picture", buttonId);
            Log.i("Found", "File not found : Overwritten" + buttonId);
        }
        startActivity(mainActivity);

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
