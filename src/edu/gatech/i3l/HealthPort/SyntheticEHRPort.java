/**
 * 
 */
package edu.gatech.i3l.HealthPort;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Condition;
import ca.uhn.fhir.model.dstu.resource.MedicationPrescription;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.valueset.ObservationReliabilityEnum;
import ca.uhn.fhir.model.dstu.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;

/**
 * @author MC142
 *
 */
public class SyntheticEHRPort implements HealthPortFHIRIntf {
	
	String serverName = "localhost";
	String url = "jdbc:mysql://" + serverName;
	String username = "healthport";
	String password = "i3lworks";

	public ArrayList<Observation> getObservations(HealthPortUserInfo userInfo) {
		// TODO Auto-generated method stub
		ArrayList<Observation> retVal = new ArrayList<Observation>();
    	ArrayList<String> retList = new ArrayList<String>();
    	
    	//Get all Observations
		//retList = getObs(response,userInfo.recordId, userInfo.personId);
		//retVal = setWeightObservation(userInfo.userId,retList,retVal);
    	String serverName = "localhost";
		String url = "jdbc:mysql://" + serverName;
		String username = "healthport";
		String password = "i3lworks";
		//String driverName = "org.gjt.mm.mysql.Driver";	
		String dbName = "OMOP";
	    Connection conn = null;
	    Statement stmt = null;
	    
	    try {
			//Class.forName(driverName);
			String URL = url + "/" + dbName;
			conn = DriverManager.getConnection(URL, username, password);
			stmt = conn.createStatement();
			String sql = "SELECT observation_value, observation_concept_id FROM observation WHERE person_id= " + userInfo.personId;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
		         String obsVal = rs.getString("observation_value");
		         String obsConceptId = rs.getString("observation_value");
		         retList.clear();
		         retList.add(obsVal);
		         //retList.add(obsConceptId);
		         retVal = setObservation(userInfo.personId,null,retList,retVal);
			}
		} catch (SQLException se) {
			// TODO Auto-generated catch block
			se.printStackTrace();
			}
		return retVal;
	}

	public ArrayList<Condition> getConditions(HealthPortUserInfo userInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<MedicationPrescription> getMedicationPrescriptions(
			HealthPortUserInfo userInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public Observation getObservation(String resourceId) {
		//Observation obs = new Observation();
		//String responseStr = null;
		//ArrayList<String> retList = new ArrayList<String>();
		//ArrayList<Observation> retVal = new ArrayList<Observation>();
		//Observation finalRetVal = new Observation();
		//String type = null;
		String[] Ids  = resourceId.split("\\-",3);
    	//Ids[0] -> person id
		//Ids[1] -> count 
		//Ids[2] -> concept id -> eg. 8302-2 for height

		
		//String driverName = "org.gjt.mm.mysql.Driver";	
		String dbName = "OMOP";
	    Connection conn = null;
	    Statement stmt = null;
	    String obsVal = null;
	    try {
			//Class.forName(driverName);
			String URL = url + "/" + dbName;
			conn = DriverManager.getConnection(URL, username, password);
			stmt = conn.createStatement();
			String sql = "SELECT observation_value FROM observation WHERE person_id= " + Ids[0] + " and observation_concept_id= " + Ids[2];
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
		         obsVal = rs.getString("observation_value");
		         //retList.add(obsVal);
		         //retVal = setObservation(Ids[0],Ids[2],retList,retVal);
			}
		} catch (SQLException se) {
			// TODO Auto-generated catch block
			se.printStackTrace();
			}
	    /*Integer index = Integer.parseInt(Ids[1]);
    	if (!index.equals(0)){
    		finalRetVal = retVal.get(index);
    	}
    	else{
    		finalRetVal = retVal.get(0);
    	}*/
	    int count = 0;
		FhirContext ctx = new FhirContext();
		Observation obs = new Observation();
		obs.setId(Ids[0] + "-"+count+"-"+ Ids[2]);
		//String nameCode = getCode("Body weight");
		//obs.setName(new CodeableConceptDt("http://loinc.org",nameCode)); 
		//QuantityDt quantity = new QuantityDt(Double.parseDouble(retList.get(i+1))).setUnits(retList.get(i+2));
		StringDt val = new StringDt(obsVal);
		obs.setValue(val);
		//obs.setComments("Body Weight");// if required, do -> if(Id[2] ==""){ set as ""} else{}
		obs.setStatus(ObservationStatusEnum.FINAL);
		obs.setReliability(ObservationReliabilityEnum.OK);
		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	    String output = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(obs);
	    obs.getText().setDiv(output);
		//finalRetVal.add(obs);
		return obs;
	    
	}
	
	static public ArrayList<Observation> setObservation(String userId, String nameCode, ArrayList<String> retList, ArrayList<Observation> retVal){
		int count = 0;
		FhirContext ctx = new FhirContext();
		for (int i = 0; i < retList.size(); i=i+3) {
		Observation obs = new Observation();
		obs.setId(userId + "-"+count+"-"+ retList.get(i));
		//String nameCode = getCode("Body weight");
		obs.setName(new CodeableConceptDt("http://loinc.org",nameCode)); 
		QuantityDt quantity = new QuantityDt(Double.parseDouble(retList.get(i+1))).setUnits(retList.get(i+2));
		obs.setValue(quantity);
		//obs.setComments("Body Weight");
		obs.setStatus(ObservationStatusEnum.FINAL);
		obs.setReliability(ObservationReliabilityEnum.OK);
		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	    String output = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(obs);
	    obs.getText().setDiv(output);
	  
		retVal.add(obs);
		}
		return retVal;
	}

	public Condition getCondition(String resourceId) {
		// TODO Auto-generated method stub
		String[] data = resourceId.split("-");
        System.out.println(data[1]);
		return null;
	}

	
}