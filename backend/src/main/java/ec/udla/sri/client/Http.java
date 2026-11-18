package ec.udla.sri.client;

import ec.udla.sri.config.AppProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class Http {

    static final String USER_AGENT = "Mozilla/5.0 (compatible; consulta-sri/1.0)";

    private Http() {}

    static RestClient crear(String baseUrl, AppProperties props) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(props.upstream().connectTimeout());
        f.setReadTimeout(props.upstream().readTimeout());
        return RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
    }
}
