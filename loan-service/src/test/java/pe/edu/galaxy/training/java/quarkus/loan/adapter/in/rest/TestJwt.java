package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest;

import io.smallrye.jwt.build.Jwt;

public final class TestJwt {

    private TestJwt() {
    }

    public static String validToken() {
        return Jwt.issuer("https://microred-lab.local/issuer")
                .subject("test-user")
                .groups("user")
                .sign();
    }
}
