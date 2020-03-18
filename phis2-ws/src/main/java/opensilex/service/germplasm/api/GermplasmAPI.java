/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opensilex.service.germplasm.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opensilex.core.experiment.api.ExperimentCreationDTO;
import org.opensilex.rest.authentication.ApiCredential;
import org.opensilex.rest.authentication.ApiProtected;
import org.opensilex.server.response.ErrorResponse;
import org.opensilex.server.response.ObjectUriResponse;
import org.opensilex.sparql.service.SPARQLService;

/**
 *
 * @author boizetal
 */
@Api(GermplasmAPI.CREDENTIAL_GERMPLASM_GROUP_ID)
@Path("/phis/germplasm")
public class GermplasmAPI {
    
    public static final String CREDENTIAL_GERMPLASM_GROUP_ID = "Germplasm";
    public static final String CREDENTIAL_GERMPLASM_GROUP_LABEL_KEY = "credential-groups.germplasm";
    
    public static final String CREDENTIAL_GERMPLASM_MODIFICATION_ID = "germplasm-modification";
    public static final String CREDENTIAL_GERMPLASM_MODIFICATION_LABEL_KEY = "credential.germplasm.modification";

    public static final String CREDENTIAL_GERMPLASM_READ_ID = "germplasm-read";
    public static final String CREDENTIAL_GERMPLASM_READ_LABEL_KEY = "credential.germplasm.read";

    public static final String CREDENTIAL_GERMPLASM_DELETE_ID = "experiment-delete";
    public static final String CREDENTIAL_GERMPLASM_DELETE_LABEL_KEY = "credential.germplasm.delete";

    protected static final String GERMPLASM_EXAMPLE_URI = "http://opensilex/set/experiments/ZA17";
    
    @Inject
    public GermplasmAPI(SPARQLService sparql) {
        this.sparql = sparql;
    }
    
    private final SPARQLService sparql;
    
    @POST
    @Path("create")
    @ApiOperation("Create a germplasm")
    @ApiProtected
    @ApiCredential(
        groupId = CREDENTIAL_GERMPLASM_GROUP_ID,
        groupLabelKey = CREDENTIAL_GERMPLASM_GROUP_LABEL_KEY,
        credentialId = CREDENTIAL_GERMPLASM_MODIFICATION_ID,
        credentialLabelKey = CREDENTIAL_GERMPLASM_MODIFICATION_LABEL_KEY
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Create an experiment", response = ObjectUriResponse.class),
        @ApiResponse(code = 409, message = "A germplasm with the same URI already exists", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResponse.class)})
    
    public Response createGermplasm(
        @ApiParam("Germplasm description") @Valid GermplasmCreationDTO germplasmDTO
    ) {
        try {
            
        }
    }
}
