package com.auth.server.connection;

public enum Status
{
    IDLE,
    CONNECTED,
    REGISTERED,
    LOGIN,
    LOGGED,
    DISCONNECTED,
    BYE,
    ERROR,
    MALFORMED
}
