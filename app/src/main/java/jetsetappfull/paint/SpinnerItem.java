package jetsetappfull.paint;

public class SpinnerItem {
    private String mCountryName;
    private int mFlagImage;

    SpinnerItem(String countryName, int flagImage) {
        mCountryName = countryName;
        mFlagImage = flagImage;
    }

    String getIconName() {
        return mCountryName;
    }

    int getIconImage() {
        return mFlagImage;
    }
}
