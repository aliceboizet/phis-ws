//******************************************************************************
//                                       PropertyDTO.java
//
// Author(s): Morgane Vidal <morgane.vidal@inra.fr>
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: 30 avr. 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  30 avr. 2018
// Subject:
//******************************************************************************
package phis2ws.service.resources.dto;

import io.swagger.annotations.ApiModelProperty;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.resources.dto.validation.interfaces.Required;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.resources.dto.validation.interfaces.URL;
import phis2ws.service.view.model.phis.Property;

/**
 * Represents the submitted JSON for the properties
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class PropertyDTO extends AbstractVerifiedClass {

    //property type (e.g. http://www.phenome-fppn.fr/vocabulary/2017#Variety)
    //null if it is a string (so not an uri)
    private String rdfType;
    //relation name (e.g. http://www.phenome-fppn.fr/vocabulary/2017#fromVariety)
    private String relation;
    //the value (e.g. http://www.phenome-fppn.fr/id/species/maize)
    private String value;

    @Override
    public Property createObjectFromDTO() {
        Property property = new Property();
        property.setRdfType(rdfType);
        property.setRelation(relation);
        property.setValue(value);
        
        return property;
    }
    
    @URL
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_SPECIES_RDF_TYPE)
    public String getRdfType() {
        return rdfType;
    }

    public void setRdfType(String rdfType) {
        this.rdfType = rdfType;
    }
    
    @URL
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_SPECIES_FROM_SPECIES)
    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
    
    @URL
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_SPECIES_URI)
    public String getValue() {
        return value;
    }
    
    
    public void setValue(String value) {
        this.value = value;
    }
}
