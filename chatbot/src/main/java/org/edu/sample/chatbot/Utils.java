package org.edu.sample.chatbot;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class Utils {

    public static String parametersToWWWFormURLEncoded(Map<String, String> parameters) throws Exception {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (s.length() > 0) {
                s.append("&");
            }
            s.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
            s.append("=");
            s.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
        }
        return s.toString();
    }

    public static String toMD5(String input) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(input.getBytes("UTF-8"));
        BigInteger hash = new BigInteger(1, md5.digest());
        return String.format("%1$032X", hash);
    }

    public static String toAcceptLanguageTags(Locale... locales) {
        // https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4
        //
        // for example, if user ask for: Locale.CANADA_FRENCH, Locale.ENGLISH
        // then this method will render: fr-CA;q=1.0, fr;q=0.99, en;q=0.5
        //
        if (locales.length == 0)
            return "";
        float qf = 1f / (float) locales.length;
        float q = 1f;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            if (s.length() > 0)
                s.append(", ");
            if (!locale.getCountry().equals("")) {
                s.append(locale.getLanguage()).append("-").append(locale.getCountry());
                s.append(";q=" + q);
                s.append(", ");
                s.append(locale.getLanguage());
                s.append(";q=" + (q - 0.01));
            } else {
                s.append(locale.getLanguage());
                s.append(";q=" + q);
            }
            q -= qf;
        }
        return s.toString();
    }

    public static String request(String url, Map<String, String> headers, Map<String, String> cookies, Map<String, String> parameters) throws Exception {
        HttpURLConnection connection;
        if (!StringUtils.isBlank(System.getProperty("http.proxyhost"))) {
            String proxyhost = System.getProperty("http.proxyhost");
            int proxyport = Integer.parseInt(System.getProperty("http.proxyport", "8080"));
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyhost, proxyport));
            connection = (HttpURLConnection) new URL(url).openConnection(proxy);
        } else {
            connection = (HttpURLConnection) new URL(url).openConnection();
        }
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36");

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (String cookie : cookies.values()) {
                if (cookieHeader.length() > 0) {
                    cookieHeader.append(";");
                }
                cookieHeader.append(cookie);
            }
            connection.setRequestProperty("Cookie", cookieHeader.toString());
        }
        connection.setDoInput(true);
        if (parameters != null && !parameters.isEmpty()) {
            connection.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(parametersToWWWFormURLEncoded(parameters));
            osw.flush();
            osw.close();
        }
        if (cookies != null) {
            for (Map.Entry<String, List<String>> headerEntry : connection.getHeaderFields().entrySet()) {
                if (headerEntry != null && headerEntry.getKey() != null && headerEntry.getKey().equalsIgnoreCase("Set-Cookie")) {
                    for (String header : headerEntry.getValue()) {
                        for (HttpCookie httpCookie : HttpCookie.parse(header)) {
                            cookies.put(httpCookie.getName(), httpCookie.toString());
                        }
                    }
                }
            }
        }
        Reader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringWriter w = new StringWriter();
        char[] buffer = new char[1024];
        int n = 0;
        while ((n = r.read(buffer)) != -1) {
            w.write(buffer, 0, n);
        }
        r.close();
        return w.toString();
    }


    public static String stringAtIndex(String[] strings, int index) {
        if (index >= strings.length) return "";
        return strings[index];
    }
}