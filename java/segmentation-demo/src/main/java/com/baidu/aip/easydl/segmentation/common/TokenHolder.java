/**
 * 引用自 github.com/Baidu-AIP/speech-demo 
 */

package com.baidu.aip.easydl.segmentation.common;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * token的获取类 将accessKey和secretKey换取token，注意有效期保存在expiresAt
 */
public class TokenHolder {

    public static final String SEG_SCOPE = "brain_all_scope";

    /**
     * url , Token的url
     */
    private static final String url = "https://aip.baidubce.com/oauth/2.0/token";

    /**
     * segmentation的权限 scope 是 "brain_all_scope"
     */
    private String scope;

    /**
     * 应用的accessKey
     */
    private String accessKey;

    /**
     * 应用的secretKey
     */
    private String secretKey;

    /**
     * 保存访问接口获取的token
     */
    private String token;

    /**
     * 当前的时间戳，毫秒
     */
    private long expiresAt;

    /**
     * @param accessKey 应用的accessKey
     * @param secretKey 应用的secretKey
     */
    public TokenHolder(String accessKey, String secretKey, String scope) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.scope = scope;
    }

    /**
     * 获取token，refresh 方法后调用有效
     *
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     * 获取过期时间，refresh 方法后调用有效
     *
     * @return
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * 获取token
     *
     * @return
     * @throws IOException   http请求错误
     * @throws DemoException http接口返回不是 200, access_token未获取
     */
    public void resfresh() throws IOException, DemoException {
        String getTokenURL = url + "?grant_type=client_credentials" + "&client_id=" + ConnUtil.urlEncode(accessKey)
                + "&client_secret=" + ConnUtil.urlEncode(secretKey);

        // 打印的url出来放到浏览器内可以复现
        System.out.println("token url:" + getTokenURL + "\n");

        URL url = new URL(getTokenURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        String result = ConnUtil.getResponseString(conn);
        System.out.println("Token result json:" + result);
        parseJson(result);
    }

    /**
     * @param result token接口获得的result
     * @throws DemoException
     */
    private void parseJson(String result) throws DemoException {
        JSONObject json = new JSONObject(result);
        if (!json.has("access_token")) {
            // 返回没有access_token字段
            throw new DemoException("access_token not obtained, " + result);
        }
        if (!json.has("scope")) {
            // 返回没有scope字段
            throw new DemoException("scope not obtained, " + result);
        }
        // scope = null, 忽略scope检查

        if (scope != null && !json.getString("scope").contains(scope)) {
            throw new DemoException("scope not exist, " + scope + "," + result);
        }
        token = json.getString("access_token");
        expiresAt = System.currentTimeMillis() + json.getLong("expires_in") * 1000;
    }
}
