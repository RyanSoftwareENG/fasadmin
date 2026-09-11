package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class ApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String token;

    public ApiClient() {

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );
    }

    // =====================================================
    // Token
    // =====================================================

    public void setToken(String token) {
        this.token = token;
    }

    public void clearToken() {
        this.token = null;
    }

    public String getToken() {
        return token;
    }

    // =====================================================
    // POST
    // =====================================================

    public <T> T post(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                createRequestBuilder(url);

        HttpRequest request;

        if (body == null) {

            request =
                    builder.POST(
                            HttpRequest.BodyPublishers.noBody()
                    ).build();

        } else {

            String json =
                    objectMapper.writeValueAsString(
                            body
                    );

            request =
                    builder.POST(
                            HttpRequest.BodyPublishers.ofString(
                                    json
                            )
                    ).build();
        }

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return parseResponse(
                response,
                responseType
        );

    }

    // =====================================================
    // GET
    // =====================================================

    public <T> T get(
            String url,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpRequest request =
                createRequestBuilder(url)
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // GET بدون تحويل JSON
    // =====================================================

    public HttpResponse<String> get(
            String url
    ) throws IOException, InterruptedException {

        HttpRequest request =
                createRequestBuilder(url)
                        .GET()
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // =====================================================
    // PUT
    // =====================================================

    public <T> T put(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        String json =
                objectMapper.writeValueAsString(body);

        HttpRequest request =
                createRequestBuilder(url)
                        .PUT(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    public void delete(
            String url
    ) throws IOException, InterruptedException {

        HttpRequest request =
                createRequestBuilder(url)
                        .DELETE()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    createErrorMessage(response)
            );
        }
    }

    // =====================================================
    // Request Builder
    // =====================================================

    private HttpRequest.Builder createRequestBuilder(
            String url
    ) {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMinutes(2))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Accept",
                                "application/json"
                        );

        if (token != null &&
                !token.isBlank()) {

            builder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }

        return builder;
    }
    // =====================================================
    // Response
    // =====================================================

    private <T> T parseResponse(
            HttpResponse<String> response,
            Class<T> responseType
    ) throws IOException {

        int status =
                response.statusCode();

        String body =
                response.body();

        if (status < 200 || status >= 300) {

            throw new IOException(
                    createErrorMessage(response)
            );
        }

        if (responseType == Void.class) {
            return null;
        }

        if (body == null || body.isBlank()) {

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        return objectMapper.readValue(
                body,
                responseType
        );
    }

    private String createErrorMessage(
            HttpResponse<String> response
    ) {

        return "فشل طلب API. HTTP "
                + response.statusCode()
                + " - "
                + response.body();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
    public HttpResponse<String> postRaw(
            String url,
            Object body
    ) throws IOException, InterruptedException {

        String json =
                objectMapper.writeValueAsString(body);

        HttpRequest request =
                createRequestBuilder(url)
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
    public <T> T postAuthResponse(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                createRequestBuilder(url);

        String json =
                objectMapper.writeValueAsString(body);

        HttpRequest request =
                builder.POST(
                                HttpRequest.BodyPublishers.ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        String responseBody =
                response.body();

        if (responseBody == null ||
                responseBody.isBlank()) {

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new IOException(
                        createErrorMessage(response)
                );
            }

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        // تسجيل الدخول قد يعيد 401
        // ونحتاج قراءة JSON الموجود في الاستجابة
        if (response.statusCode() == 200 ||
                response.statusCode() == 401) {

            return objectMapper.readValue(
                    responseBody,
                    responseType
            );
        }

        return parseResponse(
                response,
                responseType
        );
    }
    // =====================================================
// PATCH
// =====================================================

    public <T> T patch(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        String json;

        if (body == null) {
            json = "";
        } else {
            json = objectMapper.writeValueAsString(body);
        }

        HttpRequest request =
                createRequestBuilder(url)
                        .method(
                                "PATCH",
                                json.isBlank()
                                        ? HttpRequest.BodyPublishers.noBody()
                                        : HttpRequest.BodyPublishers.ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return parseResponse(
                response,
                responseType
        );
    }
    // =====================================================
// GET List
// =====================================================

    public <T> List<T> getList(
            String url,
            TypeReference<List<T>> typeReference
    ) throws IOException, InterruptedException {

        HttpRequest request =
                createRequestBuilder(url)
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        int status =
                response.statusCode();

        if (status < 200 || status >= 300) {

            throw new IOException(
                    createErrorMessage(response)
            );
        }

        String body =
                response.body();

        if (body == null || body.isBlank()) {

            throw new IOException(
                    "السيرفر أعاد قائمة فارغة."
            );
        }

        return objectMapper.readValue(
                body,
                typeReference
        );
    }
}