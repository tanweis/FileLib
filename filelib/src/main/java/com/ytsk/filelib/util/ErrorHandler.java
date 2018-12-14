package com.ytsk.filelib.util;

import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;

/**
 * Created by tan on 2017/11/22.
 */

public class ErrorHandler {

    //http
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TOO_LARGE = 413;

    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;
    private static final int SOCKET_TIME_OUT = 2001;
    //json parse
    private static final int PARSE_ERROR = 10001;
    //network error 连接失败
    private static final int NETWORK_ERROR = 1002;
    private static final int SSL_ERROR = 1003;
    public static final int NETWORK_UNCONNECT_ERROR = 1005;
    private static final int UNKNOWN = 1000;

    public static final int URL_EXCEPTION=1100;

    public static final int SOCKET_ERROR=1102;

    public static ApiException handleException(Throwable e) {
        if (e==null){
            return new ApiException(UNKNOWN,"未知错误");
        }
        ApiException ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ApiException(e);
            switch (httpException.code()) {
                case UNAUTHORIZED:
                    ex.msg = "未认证";
                    break;
                case FORBIDDEN:
                    ex.msg = "服务器拒绝";
                    break;
                case NOT_FOUND:
                    ex.msg = "服务器未找到";
                    break;
                case REQUEST_TOO_LARGE:
                    ex.msg = "请求文件太大";
                    break;
                case REQUEST_TIMEOUT:
                    ex.msg = "请求超时";
                    break;
                case GATEWAY_TIMEOUT:
                    ex.msg = "网管超时";
                    break;
                case INTERNAL_SERVER_ERROR:
                    ex.msg = "服务器内部出错";
                    break;
                case BAD_GATEWAY:
                    ex.msg = "bad gateway";
                    break;
                case SERVICE_UNAVAILABLE:
                    ex.msg = "服务不可用";
                    break;
                default:
                    //ex.code = httpException.code();
                    ex.code = httpException.code();
//                    ex.msg="网络错误";
                    break;
            }
            return ex;
        } else if (e instanceof NoNetworkException){
            ex= new ApiException(e);
            ex.code= NETWORK_UNCONNECT_ERROR;
            ex.msg="网络未连接";
            return ex;
        }else if (e instanceof ApiException) {
            ex = ((ApiException) e);
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
            /*|| e instanceof ParseException*/) {
            ex = new ApiException(e);
            ex.code = PARSE_ERROR;
            ex.msg = "解析错误";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ApiException(e);
            ex.code = NETWORK_ERROR;
            ex.msg = "服务器连接失败";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ApiException(e);
            ex.code = SSL_ERROR;
            ex.msg = "证书验证失败";
            return ex;
        } else if (e instanceof SocketTimeoutException) {
            ex = new ApiException(e);
            ex.code = SOCKET_TIME_OUT;
            ex.msg = "连接超时";
            return ex;
        } else if (e instanceof IllegalArgumentException){
            ex=new ApiException(e);
            if (e.getMessage()!=null&&e.getMessage().startsWith("unexpected url:")){

                ex.code=URL_EXCEPTION;
                ex.msg="网络访问地址错误";
            }
            return ex;
        }else   if (e instanceof SocketException){
            ex = new ApiException(e);
            ex.code=SOCKET_ERROR;
            ex.msg="网络连接异常";
            return ex;
        }else
        {
            ex = new ApiException(e);
            ex.code = UNKNOWN;
            ex.msg = "未知错误:"+e.getMessage();
            return ex;
        }
    }

    public static ApiException handleCodeMsg(int code, String msg) {
        ApiException ex = new ApiException(code, msg);
        switch (code) {
            case UNAUTHORIZED:
                ex.msg = "未认证";
                break;
            case FORBIDDEN:
                ex.msg = "服务器拒绝";
                break;
            case NOT_FOUND:
                ex.msg = "服务器未找到";
                break;
            case REQUEST_TIMEOUT:
                ex.msg = "请求超时";
                break;
            case GATEWAY_TIMEOUT:
                ex.msg = "网管超时";
                break;
            case INTERNAL_SERVER_ERROR:
                ex.msg = "服务器内部出错";
                break;
            case BAD_GATEWAY:
                ex.msg = "bad gateway";
                break;
            case SERVICE_UNAVAILABLE:
                ex.msg = "服务不可用";
                break;
            default:
                break;
        }
        return ex;
    }
}
