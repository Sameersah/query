package com.adobe.cq.query;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.Query;

@SlingServlet(paths="/bin/myQueryBuilderSearch", methods = "GET", metatype=true)
public class searchService extends org.apache.sling.api.servlets.SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L; 

	//Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private QueryBuilder builder;
	
    private Session session;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {
    	
    	Map<String, Object> param = new HashMap<String, Object>();
    	//missing line param.put difference
    	param.put(ResourceResolverFactory.USER, "jquerybuilder");
    	ResourceResolver resolver = null;
    	
    	try {
    		//Invoke the adaptTo method to create a Session used to create a QueryManager
    		//difference 
            resolver = resolverFactory.getResourceResolver(param);
            session = resolver.adaptTo(Session.class);
            session = resolver.adaptTo(Session.class);
            
            String fulltextSearchTerm = "Geometrixx";
            
            // create query description as hash map (simplest way, same as form post)
            Map<String, String> map = new HashMap<String, String>();
            
         // create query description as hash map (simplest way, same as form post)
            
            map.put("path", "/content");
            map.put("type", "cq:Page");
            map.put("group.p.or", "true"); // combine this group with OR
            map.put("group.1_fulltext", fulltextSearchTerm);
            map.put("group.1_fulltext.relPath", "jcr:content");
            map.put("group.2_fulltext", fulltextSearchTerm);
            map.put("group.2_fulltext.relPath", "jcr:content/@cq:tags");
            // can be done in map or with Query methods
            map.put("p.offset", "0"); // same as query.setStart(0) below
            map.put("p.limit", "20"); // same as query.setHitsPerPage(20) below
                              
            Query query = builder.createQuery(PredicateGroup.create(map), session);
                                
            query.setStart(0);
            query.setHitsPerPage(20);
                        
            SearchResult result = query.getResult();
            
         // paging metadata
            int hitsPerPage = result.getHits().size(); // 20 (set above) or lower
            long totalMatches = result.getTotalMatches();
            long offset = result.getStartIndex();
            long numberOfPages = totalMatches / 20;
            
          //Place the results in XML to return to client
            DocumentBuilderFactory factory =         DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
      //Start building the XML to pass back to the AEM client
        Element root = doc.createElement( "results" );
        doc.appendChild( root );
                       
                       
        // iterating over the results
        for (Hit hit : result.getHits()) {
               String path = hit.getPath();
                //Create a result element
                Element resultel = doc.createElement( "result" );
                root.appendChild( resultel );
                           
                Element pathel = doc.createElement( "path" );
                pathel.appendChild( doc.createTextNode(path ) );
                resultel.appendChild( pathel );
                                         
        }
       
        //close the session
        session.logout();  
       
        //Return the JSON formatted data
    response.getWriter().write(convertToString(doc));
                            
    	}
    	catch (Exception e){
    		
    	}
    }
    
    private String convertToString(Document xml)
    {
    try {
       Transformer transformer = TransformerFactory.newInstance().newTransformer();
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(xml);
      transformer.transform(source, result);
      return result.getWriter().toString();
    } catch(Exception ex) {
              ex.printStackTrace();
    }
      return null;
         } 
}
