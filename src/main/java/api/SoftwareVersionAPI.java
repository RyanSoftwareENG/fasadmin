package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SoftwareVersionRequest;
import dto.SoftwareVersionResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SoftwareVersionAPI {

    private final ApiClient apiClient;

    public SoftwareVersionAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public SoftwareVersionResponse create(
            SoftwareVersionRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.SOFTWARE_VERSIONS,
                request,
                SoftwareVersionResponse.class
        );
    }

    public SoftwareVersionResponse update(
            Long versionId,
            SoftwareVersionRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.SOFTWARE_VERSIONS
                        + "/"
                        + versionId,
                request,
                SoftwareVersionResponse.class
        );
    }

    public SoftwareVersionResponse getById(
            Long versionId
    ) throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.SOFTWARE_VERSIONS
                        + "/"
                        + versionId,
                SoftwareVersionResponse.class
        );
    }

    public List<SoftwareVersionResponse> getAll()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SOFTWARE_VERSIONS
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SoftwareVersionResponse>>() {}
        );
    }

    public List<SoftwareVersionResponse> getByApplication(
            String applicationName
    ) throws IOException, InterruptedException {

        String encoded =
                URLEncoder.encode(
                        applicationName,
                        StandardCharsets.UTF_8
                );

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SOFTWARE_VERSIONS
                                + "/application/"
                                + encoded
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SoftwareVersionResponse>>() {}
        );
    }

    public SoftwareVersionResponse getLatest(
            String applicationName
    ) throws IOException, InterruptedException {

        String encoded =
                URLEncoder.encode(
                        applicationName,
                        StandardCharsets.UTF_8
                );

        return apiClient.get(
                ApiEndpoints.SOFTWARE_VERSIONS
                        + "/latest/"
                        + encoded,
                SoftwareVersionResponse.class
        );
    }

    public SoftwareVersionResponse getLatestActive(
            String applicationName
    ) throws IOException, InterruptedException {

        String encoded =
                URLEncoder.encode(
                        applicationName,
                        StandardCharsets.UTF_8
                );

        return apiClient.get(
                ApiEndpoints.SOFTWARE_VERSIONS
                        + "/latest-active/"
                        + encoded,
                SoftwareVersionResponse.class
        );
    }

    public void delete(
            Long versionId
    ) throws IOException, InterruptedException {

        apiClient.delete(
                ApiEndpoints.SOFTWARE_VERSIONS
                        + "/"
                        + versionId
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب الإصدارات. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}