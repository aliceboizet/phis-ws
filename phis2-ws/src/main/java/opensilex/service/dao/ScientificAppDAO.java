//******************************************************************************
//                                ScientificAppDAO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 15 sept. 2019
// Contact: Expression userEmail is undefined on line 6, column 15 in file:///home/charlero/GIT/GITHUB/phis-ws/phis2-ws/licenseheader.txt., anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import opensilex.service.authentication.Session;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.exception.ResourceAccessDeniedException;
import opensilex.service.dao.manager.DAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.model.Document;
import opensilex.service.model.ScientificAppDescription;
import opensilex.service.model.User;
import opensilex.service.shinyProxy.ShinyProxyService;
import static opensilex.service.shinyProxy.ShinyProxyService.SHINYPROXY_APP_DOCTYPE;

/**
 *
 * @author charlero
 */
public class ScientificAppDAO extends DAO<ScientificAppDescription> {

    public Session session;

    @Override
    public List<ScientificAppDescription> create(List<ScientificAppDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<ScientificAppDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ScientificAppDescription> update(List<ScientificAppDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScientificAppDescription find(ScientificAppDescription object) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScientificAppDescription findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<ScientificAppDescription> objects) throws DAOPersistenceException, DAODataErrorAggregateException, DAOPersistenceException, ResourceAccessDeniedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void startTransaction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void commitTransaction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void rollbackTransaction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param id maybe useful
     * @param label not used for now
     * @return
     */
    public ArrayList<ScientificAppDescription> find(String id, String label) {
        ArrayList<Document> documentsMetadata = listScientificAppMetadataFromTripleStore(id);
        return getShinyProxyAppListFromDocumentMetadata(documentsMetadata);
    }

    private ArrayList<ScientificAppDescription> getShinyProxyAppListFromDocumentMetadata(ArrayList<Document> documentsMetadata) {
        File shinyDockersFileDir = ShinyProxyService.SHINYPROXY_DOCKER_FILES.toFile();
        shinyDockersFileDir.mkdirs();
        ArrayList<ScientificAppDescription> shinyProxyAppList = new ArrayList<>();
        String sessionId = null;
        if (this.session != null) {
            sessionId = session.getId();
        }
        for (Document documentMetadata : documentsMetadata) {

            ScientificAppDescription shinyAppDescription = new ScientificAppDescription(
                    documentMetadata.getUri(),
                    documentMetadata.getTitle(),
                    documentMetadata.getComment(),
                    sessionId);
            shinyProxyAppList.add(shinyAppDescription);
        }
        return shinyProxyAppList;
    }

    private ArrayList<Document> listScientificAppMetadataFromTripleStore(String uri) {
        ArrayList<Document> documentsMetadata = new ArrayList<>();
        DocumentRdf4jDAO documentRdf4jDao = new DocumentRdf4jDAO();
        User userAdmin = new User(DocumentationAnnotation.EXAMPLE_USER_EMAIL);
        documentRdf4jDao.user = userAdmin;
        documentRdf4jDao.documentType = SHINYPROXY_APP_DOCTYPE;
        documentRdf4jDao.sortByDate = "desc";
        documentRdf4jDao.setPageSize(100);
        if (uri != null) {
            documentRdf4jDao.uri = uri;
        }
        // Retreive all documents for this specific type
        documentsMetadata = documentRdf4jDao.allPaginate();
        return documentsMetadata;
    }

}
