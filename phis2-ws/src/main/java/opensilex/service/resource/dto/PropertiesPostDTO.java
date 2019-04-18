//******************************************************************************
//                          PropertiesPostDTO.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 26 Sept. 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.dto;

import java.util.ArrayList;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import opensilex.service.resource.dto.manager.AbstractVerifiedClass;
import opensilex.service.resource.dto.rdfResourceDefinition.PropertyPostDTO;
import opensilex.service.resource.validation.interfaces.Required;

/**
 * Property POST DTO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class PropertiesPostDTO extends AbstractVerifiedClass {
    
    /**
     * Label of the object concerned by the properties.
     * @example rt01
     */
    protected String label;
    
    /**
     * List of the properties of the object.
     */
    protected ArrayList<PropertyPostDTO> properties = new ArrayList<>();

    @Override
    public Object createObjectFromDTO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Required
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @NotEmpty
    @NotNull
    @Valid
    public ArrayList<PropertyPostDTO> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<PropertyPostDTO> properties) {
        this.properties = properties;
    }
}
