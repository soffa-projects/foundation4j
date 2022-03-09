package dev.soffa.foundation.commons.http;

import java.net.URL;

public interface HttpResponseProvider {

    HttpResponse apply(URL url, HttpHeaders headers);
}
