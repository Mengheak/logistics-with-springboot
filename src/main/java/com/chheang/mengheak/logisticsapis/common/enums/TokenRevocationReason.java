package com.chheang.mengheak.logisticsapis.common.enums;

/** Why a refresh token stopped being usable - kept for auditing and incident response. */
public enum TokenRevocationReason {
    /** Consumed normally: the client exchanged it for a new pair. */
    ROTATED,
    LOGOUT,
    LOGOUT_ALL,
    /** A token was presented twice, which means a copy leaked. */
    REUSE_DETECTED,
    ACCOUNT_DISABLED,
    /** The password changed, so sessions opened with the old one must not survive. */
    CREDENTIALS_CHANGED
}
