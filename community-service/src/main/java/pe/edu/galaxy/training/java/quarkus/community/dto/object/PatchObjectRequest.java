package pe.edu.galaxy.training.java.quarkus.community.dto.object;

/**
 * Es el endpoint que usa loan-service para cambiar el estado del objeto ({"status": "RESERVED"}).
 * `status` se valida contra el enum ObjectStatus en el servicio.
 */
public record PatchObjectRequest(String status) {}
