//******************************************************************************
//                                 Session.java 
// SILEX-PHIS
// Copyright © INRA 2015
// Creation date: 25 November 2015
// Contact: alice.boizet@inra.fr, arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.authentication;

import opensilex.service.resource.brapi.TokenPhenometaResult;
import opensilex.service.view.brapi.Metadata;

/**
 * Structure of a response representing a token corresponding to the BrAPI 
 * prerequisites. 
 * @author Samuël Chérimont
 */
public class TokenPhenometaResponseStructure {

    private final Metadata metadata;
    private final TokenPhenometaResult result; 

    public TokenPhenometaResponseStructure(TokenPhenometaResult result) {
        this.metadata = new Metadata(0, 0, 1);
        this.result = result;
    }    
}
