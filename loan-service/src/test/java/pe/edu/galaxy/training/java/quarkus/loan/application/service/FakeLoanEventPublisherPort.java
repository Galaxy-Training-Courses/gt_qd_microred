package pe.edu.galaxy.training.java.quarkus.loan.application.service;

import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanEventPublisherPort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;

import java.util.ArrayList;
import java.util.List;

class FakeLoanEventPublisherPort implements LoanEventPublisherPort {

    record Published(String eventType, LoanRequest loanRequest) {}

    final List<Published> published = new ArrayList<>();

    @Override
    public void publish(String eventType, LoanRequest loanRequest) {
        published.add(new Published(eventType, loanRequest));
    }
}
