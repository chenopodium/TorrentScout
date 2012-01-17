/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.scout.rest;

import com.iontorrent.jason.JSONException;
import com.iontorrent.jason.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Jersey REST client generated for REST resource:RundbExperimentsResource [/rundbExperiments/]<br>
 *  USAGE:<pre>
 *        ManyExperimentsClient client = new ManyExperimentsClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 *  </pre>
 * @author Chantal Roth
 */
public class ManyExperimentsClient {

    private WebResource webResource;
    private Client client;
    //http://ionwest.itw/rundb/api/v1/results/?format=json
   

    public ManyExperimentsClient(String BASE_URI) {
       this(BASE_URI, "rundb/api/v1/experiment/");
    }
    public ManyExperimentsClient(String BASE_URI, String uri) {
        
        int col = BASE_URI.lastIndexOf(":");
        if (col>7) {
            // remove port
            BASE_URI = BASE_URI.substring(0, col);
            p("Cut off port from URI: "+BASE_URI);
        }
        p("Connecting to "+BASE_URI);
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        webResource = client.resource(BASE_URI).path(uri);

    }
    public ClientResponse post_XML(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }

    public ClientResponse post_JSON(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(ClientResponse.class, requestEntity);
    }

    public <T> T get_XML(Class<T> responseType, String max, String start, String query, String expandLevel) throws UniformInterfaceException {
        WebResource resource = webResource;

        if (max != null) {
            resource = resource.queryParam("max", max);
        }
        if (start != null) {
            resource = resource.queryParam("start", start);
        }
        if (query != null) {
            resource = resource.queryParam("query", query);
        }
        if (expandLevel != null) {
            resource = resource.queryParam("expandLevel", expandLevel);
        }
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T get_JSON(Class<T> responseType, String max, String start, String query) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (max != null) {
            resource = resource.queryParam("limit", max);
        }
        else  resource = resource.queryParam("limit", "1000");
        if (start != null) {
            resource = resource.queryParam("start", start);
        }
        if (query != null) {
            resource = resource.queryParam("query", query);
        }
              
//        resource = resource.queryParam("order_by", "date");
//        try {
           return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
//        }
//        catch (Exception e) {
//            // order_by not ok yet
//            resource.queryParam("order_by","");
//            
//            return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
//        }
    }

    public static void main(String[] args) {
    }

    public JSONObject getJSSONObject() {
        setUsernamePassword("ionuser", "ionuser");
        String response = get_JSON(String.class, null, null, null);
      //  p("Got response:" + response + ", " + response.getClass().getName());
        JSONObject js = null;
        try {
            js = new JSONObject(response);
        } catch (JSONException ex) {
            Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
     //   p("Json: " + js);
       return js;
    }

//    public static ArrayList<RundbExperiment> getAllExperiments() {
//        ManyExperimentsClient client = new ManyExperimentsClient();
//        client.setUsernamePassword("ionuser", "ionuser");
//        String response = client.get_JSON(String.class, null, null, null, null);
//        p("Got response:" + response + ", " + response.getClass().getName());
//        JSONObject js = null;
//        try {
//            js = new JSONObject(response);
//        } catch (JSONException ex) {
//            Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        client.close();
//        p("Json: " + js);
//        ArrayList<RundbExperiment> explist = client.getExperiments(js);
//        p("Got exp list: "+explist);
//        // do whatever with response
//        return explist;
//    }
    public void close() {
        client.destroy();
    }

    public void setUsernamePassword(String username, String password) {
        client.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(username, password));
    }

    private static void p(String string) {
        System.out.println("ListOfExpClient: " + string);
    }
}
