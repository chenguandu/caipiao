package cn.asbest.caipiao.factory;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**支持对数据加工处理
 * Created by chenyanlan on 2016/11/4.
 */

public final class GsonSecureConverterFactory extends Converter.Factory {

    /**
     * 实现此接口对返回结果进行加工处理，如解密
     */
    public interface ResultHandler{
        String handle(ResponseBody value);
    }

    ResultHandler mResultHandler = null;

    /**
     * 对返回结果进行加工处理，如解密
     * @param handler
     * @return
     */
    public static GsonSecureConverterFactory create(ResultHandler handler) {
        return create(new Gson(), handler);
    }

    public static GsonSecureConverterFactory create(Gson gson, ResultHandler handler) {
        GsonSecureConverterFactory factory = new GsonSecureConverterFactory(gson);
        factory.mResultHandler = handler;
        return factory;
    }

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonSecureConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonSecureConverterFactory create(Gson gson) {
        return new GsonSecureConverterFactory(gson);
    }

    private final Gson gson;

    private GsonSecureConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        GsonSecureResponseBodyConverter converter = new GsonSecureResponseBodyConverter(gson, adapter){
            @Override
            protected String getValue(ResponseBody value) {
                if (mResultHandler != null){
                    return mResultHandler.handle(value);
                }
                return null;
            }
        };
        return converter;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonSecureRequestBodyConverter<>(gson, adapter);
    }
}
