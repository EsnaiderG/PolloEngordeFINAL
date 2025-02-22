package com.example.polloengorde.helper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.Set;


public class EnhancedSharedPreferences implements SharedPreferences {

    private static EnhancedSharedPreferences enhancedSharedPreferences = null;
    private final SharedPreferences _sharedPreferences;

    private EnhancedSharedPreferences(SharedPreferences sharedPreferences) {
        _sharedPreferences = sharedPreferences;
    }

    public static EnhancedSharedPreferences getInstance(Context context, String prefsName) {
        if(enhancedSharedPreferences == null){
            enhancedSharedPreferences = new EnhancedSharedPreferences(context.getSharedPreferences(prefsName, Context.MODE_PRIVATE));
        }

        return enhancedSharedPreferences;
    }

    @Override
    public Map<String, ?> getAll() {
        return _sharedPreferences.getAll();
    }

    @Override
    public String getString(String key, String defValue) {
        return _sharedPreferences.getString(key, defValue);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return _sharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return _sharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return _sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return _sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return _sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return _sharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return new Editor(_sharedPreferences.edit());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        _sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        _sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public Double getDouble(String key, Double defValue) {
        return Double.longBitsToDouble(_sharedPreferences.getLong(key, Double.doubleToRawLongBits(defValue)));
    }

    public static class Editor implements SharedPreferences.Editor {

        private final SharedPreferences.Editor _editor;

        public Editor(SharedPreferences.Editor editor) {
            _editor = editor;
        }

        private Editor ReturnEditor(SharedPreferences.Editor editor) {
            if(editor instanceof Editor)
                return (Editor)editor;
            return new Editor(editor);
        }

        @Override
        public Editor putString(String key, String value) {
            return ReturnEditor(_editor.putString(key, value));
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            return ReturnEditor(_editor.putStringSet(key, values));
        }

        @Override
        public Editor putInt(String key, int value) {
            return ReturnEditor(_editor.putInt(key, value));
        }

        @Override
        public Editor putLong(String key, long value) {
            return ReturnEditor(_editor.putLong(key, value));
        }

        @Override
        public Editor putFloat(String key, float value) {
            return ReturnEditor(_editor.putFloat(key, value));
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            return ReturnEditor(_editor.putBoolean(key, value));
        }

        @Override
        public Editor remove(String key) {
            return ReturnEditor(_editor.remove(key));
        }

        @Override
        public Editor clear() {
            return ReturnEditor(_editor.clear());
        }

        @Override
        public boolean commit() {
            return _editor.commit();
        }

        @Override
        public void apply() {
            _editor.apply();
        }

        public Editor putDouble(String key, double value) {
            return new Editor(_editor.putLong(key, Double.doubleToRawLongBits(value)));
        }
    }
}