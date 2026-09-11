package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.DeviceResponse;

import java.io.IOException;
import java.util.List;

public class DeviceAPI {

    private final ApiClient apiClient;

    public DeviceAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // =====================================================
    // جلب جميع الأجهزة
    // GET /api/admin/devices
    // =====================================================

    public List<DeviceResponse> getAllDevices()
            throws IOException, InterruptedException {

        return apiClient.getList(
                ApiEndpoints.DEVICES,
                new TypeReference<List<DeviceResponse>>() {}
        );
    }

    // =====================================================
    // جلب جهاز واحد
    // GET /api/admin/devices/{deviceId}
    // =====================================================

    public DeviceResponse getDevice(
            Long deviceId
    ) throws IOException, InterruptedException {

        validateDeviceId(deviceId);

        return apiClient.get(
                ApiEndpoints.device(deviceId),
                DeviceResponse.class
        );
    }

    // =====================================================
    // تفعيل الجهاز
    // =====================================================

    public DeviceResponse activateDevice(
            Long deviceId
    ) throws IOException, InterruptedException {

        return changeStatus(
                deviceId,
                "ACTIVE"
        );
    }

    // =====================================================
    // حظر الجهاز
    // =====================================================

    public DeviceResponse blockDevice(
            Long deviceId
    ) throws IOException, InterruptedException {

        return changeStatus(
                deviceId,
                "BLOCKED"
        );
    }

    // =====================================================
    // إيقاف الجهاز
    // =====================================================

    public DeviceResponse suspendDevice(
            Long deviceId
    ) throws IOException, InterruptedException {

        return changeStatus(
                deviceId,
                "SUSPENDED"
        );
    }

    // =====================================================
    // تغيير حالة الجهاز
    // PATCH /api/admin/devices/{id}/status
    // =====================================================

    public DeviceResponse changeStatus(
            Long deviceId,
            String status
    ) throws IOException, InterruptedException {

        validateDeviceId(deviceId);

        if (status == null || status.isBlank()) {

            throw new IllegalArgumentException(
                    "حالة الجهاز غير صالحة."
            );
        }

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!normalizedStatus.equals("ACTIVE")
                && !normalizedStatus.equals("BLOCKED")
                && !normalizedStatus.equals("REVOKED")) {

            throw new IllegalArgumentException(
                    "حالة الجهاز غير صالحة."
            );
        }

        return apiClient.patch(
                ApiEndpoints.deviceStatus(
                        deviceId,
                        normalizedStatus
                ),
                null,
                DeviceResponse.class
        );
    }

    // =====================================================
    // حذف الجهاز
    // DELETE /api/admin/devices/{deviceId}
    // =====================================================

    public void deleteDevice(
            Long deviceId
    ) throws IOException, InterruptedException {

        validateDeviceId(deviceId);

        apiClient.delete(
                ApiEndpoints.device(deviceId)
        );
    }

    // =====================================================
    // Validation
    // =====================================================

    private void validateDeviceId(
            Long deviceId
    ) {

        if (deviceId == null || deviceId <= 0) {

            throw new IllegalArgumentException(
                    "معرف الجهاز غير صالح."
            );
        }
    }
}