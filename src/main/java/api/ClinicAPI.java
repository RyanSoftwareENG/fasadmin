package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.ClinicCreateRequest;
import dto.ClinicResponse;
import dto.ClinicUpdateRequest;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class ClinicAPI {

    private final ApiClient apiClient;

    public ClinicAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // =====================================================
    // جميع العيادات
    // =====================================================

    public List<ClinicResponse> getAllClinics()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.CLINICS
                );

        checkResponse(response);

        return apiClient
                .getObjectMapper()
                .readValue(
                        response.body(),
                        new TypeReference<List<ClinicResponse>>() {}
                );
    }

    // =====================================================
    // عيادة واحدة
    // =====================================================

    public ClinicResponse getClinic(
            Long clinicId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.CLINICS
                                + "/"
                                + clinicId
                );

        checkResponse(response);

        return apiClient
                .getObjectMapper()
                .readValue(
                        response.body(),
                        ClinicResponse.class
                );
    }

    // =====================================================
    // إنشاء عيادة
    // =====================================================

    public ClinicResponse createClinic(
            ClinicCreateRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.CLINICS,
                request,
                ClinicResponse.class
        );
    }

    // =====================================================
    // تعديل عيادة
    // =====================================================

    public ClinicResponse updateClinic(
            Long clinicId,
            ClinicUpdateRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.CLINICS
                        + "/"
                        + clinicId,
                request,
                ClinicResponse.class
        );
    }

    // =====================================================
    // تعليق العيادة
    // =====================================================

    public ClinicResponse suspendClinic(
            Long clinicId
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.CLINICS
                        + "/"
                        + clinicId
                        + "/suspend",
                null,
                ClinicResponse.class
        );
    }

    // =====================================================
    // إعادة تفعيل العيادة
    // =====================================================

    public ClinicResponse activateClinic(
            Long clinicId
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.CLINICS
                        + "/"
                        + clinicId
                        + "/activate",
                null,
                ClinicResponse.class
        );
    }

    // =====================================================
    // التحقق من HTTP Response
    // =====================================================

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب العيادات. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}