package cl.duoc.bancoxyz.exceptions;

public class ServicioBancoNoDisponibleException extends RuntimeException {

    public ServicioBancoNoDisponibleException(String message) {
        super(message);
    }
}