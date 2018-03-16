package api;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/some-path") 
public interface DataResources {
    
    @POST
    @Path("/") 
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    String store(byte[] data);
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] download(@PathParam("id") String id);
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    byte[] change(@PathParam("id") String id, byte[] data);
    
    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") String id);
    
}