package com.proindiemusic.backend.endpoint;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.pojo.CommonTools;
import com.proindiemusic.backend.pojo.Result;
import com.proindiemusic.backend.pojo.templates.EndpointTemplate;
import com.proindiemusic.backend.service.ArtistService;
import com.proindiemusic.backend.service.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("Duplicates")
@Component
@Path("/v1")
public class ArtistEndpoint extends EndpointTemplate<Artist> {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MediaService mediaService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/artist")
    @Produces("application/json")
    public Response getArtists() throws IOException {
        Result artistResult = artistService.getAll();
        return getResponse(artistResult, true);
    }

    @GET
    @Path("/artist/{uuid}")
    @Produces("application/json")
    public Response getUserByUuid(@PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Optional<Artist> artist = artistService.getByUuid(uuid);
        Response response;
        Result artistResult = new Result();
        //noinspection Duplicates
        if (artist.isPresent()) {
            artistResult.setCode(Result.OK);
            artistResult.setMessage("¡Exito!");
            response = Response.ok(artistResult.getData(artist.get())).build();
        } else {
            artistResult.setCode(Result.BAD_REQUEST);
            artistResult.setMessage("¡Ups! No existe ese uuid de registro");
            response = Response.serverError().status(artistResult.getCode(), artistResult.getMessage()).entity(artistResult.getData(null)).build();
        }
        return response;
    }

    @POST
    @Path("/artist")
    @Produces("application/json")
    public Response insert(HashMap<String, Object> data, @Context HttpServletRequest httpRequest) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        logger.info(data.toString());
        Result artistResult = artistService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @PUT
    @Path("/artist")
    @Produces("application/json")
    public Response update(HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        Result artistResult = artistService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @DELETE
    @Path("/artist/{uuid}")
    @Produces("application/json")
    public Response delete(@PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result artistResult = artistService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @GET
    @Path("/artist/{artist}/media")
    @Produces("application/json")
    public Response getMedias() throws IOException {
        Result mediaResult = mediaService.getAll();
        return getResponse(mediaResult, true);
    }

    @GET
    @Path("/artist/{artist}/media/{uuid}")
    @Produces("application/json")
    public Response getMediaUserByUuid(@PathParam("artist") String artist, @PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Optional<Media> media = mediaService.getByUuid(artist, uuid);
        Response response;
        Result mediaResult = new Result();
        //noinspection Duplicates
        if (media.isPresent()) {
            mediaResult.setCode(Result.OK);
            mediaResult.setMessage("¡Exito!");
            response = Response.ok(mediaResult.getData(media.get())).build();
        } else {
            mediaResult.setCode(Result.BAD_REQUEST);
            mediaResult.setMessage("¡Ups! No existe ese uuid de registro");
            response = Response.serverError().status(mediaResult.getCode(), mediaResult.getMessage()).entity(mediaResult.getData(null)).build();
        }
        return response;
    }

    @POST
    @Path("/artist/{artist}/media")
    @Produces("application/json")
    public Response insertMedia(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        data.put("artistUuid", artist);
        logger.info(data.toString());
        Result mediaResult = mediaService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    @PUT
    @Path("/artist/{artist}/media")
    @Produces("application/json")
    public Response updateMedia(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        data.put("artistUuid", artist);
        Result mediaResult = mediaService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    @DELETE
    @Path("/artist/{artist}/media/{uuid}")
    @Produces("application/json")
    public Response deleteMedia(@PathParam("artist") String artist, @PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result mediaResult = mediaService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    private Response getResponse(Result artistResult, boolean type) {
        Response response;
        if (artistResult.getCode().equals(Result.CREATED) || artistResult.getCode().equals(Result.OK)) {
            response = Response.ok(artistResult.getData(type)).build();
        } else {
            response = Response.serverError().status(artistResult.getCode(), artistResult.getMessage()).entity(artistResult.getData(type)).build();
        }
        return response;
    }
}
