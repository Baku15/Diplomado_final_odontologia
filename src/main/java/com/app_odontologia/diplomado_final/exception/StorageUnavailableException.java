package com.app_odontologia.diplomado_final.exception;

/**
 * Excepción lanzada cuando el servicio de almacenamiento (MinIO) no está disponible.
 */
public class StorageUnavailableException extends RuntimeException {
    public StorageUnavailableException(String message) {
        super(message);
    }
    public StorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
