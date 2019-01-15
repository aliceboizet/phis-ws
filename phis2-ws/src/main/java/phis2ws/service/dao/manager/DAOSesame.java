//**********************************************************************************************
//                                       DAOSesame.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date: 11 jan. 2019
// Subject:This abstract class is the base of all Dao class for the Sesame TripleStore 
//***********************************************************************************************
package phis2ws.service.dao.manager;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import static org.apache.jena.sparql.vocabulary.VocabTestQuery.query;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.PropertiesFileManager;
import phis2ws.service.authentication.TokenManager;
import phis2ws.service.configuration.DateFormat;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.URINamespaces;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.model.User;
import phis2ws.service.ontologies.Xsd;
import phis2ws.service.utils.dates.Dates;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.utils.sparql.SPARQLStringBuilder;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.ResponseFormPOST;

/**
 * Répresente une définition de la classe DAO permettant de se connecter au
 * TripleStore Sesame
 *
 * @author Arnaud Charleroy
 * @update [Morgane Vidal] 04 Oct, 2018 : Rename existObject to existUri and change the query of the method existUri.
 * @update [Andréas Garcia] 11 Jan, 2019 : Add generic date time stamp comparison SparQL filter.
 * @param <T>
 */
public abstract class DAOSesame<T> {

    final static Logger LOGGER = LoggerFactory.getLogger(DAOSesame.class);
    protected static final String PROPERTY_FILENAME = "sesame_rdf_config";
    //SILEX:test
    // Pour le soucis de pool de connexion plein
    protected static final String SESAME_SERVER = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
    protected static final String REPOSITORY_ID = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "repositoryID");
    //\SILEX:test

    //used for logger
    protected static final String SPARQL_SELECT_QUERY = "SPARQL query : ";
    
    protected static final String COUNT_ELEMENT_QUERY = "count";
    
    /**
     * The following constants are SPARQL variables name used for each subclass 
     * to query the triplestore.
     */
    protected static final String URI = "uri";
    protected static final String URI_SELECT_NAME_SPARQL = "?" + URI;
    protected static final String RDF_TYPE = "rdfType";
    protected static final String RDF_TYPE_SELECT_NAME_SPARQL = "?" + RDF_TYPE;
    protected static final String LABEL = "label";
    protected static final String COMMENT = "comment";
    
    protected static final String DATETIME_SELECT_NAME = "dateTime";
    protected static final String DATETIME_SELECT_NAME_SPARQL = "?" + DATETIME_SELECT_NAME;
    
    protected static final String DATE_RANGE_START_DATETIME_SELECT_NAME = "dateRangeStartDateTime";
    protected static final String DATE_RANGE_START_DATETIME_SELECT_NAME_SPARQL = "?" + DATE_RANGE_START_DATETIME_SELECT_NAME;
    
    protected static final String DATE_RANGE_END_DATETIME_SELECT_NAME = "dateRangeEndDateTime";
    protected static final String DATE_RANGE_END_DATETIME_SELECT_NAME_SPARQL = "?" + DATE_RANGE_END_DATETIME_SELECT_NAME;
    
    protected final String DATETIMESTAMP_FORMAT_SPARQL = DateFormat.YMDTHMSZZ.toString();
    
    //Triplestore relations
    protected static final URINamespaces ONTOLOGIES = new URINamespaces();

    protected static Repository rep;
    private RepositoryConnection connection;

    protected static String resourceType;

    public User user;
    protected Integer page;
    protected Integer pageSize;
    /**
     * User ip adress
     */
    public String remoteUserAdress;

    public DAOSesame() {
        try {
            String sesameServer = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
            String repositoryID = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "repositoryID");
            rep = new HTTPRepository(sesameServer, repositoryID); //Stockage triplestore Sesame
            rep.initialize();
            setConnection(rep.getConnection());
        } catch (Exception e) {
            ResponseFormPOST postForm = new ResponseFormPOST(new Status("Can't connect to triplestore", StatusCodeMsg.ERR, e.getMessage()));
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(postForm).build());
        }
    }

    public DAOSesame(String repositoryID) {
        try {
            String sesameServer = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
            rep = new HTTPRepository(sesameServer, repositoryID); //Stockage triplestore Sesame
            rep.initialize();
            setConnection(rep.getConnection());
        } catch (Exception e) {
            ResponseFormPOST postForm = new ResponseFormPOST(new Status("Can't connect to triplestore", StatusCodeMsg.ERR, e.getMessage()));
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(postForm).build());
        }
    }

    public RepositoryConnection getConnection() {
        return connection;
    }

    public final void setConnection(RepositoryConnection connection) {
        this.connection = connection;
    }

    public static Repository getRepository() {
        return rep;
    }

    /**
     * La page de l'api brapi commence à 0
     *
     * @return numéro de la page courante
     */
    public Integer getPage() {
        if (page == null || pageSize < 0) {
            return 0;
        }
        return page;
    }

    /**
     * La page de l'api brapi pour pouvoir l'utiliser pour la pagination dans
     * une base de données
     *
     * @return numéro de la page courante + 1
     */
    public Integer getPageForDBQuery() {
        if (page == null || pageSize < 0) {
            return 1;
        }
        return page + 1;
    }

    /**
     * Définit le paramètre page
     *
     * @param page
     */
    public void setPage(Integer page) {
        if (page < 0) {
            this.page = Integer.valueOf(DefaultBrapiPaginationValues.PAGE);
        }
        this.page = page;
    }

    /**
     * Retourne le paramètre taille de la page
     *
     * @return
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return Integer.valueOf(DefaultBrapiPaginationValues.PAGE_SIZE);
        }
        return pageSize;
    }

    /**
     * Définit le paramètre taille de page
     *
     * @param pageSize
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Méthode de test d'existence d'un sujet par triplet
     *
     * @param subject
     * @param predicate
     * @param object
     * @return boolean
     */
    public boolean exist(String subject, String predicate, String object) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        boolean exist = false;
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect(null);
        query.appendTriplet(subject, predicate, object, null);
        query.appendParameters("LIMIT 1");
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                exist = true;
            }
        }
        return exist;
    }

    /**
     * 
     * @param uri the uri to test
     * @example
     * ASK {
     *  VALUES (?r) { (<http://www.w3.org/2000/01/rdf-schema#Literal>) }
     *  { ?r ?p ?o }
     *  UNION
     *  { ?s ?r ?o }
     *  UNION
     *  { ?s ?p ?r }
     * }
     * @return true if the uri exist in the triplestore
     *         false if it does not exist
     */
    public boolean existUri(String uri) {
        if (uri == null) {
            return false;
        }
        try {
            //SILEX:warning
            //Remember to add rdf, rdfs and owl ontologies in your triplestore
            //\SILEX:warning
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendAsk("VALUES (?r) { (<" + uri + ">) }\n" +
                        "    { ?r ?p ?o }\n" +
                        "    UNION\n" +
                        "    { ?s ?r ?o }\n" +
                        "    UNION\n" +
                        "    { ?s ?p ?r }\n");
            
            LOGGER.debug(SPARQL_SELECT_QUERY + query.toString());
            BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
            return booleanQuery.evaluate();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Méthode de récupération d'élement d'existence par triplet
     *
     * @param subject
     * @param predicate
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public String getValueFromPredicate(String subject, String predicate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String value = null;
        if (subject != null || predicate != null) {
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendSelect("?x");
            query.appendTriplet(subject, predicate, "?x", null);
            query.appendParameters("LIMIT 1");
            LOGGER.trace(query.toString());
            TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    value = result.next().getBinding("x").getValue().stringValue();
                }
            }
            LOGGER.trace(value);
        }
        return value;
    }

    /**
     * Fonction qui permet de créer la partie commune d'une requête à la fois
     * pour lister les éléments et les récupérés
     *
     * @return SPARQLQueryBuilder
     */
    abstract protected SPARQLQueryBuilder prepareSearchQuery();
    
    /** Add a filter to the search query comparing a SPARQL dateTimeStamp 
     * variable to a date. 
     * SPARQL dateTimeStamp dates have to be handled in a specific way as 
     * the comparison operators (<, >, etc.) aren't available for dateTimeStamp
     * objects.
     * @see <a href="https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping">
     * SparQL Operator Mapping
     * </a>
     * @param query
     * @param filterDateString
     * @param filterDateFormat
     * @param filterDateSparqlVariable SPARQL variable (?abc format)
     * @param comparisonSign e.g >, >=, <, <= 
     * @param dateTimeStampToCompareSparqlVariable the SPARQL variable 
     * (?abc format) of the dateTimeStamp to which the date has to be compared
     * @example SparQL code added to the query :
     *   BIND(xsd:dateTime(str("2017-09-10T12:00:00+01:00")) as ?dateRangeStartDateTime) .
     *   FILTER ( (?dateRangeStartDateTime <= ?dateTime) ) 
     */
    protected void filterSearchQueryWithDateTimeStampComparison( SPARQLStringBuilder query, String filterDateString, String filterDateFormat, String filterDateSparqlVariable, String comparisonSign, String dateTimeStampToCompareSparqlVariable){
        
        DateTime filterDate = Dates.stringToDateTimeWithGivenPattern(filterDateString, filterDateFormat);
        
        String filterDateStringInSparqlDateTimeStampFormat = DateTimeFormat.forPattern(DATETIMESTAMP_FORMAT_SPARQL).print(filterDate);

        query.appendToBody("\nBIND(<" + Xsd.FUNCTION_DATETIME.toString() + ">(str(\"" + filterDateStringInSparqlDateTimeStampFormat + "\")) as " + filterDateSparqlVariable + ") .");
        
        query.appendAndFilter(filterDateSparqlVariable + comparisonSign + dateTimeStampToCompareSparqlVariable);
    }

    /**
     * Append a filter to select only the results whose datetime is 
     * included in the date range in parameter
     * @param query
     * @param filterRangeDatesStringFormat
     * @param filterRangeStartDateString
     * @param filterRangeEndDateString
     * @param dateTimeStampToCompareSparqleVariable the SPARQL variable (?abc 
     * format) of the dateTimeStamp to compare to the range
     * @example SparQL code added to the query :
     *   BIND(xsd:dateTime(str(?dateTimeStamp)) as ?dateTime) .
     *   BIND(xsd:dateTime(str("2017-09-10T12:00:00+01:00")) as ?dateRangeStartDateTime) .
     *   BIND(xsd:dateTime(str("2017-09-12T12:00:00+01:00")) as ?dateRangeEndDateTime) .
     *   FILTER ( (?dateRangeStartDateTime <= ?dateTime) && (?dateRangeEndDateTime >= ?dateTime) ) 
     */
    protected void filterSearchQueryWithDateRangeComparisonWithDateTimeStamp(SPARQLStringBuilder query, String filterRangeDatesStringFormat, String filterRangeStartDateString, String filterRangeEndDateString, String dateTimeStampToCompareSparqleVariable){
        
        query.appendToBody("\nBIND(<" + Xsd.FUNCTION_DATETIME.toString() 
                + ">(str(" + dateTimeStampToCompareSparqleVariable 
                + ")) as " + DATETIME_SELECT_NAME_SPARQL + ") .");
        
        if (filterRangeStartDateString != null){
            filterSearchQueryWithDateTimeStampComparison(query, filterRangeStartDateString, filterRangeDatesStringFormat, DATE_RANGE_START_DATETIME_SELECT_NAME_SPARQL, " <= ", DATETIME_SELECT_NAME_SPARQL);
        }
        if (filterRangeEndDateString != null){
            filterSearchQueryWithDateTimeStampComparison(query, filterRangeEndDateString, filterRangeDatesStringFormat, DATE_RANGE_END_DATETIME_SELECT_NAME_SPARQL, " >= ", DATETIME_SELECT_NAME_SPARQL);
        }
    }

    /**
     * Compte le nombre d'élement retournés par la requête
     *
     * @return Integer
     */
    public abstract Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException;

    /**
     *
     * @return Les logs qui seront utilisés pour la traçabilité
     */
    protected String getTraceabilityLogs() {
        String log = "";
        if (remoteUserAdress != null) {
            log += "IP Address " + remoteUserAdress + " - ";
        }
        if (user != null) {
            log += "User : " + user.getEmail() + " - ";
        }
        return log;
    }

    /**
     * Définit un objet utilisateur à partir d'un identifiant
     *
     * @param id identifiant
     */
    public void setUser(String id) {
//        LOGGER.debug(JsonConverter.ConvertToJson(TokenManager.Instance().getSession(id).getUser()));
        if (TokenManager.Instance().getSession(id).getUser() == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            this.user = TokenManager.Instance().getSession(id).getUser();
        }
    }
    
    /**
     * Add object properties to a given object.
     * @param subjectUri the subject uri which will have the object properties uris
     * @param predicateUri the uri of the predicates
     * @param objectPropertiesUris the list of the object properties to link to the subject
     * @param graphUri
     * @example
     * INSERT DATA {
     *      GRAPH <http://www.phenome-fppn.fr/diaphen/sensors> { 
     *          <http://www.phenome-fppn.fr/diaphen/2018/s18533>  <http://www.phenome-fppn.fr/vocabulary/2017#measures>  <http://www.phenome-fppn.fr/id/variables/v001> . 
     * }}
     * @return true if the insertion has been done
     *         false if an error occurred (see the error logs to get more details)
     */
    protected boolean addObjectProperties(String subjectUri, String predicateUri, List<String> objectPropertiesUris, String graphUri) {
        //Generates insert query
        UpdateBuilder spql = new UpdateBuilder();
        Node graph  = NodeFactory.createURI(graphUri);
        
        objectPropertiesUris.forEach((objectProperty) -> {
            Node subjectUriNode  = NodeFactory.createURI(subjectUri);
            Node predicateUriNode  = NodeFactory.createURI(predicateUri);
            Node objectPropertyNode  = NodeFactory.createURI(objectProperty);
            
            spql.addInsert(graph, subjectUriNode, predicateUriNode, objectPropertyNode);
        });
        
        LOGGER.debug(SPARQL_SELECT_QUERY + query.toString());
        
        //Insert the properties in the triplestore
        Update prepareUpdate = getConnection().prepareUpdate(QueryLanguage.SPARQL, spql.buildRequest().toString());
        try {
            prepareUpdate.execute();
        } catch (UpdateExecutionException ex) {
            LOGGER.error("Add object properties error : " + ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Delete the given object properties.
     * @param subjectUri
     * @param predicateUri
     * @param objectPropertiesUris
     * @example
     * DELETE WHERE { 
     *  <http://www.phenome-fppn.fr/diaphen/2018/s18533> <http://www.phenome-fppn.fr/vocabulary/2017#measures> <http://www.phenome-fppn.fr/id/variables/v001> .  
     * }
     * @return true if the object properties have been deleted
     *         false if the delete has not been done.
     */
    protected boolean deleteObjectProperties(String subjectUri, String predicateUri, List<String> objectPropertiesUris) {
        //1. Generates delete query
        UpdateBuilder query = new UpdateBuilder();
        
        Resource subject = ResourceFactory.createResource(subjectUri);
        Property predicate = ResourceFactory.createProperty(predicateUri);        
        
        for (String objectProperty : objectPropertiesUris) {
            Node object = NodeFactory.createURI(objectProperty);
            query.addWhere(subject, predicate, object);
        }
        
        UpdateDeleteWhere request = query.buildDeleteWhere();
        LOGGER.debug(request.toString());
        
        //2. Delete data in the triplestore
        Update prepareDelete = getConnection().prepareUpdate(QueryLanguage.SPARQL, request.toString());
        try {
            prepareDelete.execute();
        } catch (UpdateExecutionException ex) {
            LOGGER.error("Delete object properties error : " + ex.getMessage());
            return false;
        }
        
        return true;
    }
}
