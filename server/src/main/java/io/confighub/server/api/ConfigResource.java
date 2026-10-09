package io.confighub.server.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.confighub.server.application.ConfigurationService;
import io.confighub.server.domain.ResolvedConfiguration;

@Path("/api/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {
    private final ConfigurationService service;

    public ConfigResource(ConfigurationService service) {
        this.service = service;
    }

    @GET
    @Path("/{application}/{environment}")
    public ResolvedConfiguration resolve(
            @PathParam("application") String application,
            @PathParam("environment") String environment) {
        try {
            return service.resolve(application, environment);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse(e.getMessage()))
                            .build());
        }
    }

    public record ErrorResponse(String message) {
    }
}
