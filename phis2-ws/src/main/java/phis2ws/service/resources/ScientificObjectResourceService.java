//**********************************************************************************************
//                               ScientificObjectResourceService.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: august 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  August, 30 2017 - update post - génération des uris
// Subject: Represents the scientific object data service
//***********************************************************************************************
package phis2ws.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.GlobalWebserviceValues;
import phis2ws.service.dao.sesame.ScientificObjectDAOSesame;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.resources.dto.scientificObject.ScientificObjectDTO;
import phis2ws.service.resources.dto.scientificObject.ScientificObjectPostDTO;
import phis2ws.service.resources.dto.scientificObject.ScientificObjectPutDTO;
import phis2ws.service.resources.validation.interfaces.Required;
import phis2ws.service.resources.validation.interfaces.URL;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.AbstractResultForm;
import phis2ws.service.view.brapi.form.ResponseFormPOST;
import phis2ws.service.view.manager.ResultForm;
import phis2ws.service.view.model.phis.ScientificObject;

/**
 * scientific objects service
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
@Api("/scientificObjects")
@Path("scientificObjects")
public class ScientificObjectResourceService extends ResourceService {
    final static Logger LOGGER = LoggerFactory.getLogger(ScientificObjectResourceService.class);
    
    /**
     * Transform ScientificObjectPostDTO to ScientificObject
     * @param scientificObjectDTOs
     * @return the transformed list
     */
    private List<ScientificObject> scientificObjectPostsDTOsToScientificObjects(List<ScientificObjectPostDTO> scientificObjectDTOs) {
        ArrayList<ScientificObject> scientificObjects = new ArrayList<>();
        
        for (ScientificObjectPostDTO scientificObjectDTO : scientificObjectDTOs) {
            scientificObjects.add(scientificObjectDTO.createObjectFromDTO());
        }
        
        return scientificObjects;
    }
    
    /**
     * Transform ScientificObjectPutDTO to ScientificObject
     * @param scientificObjectPutDTOs
     * @return the transformed list
     */
    private List<ScientificObject> scientificObjectPutDTOsToScientificObjects(List<ScientificObjectPutDTO> scientificObjectPutDTOs) {
        ArrayList<ScientificObject> scientificObjects = new ArrayList<>();
        
        for (ScientificObjectPutDTO scientificObjectDTO : scientificObjectPutDTOs) {
            scientificObjects.add(scientificObjectDTO.createObjectFromDTO());
        }
        
        return scientificObjects;
    }
    
    /**
     * Enter a set of scientific objects into the triplestore and associate them to an experiment if it is given
     * @param scientificObjectsDTO scientific objects to save
     * @param context query context element to get the ip address information of the user
     * @example 
     * [
        {
          "rdfType": "http://www.opensilex.org/vocabulary/oeso#Plant",
          "experiment": "http://www.phenome-fppn.fr/diaphen/DIA2017-2",
          "year": "2018",
          "properties": [
           {
             "relation": "http://www.w3.org/2000/01/rdf-schema#label",
             "value": "plt030"
           }
          ]
        }
     * 
     * @return 
     */
    @POST
    @ApiOperation(value = "Post scientific object(s)",
                  notes = "Register new scientific object(s) in the database.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Scientific object(s) saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
       @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                         dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                         value = DocumentationAnnotation.ACCES_TOKEN,
                         example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postScientificObject(
            @ApiParam(value = DocumentationAnnotation.SCIENTIFIC_OBJECT_POST_DATA_DEFINITION, required = true) @Valid ArrayList<ScientificObjectPostDTO> scientificObjectsDTO,
            @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        //if there is at least one scientific object
        if (!scientificObjectsDTO.isEmpty()) {
            try {
                ScientificObjectDAOSesame scientificObjectDaoSesame = new ScientificObjectDAOSesame();
                if (context.getRemoteAddr() != null) {
                    scientificObjectDaoSesame.remoteUserAdress = context.getRemoteAddr();
                }
                
                scientificObjectDaoSesame.user = userSession.getUser();
                
                //Check the scientific objects and insert them in triplestore
                POSTResultsReturn resultSesame = scientificObjectDaoSesame.checkAndInsert(scientificObjectPostsDTOsToScientificObjects(scientificObjectsDTO));
                if (resultSesame.getHttpStatus().equals(Response.Status.CREATED)) {
                    //scientific objects inserted (201)
                    postResponse = new ResponseFormPOST(resultSesame.statusList);
                    postResponse.getMetadata().setDatafiles(resultSesame.getCreatedResources());
                    return Response.status(resultSesame.getHttpStatus()).entity(postResponse).build();
                } else if (resultSesame.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                        || resultSesame.getHttpStatus().equals(Response.Status.OK)
                        || resultSesame.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                    postResponse = new ResponseFormPOST(resultSesame.statusList);
                }
                return Response.status(resultSesame.getHttpStatus()).entity(postResponse).build();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
            }
        } else {
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, "Empty scientific objects list"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
    
    /**
     * Update the data of a given scientific object in the given context (experiment).
     * @param uri
     * @param experiment
     * @param scientificObjectDTO
     * @param context
     * @example
     * {
     *      "rdfType": "http://www.opensilex.org/vocabulary/oeso#Plot",
     *      "geometry": "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))",
     *      "isPartOf": "http://www.opensilex.org/opensilex/2019/o19000102",
     *      "label": "POZ_1",
     *      "properties": [
     *          {
     *              "relation": "http://www.w3.org/2000/01/rdf-schema#label",
     *              "value": "LA23"
     *          }
     *      ]
     * }
     * @return the put result
     * @example
     * {
     *      "metadata": {
     *          "pagination": null,
     *          "status": [
     *              {
     *                  "message": "Resource(s) updated",
     *                  "exception": {
     *                      "type": "Info",
     *                      "href": null,
     *                      "details": "1 updated resource(s)."
     *                  }
     *              }
     *          ],
     *          "datafiles": [
     *              "http://www.opensilex.org/opensilex/2019/o19000106"
     *          ]
     *      }
     * }
     * @throws Exception 
     */
    @PUT
    @Path("{uri}/{experiment}")
    @ApiOperation(value = "Put scientific object(s) in the given experiment",
                  notes = "Update scientific object(s) in the database.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Scientific object(s) updated", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
       @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                         dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                         value = DocumentationAnnotation.ACCES_TOKEN,
                         example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(
            @ApiParam(value = DocumentationAnnotation.SCIENTIFIC_OBJECT_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_SCIENTIFIC_OBJECT_URI) @PathParam("uri") @Required @URL String uri,
            @ApiParam(value = DocumentationAnnotation.EXPERIMENT_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_URI) @PathParam("experiment") @Required @URL String experiment,
            @ApiParam(value = DocumentationAnnotation.SCIENTIFIC_OBJECT_POST_DATA_DEFINITION, required = true) @Valid ScientificObjectPutDTO scientificObjectDTO,
            @Context HttpServletRequest context) throws Exception {
        
        ScientificObjectDAOSesame scientificObjectDAO = new ScientificObjectDAOSesame();
        
        ScientificObject scientificObject = scientificObjectDTO.createObjectFromDTO();
        scientificObject.setUri(uri);
        POSTResultsReturn result = scientificObjectDAO.checkAndUpdateInContext(scientificObject, experiment);
        
        AbstractResultForm putResponse = null;
        if (result.getHttpStatus().equals(Response.Status.OK)
                || result.getHttpStatus().equals(Response.Status.CREATED)) {
            putResponse = new ResponseFormPOST(result.statusList);
            putResponse.getMetadata().setDatafiles(result.createdResources);
        } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                || result.getHttpStatus().equals(Response.Status.OK)
                || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
            putResponse = new ResponseFormPOST(result.statusList);
        }
        return Response.status(result.getHttpStatus()).entity(putResponse).build();
    }
    
    @GET
    @ApiOperation(value = "Get all scientific objects corresponding to the searched params given",
                  notes = "Retrieve all scientific objects authorized for the user corresponding to the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all scientific objects", response = ScientificObjectDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
         @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                         dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                         value = DocumentationAnnotation.ACCES_TOKEN,
                         example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScientificObjectsBySearch(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
        @ApiParam(value = "Search by URI", example = DocumentationAnnotation.EXAMPLE_SCIENTIFIC_OBJECT_URI) @QueryParam("uri") @URL String uri,
        @ApiParam(value = "Search by experiment URI", example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_URI) @QueryParam("experiment") @URL String experimentURI,
        @ApiParam(value = "Search by alias", example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_ALIAS) @QueryParam("alias") String alias,
        @ApiParam(value = "Search by rdfType", example = DocumentationAnnotation.EXAMPLE_SCIENTIFIC_OBJECT_TYPE) @QueryParam("rdfType") @URL String rdfType
    ) {
        ArrayList<ScientificObjectDTO> scientificObjectsToReturn = new ArrayList<>();
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<ScientificObjectDTO> getResponse;
        
        ScientificObjectDAOSesame scientificObjectDaoSesame = new ScientificObjectDAOSesame();
        scientificObjectDaoSesame.user = userSession.getUser();
        scientificObjectDaoSesame.setPage(page);
        scientificObjectDaoSesame.setPageSize(pageSize);
        
        ArrayList<ScientificObject> scientificObjects = scientificObjectDaoSesame.find(uri, rdfType, experimentURI, alias);
        
        if (scientificObjects == null) { //Request failure
            getResponse = new ResultForm<>(0, 0, scientificObjectsToReturn, true);
            return noResultFound(getResponse, statusList);
        } else if (scientificObjects.isEmpty()) { //No result
            getResponse = new ResultForm<>(0, 0, scientificObjectsToReturn, true);
            return noResultFound(getResponse, statusList);
        } else {
            //Convert all scientific objects to DTO
            scientificObjects.forEach((scientificObject) -> {
                scientificObjectsToReturn.add(new ScientificObjectDTO(scientificObject));
            });
            
            getResponse = new ResultForm<>(scientificObjectDaoSesame.getPageSize(), scientificObjectDaoSesame.getPage(), scientificObjectsToReturn, false);
            if (getResponse.getResult().dataSize() == 0) {
                return noResultFound(getResponse, statusList);
            } else {
                getResponse.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(getResponse).build();
            }
        }
    }
}
