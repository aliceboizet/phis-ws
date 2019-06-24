//******************************************************************************
//                                TokenPhenometaResult.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 12 juin 2019
// Contact: Expression userEmail is undefined on line 6, column 15 in file:///home/boizetal/OpenSilex/phis-ws/phis2-ws/licenseheader.txt., anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.brapi;

import opensilex.service.PropertiesFileManager;

/**
 *
 * @author boizetal
 */
public class TokenPhenometaResult {
    
    private final String userDisplayName;
    private final String access_token;
    private final String expires_in;

    public TokenPhenometaResult(String userDisplayName, String access_token) {
        this.userDisplayName = userDisplayName;
        this.access_token = access_token;
        this.expires_in = PropertiesFileManager.getConfigFileProperty("service", "sessionTime");
    }
    
    public TokenPhenometaResult(String userDisplayName, String access_token, String expires_in) {
        this.userDisplayName = userDisplayName;
        this.access_token = access_token;
        if (expires_in == null) {
            this.expires_in = PropertiesFileManager.getConfigFileProperty("service", "sessionTime");
        } else {
            this.expires_in = expires_in;
        }
    }   
    
}
