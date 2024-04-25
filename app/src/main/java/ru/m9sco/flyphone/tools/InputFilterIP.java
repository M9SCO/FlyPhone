package ru.m9sco.flyphone.tools;

import android.text.InputFilter;
import android.text.Spanned;

// See: https://stackoverflow.com/questions/31529651/edittext-android-filter-for-ip-address
public class InputFilterIP implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        if (end > start) {
            String r = dest.toString().substring(0, dstart) + source.subSequence(start, end) + dest.toString().substring(dend);
            if (!r.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                return "";
            } else {
                String[] splits = r.split("\\.");
                for (String split : splits) {
                    if (Integer.valueOf(split) > 255) return "";
                }
            }
        }
        return null;
    }
}
