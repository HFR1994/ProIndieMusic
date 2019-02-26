package com.proindiemusic.backend.endpoint;

import com.proindiemusic.backend.domain.Prueba;
import com.proindiemusic.backend.pojo.CommonTools;
import com.proindiemusic.backend.pojo.Result;
import com.proindiemusic.backend.pojo.templates.EndpointTemplate;
import com.proindiemusic.backend.service.PruebaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Optional;

@Component
@Path("/v1")
public class PruebaEndpoint extends EndpointTemplate<Prueba> {

    @Autowired
    private PruebaService pruebaService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/prueba")
    @Produces("application/json")
    public Response getPruebas() throws IOException {
        Result pruebaResult = pruebaService.getAll();
        return getResponse(pruebaResult, true);
    }

    @GET
    @Path("/prueba/{uuid}")
    @Produces("application/json")
    public Response getUserByUuid(@PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Optional<Prueba> prueba = pruebaService.getByUuid(uuid);
        Response response;
        Result pruebaResult = new Result();
        //noinspection Duplicates
        if (prueba.isPresent()) {
            pruebaResult.setCode(Result.OK);
            pruebaResult.setMessage("¡Exito!");
            response = Response.ok(pruebaResult.getData(prueba.get())).build();
        } else {
            pruebaResult.setCode(Result.BAD_REQUEST);
            pruebaResult.setMessage("¡Ups! No existe ese uuid de registro");
            response = Response.serverError().status(pruebaResult.getCode(), pruebaResult.getMessage()).entity(pruebaResult.getData(null)).build();
        }
        return response;
    }

    @POST
    @Path("/prueba")
    @Produces("application/json")
    public Response insert(HashMap<String, Object> data, @Context HttpServletRequest httpRequest) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        logger.info(data.toString());
        Result pruebaResult = pruebaService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(pruebaResult, false);
    }

    @PUT
    @Path("/prueba")
    @Produces("application/json")
    public Response update(HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        Result pruebaResult = pruebaService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(pruebaResult, false);
    }

    @DELETE
    @Path("/prueba/{uuid}")
    @Produces("application/json")
    public Response delete(@PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result pruebaResult = pruebaService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(pruebaResult, false);
    }

    private Response getResponse(Result pruebaResult, boolean type) {
        Response response;
        if (pruebaResult.getCode().equals(Result.CREATED) || pruebaResult.getCode().equals(Result.OK)) {
            response = Response.ok(pruebaResult.getData(type)).build();
        } else {
            response = Response.serverError().status(pruebaResult.getCode(), pruebaResult.getMessage()).entity(pruebaResult.getData(type)).build();
        }
        return response;
    }

}
