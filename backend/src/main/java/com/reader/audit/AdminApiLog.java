package com.reader.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// 後臺日誌
@Entity
@Table(name = "admin_api_logs")
public class AdminApiLog extends ApiLog {

    protected AdminApiLog() {
    }

    AdminApiLog(ApiCall call) {
        super(call);
    }
}
