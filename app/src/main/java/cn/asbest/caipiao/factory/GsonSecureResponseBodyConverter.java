package cn.asbest.caipiao.factory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import static okhttp3.internal.Util.UTF_8;

/**
 * Created by chenyanlan on 2016/11/4.
 */

public class GsonSecureResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonSecureResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    protected String getValue(ResponseBody value){
        return null;
    }

    private Charset charset(MediaType mediaType) {
        return mediaType != null ? mediaType.charset(UTF_8) : UTF_8;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        Reader reader = null;
        String result = getValue(value);
        if (result != null){
            reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes()), charset(value.contentType()));
        } else {
            reader = value.charStream();
        }
        JsonReader jsonReader = gson.newJsonReader(reader);
        try {
            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }
}
