package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionRequestResponse;
import dto.SubscriptionRequestReviewRequest;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class SubscriptionRequestAPI {

    private final ApiClient apiClient;

    public SubscriptionRequestAPI(
            ApiClient apiClient
    ) {

        if (apiClient == null) {

            throw new IllegalArgumentException(
                    "ApiClient مطلوب."
            );
        }

        this.apiClient =
                apiClient;
    }

    // =====================================================
    // جلب الطلبات المعلقة
    // GET /api/admin/subscription-requests/pending
    // =====================================================

    public List<SubscriptionRequestResponse>
    getPendingRequests()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints
                                .SUBSCRIPTION_REQUESTS_PENDING
                );

        checkResponse(
                response
        );

        String body =
                response.body();

        if (body == null ||
                body.isBlank()) {

            return List.of();
        }

        return apiClient
                .getObjectMapper()
                .readValue(
                        body,
                        new TypeReference<
                                List<SubscriptionRequestResponse>
                                >() {}
                );
    }

    // =====================================================
    // الموافقة
    // POST /api/admin/subscription-requests/{id}/approve
    // =====================================================

    public SubscriptionRequestResponse approve(
            Long requestId,
            String adminNotes
    ) throws IOException, InterruptedException {

        validateRequestId(
                requestId
        );

        SubscriptionRequestReviewRequest request =
                new SubscriptionRequestReviewRequest();

        request.setAdminNotes(
                clean(adminNotes)
        );

        return apiClient.post(
                ApiEndpoints
                        .subscriptionRequestApprove(
                                requestId
                        ),
                request,
                SubscriptionRequestResponse.class
        );
    }

    // =====================================================
    // الرفض
    // POST /api/admin/subscription-requests/{id}/reject
    // =====================================================

    public SubscriptionRequestResponse reject(
            Long requestId,
            String adminNotes
    ) throws IOException, InterruptedException {

        validateRequestId(
                requestId
        );

        SubscriptionRequestReviewRequest request =
                new SubscriptionRequestReviewRequest();

        request.setAdminNotes(
                clean(adminNotes)
        );

        return apiClient.post(
                ApiEndpoints
                        .subscriptionRequestReject(
                                requestId
                        ),
                request,
                SubscriptionRequestResponse.class
        );
    }

    // =====================================================
    // التحقق من الاستجابة
    // =====================================================

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response == null) {

            throw new IOException(
                    "لم تصل استجابة من السيرفر."
            );
        }

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    createErrorMessage(
                            response
                    )
            );
        }
    }

    // =====================================================
    // إنشاء رسالة الخطأ
    // =====================================================

    private String createErrorMessage(
            HttpResponse<String> response
    ) {

        String body =
                response.body();

        if (body == null ||
                body.isBlank()) {

            return "فشل طلب الاشتراك. HTTP "
                    + response.statusCode();
        }

        return "فشل طلب الاشتراك. HTTP "
                + response.statusCode()
                + " - "
                + body;
    }

    // =====================================================
    // التحقق من Request ID
    // =====================================================

    private void validateRequestId(
            Long requestId
    ) {

        if (requestId == null ||
                requestId <= 0) {

            throw new IllegalArgumentException(
                    "معرف طلب الاشتراك غير صالح."
            );
        }
    }

    // =====================================================
    // تنظيف النص
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }
}