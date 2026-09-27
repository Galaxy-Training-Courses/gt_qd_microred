package pe.edu.galaxy.training.java.quarkus.loan.application.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ConflictException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.RequesterIsOwnerException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;

/**
 * Interceptor mínimo para {@link BusinessOperation}: registra inicio, fin
 * y duración de la operación. Los campos dinámicos del log estructurado
 * viajan por MDC; `requestId` ya está en el MDC desde {@code RequestIdFilter}
 * cuando este interceptor corre, así que sus dos líneas también quedan
 * correlacionadas sin ningún trabajo extra.
 */
@Interceptor
@BusinessOperation("")
@Priority(Interceptor.Priority.APPLICATION)
public class BusinessOperationInterceptor {

    private static final String OPERATION_KEY = "operation";
    private static final String LOAN_ID_KEY = "loanId";
    private static final String STATUS_KEY = "status";

    private static final Logger LOG = Logger.getLogger(BusinessOperationInterceptor.class);

    @AroundInvoke
    public Object logOperation(InvocationContext context) throws Exception {
        String operation = context.getMethod().getAnnotation(BusinessOperation.class).value();
        MDC.put(OPERATION_KEY, operation);
        putLoanIdIfPresent(context.getParameters());

        LOG.info("Operacion de negocio iniciada");
        long start = System.nanoTime();
        try {
            Object result = context.proceed();
            if (result instanceof LoanRequest loanRequest) {
                MDC.put(LOAN_ID_KEY, loanRequest.id());
                MDC.put(STATUS_KEY, loanRequest.status().name());
            }
            LOG.infof("Operacion de negocio finalizada en %d ms", elapsedMs(start));
            return result;
        } catch (RuntimeException e) {
            if (isBusinessRuleViolation(e)) {
                LOG.warnf("Operacion de negocio rechazada tras %d ms: %s", elapsedMs(start), e.getMessage());
            } else {
                LOG.errorf("Operacion de negocio fallo tras %d ms: %s", elapsedMs(start), e.getMessage());
            }
            throw e;
        } finally {
            MDC.remove(OPERATION_KEY);
            MDC.remove(LOAN_ID_KEY);
            MDC.remove(STATUS_KEY);
        }
    }

    private void putLoanIdIfPresent(Object[] parameters) {
        if (parameters.length > 0 && parameters[0] instanceof Long loanId) {
            MDC.put(LOAN_ID_KEY, loanId);
        }
    }

    private boolean isBusinessRuleViolation(RuntimeException e) {
        return e instanceof NotFoundException
                || e instanceof ConflictException
                || e instanceof RequesterIsOwnerException
                || e instanceof InvalidFieldValueException;
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
