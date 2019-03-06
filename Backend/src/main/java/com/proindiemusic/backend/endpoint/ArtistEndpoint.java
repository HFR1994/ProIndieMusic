package com.proindiemusic.backend.endpoint;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.domain.International;
import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.pojo.CommonTools;
import com.proindiemusic.backend.pojo.Result;
import com.proindiemusic.backend.pojo.templates.EndpointTemplate;
import com.proindiemusic.backend.service.ArtistService;
import com.proindiemusic.backend.service.InternationalService;
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
@Consumes("application/json")
@Path("/v1")
public class ArtistEndpoint extends EndpointTemplate<Artist> {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private InternationalService internationalService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/artist")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getArtists() throws IOException {
        Result artistResult = artistService.getAll();
        return getResponse(artistResult, true);
    }

    @GET
    @Path("/artist/{uuid}")
    @Consumes("application/json")
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

    @GET
    @Path("/artist/user")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getByUser(@Context HttpServletRequest httpRequest) {
        Optional<Artist> artist = artistService.getByUser(CommonTools.transformPrincipal(httpRequest).getUuid());
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
    @Consumes("application/json")
    @Produces("application/json")
    public Response insert(HashMap<String, Object> data, @Context HttpServletRequest httpRequest){
        logger.info(data.toString());
        Result artistResult = artistService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @PUT
    @Path("/artist")
    @Consumes("application/json")
    @Produces("application/json")
    public Response update(HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        Result artistResult = artistService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @DELETE
    @Path("/artist/{uuid}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response delete(@PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result artistResult = artistService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(artistResult, false);
    }

    @GET
    @Path("/artist/{artist}/media")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getMedias() throws IOException {
        Result mediaResult = mediaService.getAll();
        return getResponse(mediaResult, true);
    }

    @GET
    @Path("/artist/{artist}/media/{uuid}")
    @Consumes("application/json")
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
    @Consumes("application/json")
    @Produces("application/json")
    public Response insertMedia(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        data.put("artistUuid", artist);
        logger.info(data.toString());
        Result mediaResult = mediaService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    @PUT
    @Path("/artist/{artist}/media")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateMedia(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        data.put("artistUuid", artist);
        Result mediaResult = mediaService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    @DELETE
    @Path("/artist/{artist}/media/{uuid}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response deleteMedia(@PathParam("artist") String artist, @PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result mediaResult = mediaService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(mediaResult, false);
    }

    @GET
    @Path("/artist/{artist}/international")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getInternationals() throws IOException {
        Result internationalResult = internationalService.getAll();
        return getResponse(internationalResult, true);
    }

    @GET
    @Path("/artist/{artist}/international/{uuid}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getInternationalUserByUuid(@PathParam("artist") String artist, @PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Optional<International> international = internationalService.getByUuid(artist, uuid);
        Response response;
        Result internationalResult = new Result();
        //noinspection Duplicates
        if (international.isPresent()) {
            internationalResult.setCode(Result.OK);
            internationalResult.setMessage("¡Exito!");
            response = Response.ok(internationalResult.getData(international.get())).build();
        } else {
            internationalResult.setCode(Result.BAD_REQUEST);
            internationalResult.setMessage("¡Ups! No existe ese uuid de registro");
            response = Response.serverError().status(internationalResult.getCode(), internationalResult.getMessage()).entity(internationalResult.getData(null)).build();
        }
        return response;
    }

    @POST
    @Path("/artist/{artist}/international")
    @Consumes("application/json")
    @Produces("application/json")
    public Response insertInternational(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        data.put("artistUuid", artist);
        logger.info(data.toString());
        Result internationalResult = internationalService.insert(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(internationalResult, false);
    }

    @PUT
    @Path("/artist/{artist}/international")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateInternational(@PathParam("artist") String artist, HashMap<String, Object> data, @Context HttpServletRequest httpRequest) {
        data.put("artistUuid", artist);
        Result internationalResult = internationalService.update(data, CommonTools.transformPrincipal(httpRequest));
        return getResponse(internationalResult, false);
    }

    @DELETE
    @Path("/artist/{artist}/international/{uuid}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response deleteInternational(@PathParam("artist") String artist, @PathParam("uuid") String uuid, @Context HttpServletRequest httpRequest) {
        Result internationalResult = internationalService.delete(uuid, CommonTools.transformPrincipal(httpRequest));
        return getResponse(internationalResult, false);
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
