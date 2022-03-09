package dev.soffa.foundation.commons.http;

import java.util.Map;

public interface HttpClient {

    HttpResponse request(HttpRequest request);

     default HttpResponse get(String url, Map<String, String> headers) {
         return request(new HttpRequest("GET", url, null, headers));
     }

     default HttpResponse get(String url) {
         return get(url, null);
     }

     default HttpResponse post(String url, Object data, Map<String, String> headers) {
         return request(new HttpRequest("POST", url, data, headers));
     }

     default HttpResponse post(String url, Object data) {
         return post(url, data, null);
     }



}
