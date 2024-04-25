package ru.m9sco.flyphone.tools;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    protected int min;
    protected int max;

    public InputFilterMinMax(int m, int M)
    {
        min = m;
        max = M;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        String r = dest.toString().substring(0, dstart) + source.subSequence(start, end) + dest.toString().substring(dend);

        if (!r.matches("^([-]?|[-]?[1-9]\\d*|[0]|[1-9]?[1-9]\\d*)")) return "";
        if(r.isEmpty()||r.equals("-")) return null;
        if ((Integer.parseInt(r) > max || Integer.parseInt(r) < min)) return "";

        return null;
    }
}