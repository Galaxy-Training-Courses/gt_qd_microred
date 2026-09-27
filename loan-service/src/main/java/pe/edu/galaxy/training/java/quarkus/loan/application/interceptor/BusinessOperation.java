package pe.edu.galaxy.training.java.quarkus.loan.application.interceptor;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una operación de negocio relevante. Se usa "de forma limitada":
 * Solo las 5 operaciones de {@code LoanApplicationService} — no se construye
 * un framework AOP alrededor de esto,
 * {@link BusinessOperationInterceptor} es el único consumidor.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface BusinessOperation {

    /**
     * Nombre de la operación (p. ej. "loan-approval"). {@code @Nonbinding}:
     * el binding del interceptor solo mira que la anotación esté presente,
     * no su valor — el interceptor lee el valor real en tiempo de ejecución
     * desde el método interceptado.
     */
    @Nonbinding
    String value();
}
