package com.reader.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// 官網日誌
@Entity
@Table(name = "user_api_logs")
public class UserApiLog extends ApiLog {

    protected UserApiLog() {
    }

    UserApiLog(ApiCall call) {
        super(call);
    }
}
