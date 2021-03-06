package org.biocaddie.citationanalysis.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;


/**
 * This is a utility class to generate data for charts for various purposes as explained within the methods.
 * It is not intended to use from command line. But can be modified later.
 */
public class GenerateDataForCharts {
	final static Charset ENCODING = StandardCharsets.UTF_8;
    final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * The main function.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		// "/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network.net"
		// "/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network_metrics_d_0.5.txt"
		// "/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/PdbId_PubMedId_April29.csv"
		if(args.length < 6){
    		System.out.println("Call: java org.biocaddie.citationanalysis.utility.GenerateDataForCharts <paperOrPdb> <pageRankorCiteCnt> <top100orOutliers> <network> <networkMetrics> <PdbIdPubMedId>");
    		System.out.println("<paperOrPdb>: 1:sort all papers  2: sort only PDB primary citations");
    		System.out.println("<pageRankorCiteCnt>: 1:use pageRank  2: use CiteCount");
    		System.out.println("<top100orOutliers>: 1:top100  2: outliers within top100");
    		System.exit(1);
    	}
		
		int paperOrPdb = Integer.valueOf(args[0]).intValue();        // 1:sort all papers  2: sort only PDB primary citations
		int pageRankorCiteCnt = Integer.valueOf(args[1]).intValue(); //1:use pageRank  2: use CiteCount
		int top100orOutliers = Integer.valueOf(args[2]).intValue();  //1:top100  2: outliers within top100
		String networkPath = args[3];
		String networkMetricsPath = args[4];
		String pdbIdPubMedIdPath =  args[5];
		
		sortAndFindTop100PapersOrPDBIds(paperOrPdb, pageRankorCiteCnt, top100orOutliers, networkPath, networkMetricsPath, pdbIdPubMedIdPath);

	//	generateChartForDumpingFactor();

		/*	This code generates chart for citations per year
		 * Map<String, YearCitations> years_map = new HashMap<String, YearCitations>(); // (key: nodeId value: count)
		for (Iterator<Map.Entry<Integer, Node>> iter = network.nodeMap.entrySet().iterator(); iter.hasNext(); ) {	
			Node n = iter.next().getValue();
			String pubYear = n.name;
			Double citeCount = Double.valueOf(n.inLinks.size());
		    if (years_map.containsKey(n.name)){  	
		    	YearCitations yearCitations = years_map.get(pubYear);
		    	yearCitations.totalCitationCount += citeCount ;
	    		yearCitations.numOfPapers++;
		    	if (n.inLinks.size()==0 && n.outLinks.size()==0)
		    		yearCitations.numOfDanglingPapers++;		    	
		    }else{
		    	if (n.inLinks.size()==0 && n.outLinks.size()==0)
		    		years_map.put(pubYear, new YearCitations(pubYear, citeCount, 1.0, 1.0));
		    	else
		    		years_map.put(pubYear, new YearCitations(pubYear, citeCount, 1.0, 0.0));		    				    	
		    }		    
		}
		
	    Map<String, YearCitations> sorted_years_map = new TreeMap<String, YearCitations>(years_map);
	    years_map = sorted_years_map;
	    double numOfYears = years_map.size();

	    for (Iterator<Map.Entry<String, YearCitations>> iter = years_map.entrySet().iterator(); iter.hasNext(); ) {	    		
	    	YearCitations yearCitations = iter.next().getValue();

	    	//System.out.println(yearCitations.year + "|" + yearCitations.totalCitationCount + "|"+ yearCitations.numOfPapers + "|"+ yearCitations.totalPageRank);
	    	System.out.println(yearCitations.year +"|" +yearCitations.numOfPapers.intValue() + "|" + yearCitations.totalCitationCount + "|" + yearCitations.numOfDanglingPapers);
	    }
	    */		

//		lightPaperCitationNetwork();
	}

	//paper_citation_network is too large 3.7GB probably because of nodeNames (title, journalName etc.) we put only PubMedId as nodeName 
	private static void lightPaperCitationNetwork() throws Exception{
		
	    BufferedWriter out = new BufferedWriter(new FileWriter(new File("/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network_gt10_light.net")));

		
	    BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network.net"), ENCODING);
		String line = null; String currentPart = ""; int lineCnt = 0;
	    while ((line = reader.readLine()) != null) {	    	
	    	line = line.trim();
	    	lineCnt++;
        	if ( ( lineCnt % 1000000 ) == 0 )
        		System.out.println( lineCnt + " " + dateFormat.format(new Date()));
	    	
	    	if (line.length() > 9 && line.substring(0, 9).equalsIgnoreCase("*Vertices")){
	    		currentPart = "nodes";	                   	            
	    		System.out.println("Reading nodes...");
	    		out.write(line); out.newLine();
	    		continue;
	    	} 

	    	if (line.length() > 6 && ( line.substring(0, 6).equalsIgnoreCase("*Edges") || line.substring(0, 5).equalsIgnoreCase("*Arcs"))){
	    		currentPart = "links";
	    		System.out.println("Reading links...");
	    		out.write(line); out.newLine();
	    		continue;	    		
	    	} 
	    	
	    	if (currentPart.equals("nodes")){
	    	    int nameStart = line.indexOf("\"");
	    	    int nameEnd = line.lastIndexOf("\"");
	    	    
	    	    Integer id = Integer.valueOf(line.substring(0, nameStart-1));	    	    
	    	    String pubmed_id = line.substring(nameStart+1, nameEnd).split(Pattern.quote("||"))[1].trim();
	    	    out.write(id + " \"" + pubmed_id + "\""); out.newLine();
	    	}else{
	    		out.write(line); out.newLine();
	    	}
	    	
	    }
		
	    reader.close();
        out.flush();       
        out.close();   
	}
	
	/**
	 * 
	 * @param paperOrPDB: 1:sort all papers  2: sort only PDB primary citations
	 * @param pageRankOrCiteCnt: 1:use pageRank  2: use CiteCount
	 * @param top100orOutliers: 1:top100  2: outliers within top100
	 * @throws Exception
	 */
	private static void sortAndFindTop100PapersOrPDBIds(int paperOrPDB, int pageRankOrCiteCnt, int top100orOutliers, String networkPath, String networkMetricsPath, String pdbIdPubMedIdPath) throws Exception {
		
		System.out.println("Start Time: " + dateFormat.format(new Date()));
		String line;

		Map<Integer, String> pubmed_id_map = new HashMap<Integer, String>(); //unique pubMed Id's (key: pubMedId value: count)		
    	BufferedReader reader3 = Files.newBufferedReader(Paths.get(pdbIdPubMedIdPath), ENCODING);
    	line = null;
		while ((line = reader3.readLine()) != null) {			
			//skip the header line and any empty lines
			if (line.trim().startsWith("PDB") || line.trim().equals(""))
				continue;
			
			String[] tokens  = line.split(","); 
		    String pdb_id    = tokens[0].replaceAll("\"", "").trim();		    
		    String s_pubmed_id =  tokens[1].replaceAll("\"", "").trim();
		    
		    if (s_pubmed_id.length() <= 0) // if the primary citation PubMed id is empty, continue
		    	continue;
		    
		    Integer pubmed_id = Integer.valueOf(s_pubmed_id);
	    	if (pubmed_id_map.containsKey(pubmed_id)){
		   		String currentValue = pubmed_id_map.get(pubmed_id);
		   		pubmed_id_map.put(pubmed_id, currentValue+"|"+pdb_id);	    		
		   	}else{	    		
		   		pubmed_id_map.put(pubmed_id, pdb_id);
		   	}	
		}	
				
		List<NodeSort> nodeList = new ArrayList<NodeSort>();
		List<NodeSort> nodeListPDB = new ArrayList<NodeSort>();

		Map<Integer, String> paperNetworkMap = new HashMap<Integer, String>();
	    BufferedReader reader2 = Files.newBufferedReader(Paths.get(networkPath), ENCODING);
		line = null;  
	    while ((line = reader2.readLine()) != null) {	    	
	    	line = line.trim();
	    	if (line.equals("")) //skip the empty lines, if there is any
	    		continue;	    	
	    	if (line.length() > 9 && line.substring(0, 9).equalsIgnoreCase("*Vertices"))
	    		continue;	    	
	    	if (line.length() > 6 && ( line.substring(0, 6).equalsIgnoreCase("*Edges") || line.substring(0, 5).equalsIgnoreCase("*Arcs")))
	    		break;
	    	
    	    int nameStart = line.indexOf("\"");
    	    int nameEnd = line.lastIndexOf("\"");
    	    
    	    Integer id = Integer.valueOf(line.substring(0, nameStart-1));
    	    String name = line.substring(nameStart+1, nameEnd);
    	    paperNetworkMap.put(id, name);
	    }	    
	    System.out.println("Network size: " + paperNetworkMap.size());
	    
		//Read the network metrics file, list the top 10 by pageRank
	    BufferedReader reader = Files.newBufferedReader(Paths.get(networkMetricsPath), ENCODING);
		line = null; 
	    while ((line = reader.readLine()) != null) {
	    	//skip the header line and any empty lines
	    	if (line.startsWith("*") || line.trim().equals("")) //first line or last line
	    		continue;
	    	
			String[] tokens  = line.split(Pattern.quote("||")); 
			NodeSort nSort = new NodeSort(Integer.valueOf(tokens[0].trim()), Integer.valueOf(tokens[1].trim()), Double.valueOf(tokens[3].trim()));
			nodeList.add(nSort);
			
			Integer id = Integer.valueOf(tokens[0].trim());
			Integer pubmed_id = Integer.valueOf(paperNetworkMap.get(id).split(Pattern.quote("||"))[1].trim());
			
			if (pubmed_id_map.containsKey(pubmed_id)){
				NodeSort nSort2 = new NodeSort(Integer.valueOf(tokens[0].trim()), Integer.valueOf(tokens[1].trim()), Double.valueOf(tokens[3].trim()));
				nSort2.name = pubmed_id_map.get(pubmed_id);
				nodeListPDB.add(nSort2);				
			}
	    }

	    if (paperOrPDB == 1){//sort papers
	    
		    Collections.sort(nodeList, NodeSort.COMPARE_BY_CITECNT);
		    for (int i = 0; i < nodeList.size(); i++)
		    	nodeList.get(i).citeCntOrder = i+1;
		    
		    Collections.sort(nodeList, NodeSort.COMPARE_BY_PAGERANK);
		    for (int i = 0; i < nodeList.size(); i++)
		    	nodeList.get(i).pageRankOrder = i+1;
	
		    if (pageRankOrCiteCnt == 1)
		    	Collections.sort(nodeList, NodeSort.COMPARE_BY_PAGERANK);
		    else if (pageRankOrCiteCnt == 2)
		    	Collections.sort(nodeList, NodeSort.COMPARE_BY_CITECNT);
		    	
		    Collections.sort(nodeList, NodeSort.COMPARE_BY_PAGERANK);	    	
		    for (int i = 0; i < 100; i++){
		    	System.out.println("pageRankOrder | pageRankValue | citeCountOrder | citeCount | paperId | paperTitleIdYearJournal");
		    	if (top100orOutliers == 2 && nodeList.get(i).citeCntOrder / nodeList.get(i).pageRankOrder >= 5) //if outliers
		    		System.out.println(nodeList.get(i).pageRankOrder + "|" +nodeList.get(i).pageRank + "|" + nodeList.get(i).citeCntOrder + "|" + nodeList.get(i).citeCnt + "|" + nodeList.get(i).id + "|" + paperNetworkMap.get(nodeList.get(i).id) );
		    	else
		    		System.out.println(nodeList.get(i).pageRankOrder + "|" +nodeList.get(i).pageRank + "|" + nodeList.get(i).citeCntOrder + "|" + nodeList.get(i).citeCnt + "|" + nodeList.get(i).id + "|" + paperNetworkMap.get(nodeList.get(i).id) );
		    }
		    
	    }else if (paperOrPDB == 2){ //sort PDB

		    Collections.sort(nodeListPDB, NodeSort.COMPARE_BY_CITECNT);
		    for (int i = 0; i < nodeListPDB.size(); i++)
		    	nodeListPDB.get(i).citeCntOrder = i+1;
		    
		    Collections.sort(nodeListPDB, NodeSort.COMPARE_BY_PAGERANK);
		    for (int i = 0; i < nodeListPDB.size(); i++)
		    	nodeListPDB.get(i).pageRankOrder = i+1;
	
		    if (pageRankOrCiteCnt == 1)
		    	Collections.sort(nodeListPDB, NodeSort.COMPARE_BY_PAGERANK);
		    else if (pageRankOrCiteCnt == 2)
		    	Collections.sort(nodeListPDB, NodeSort.COMPARE_BY_CITECNT);
	
		    for (int i = 0; i < 100; i++){
		    	System.out.println("pageRankOrder | pageRankValue | citeCountOrder | citeCount | PDB id | paperTitleIdYearJournal");
		    	if (top100orOutliers == 2 && nodeListPDB.get(i).citeCntOrder / nodeListPDB.get(i).pageRankOrder >= 5) //list outliers
		    		System.out.println(nodeListPDB.get(i).pageRankOrder + "|" + nodeListPDB.get(i).pageRank + "|" + nodeListPDB.get(i).citeCntOrder + "|" + nodeListPDB.get(i).citeCnt + "|" + nodeListPDB.get(i).name + "|"+paperNetworkMap.get(nodeListPDB.get(i).id));
		    	else
		    		System.out.println(nodeListPDB.get(i).pageRankOrder + "|" + nodeListPDB.get(i).pageRank + "|" + nodeListPDB.get(i).citeCntOrder + "|" + nodeListPDB.get(i).citeCnt + "|" + nodeListPDB.get(i).name + "|"+paperNetworkMap.get(nodeListPDB.get(i).id));		    		
		    }
	    }

	 /*   DrugTargetCorrelation drug =new DrugTargetCorrelation();
	    Map<String, Map<String, Integer>> pdb_drug_map = drug.readPDBDrugTargetCSVFile();
	    
	    Map<String, String> pdb_drug_map_temp = new HashMap<String, String>();
    	for (Iterator<Map.Entry<String, Map<String, Integer>>> iter = pdb_drug_map.entrySet().iterator(); iter.hasNext(); ) 	{
    		Map.Entry<String, Map<String, Integer>> entry = iter.next();
    		pdb_drug_map_temp.put(entry.getKey(), "");
    	}	    
	   */ 
	    
	//    	if (nodeListPDB.get(i).citeCntOrder / nodeListPDB.get(i).pageRankOrder >= 5)
//	    for (int i = 0; i < nodeListPDB.size(); i++){
	    	//String[] tokens = nodeListPDB.get(i).name.split(Pattern.quote("|"));
	    	//for (int j=0; j<tokens.length; j++){
	//    		if (pdb_drug_map.containsKey(tokens[j])){            // nodeListPDB.get(i).name+"|"+     + "|" + nodeListPDB.get(i).name 
//	    			System.out.println(pdb_drug_map.get(tokens[j]) + "|" + tokens[j] + "|" + nodeListPDB.get(i).pageRankOrder + "|" +nodeListPDB.get(i).pageRank + "|" + nodeListPDB.get(i).citeCntOrder + "|" + nodeListPDB.get(i).citeCnt + "|" + nodeListPDB.get(i).id + "|" + nodeListPDB.get(i).name + "|"+ paperNetworkMap.get(nodeListPDB.get(i).id) );     
	    //			pdb_drug_map_temp.put(tokens[j], nodeListPDB.get(i).pageRankOrder + "|" +nodeListPDB.get(i).pageRank + "|" + nodeListPDB.get(i).citeCntOrder + "|" + nodeListPDB.get(i).citeCnt + "|" + nodeListPDB.get(i).id + "|"+ paperNetworkMap.get(nodeListPDB.get(i).id) );
	  //  		}
	    //	}	    
	    
/*    	for (Iterator<Map.Entry<String, Map<String, Integer>>> iter = pdb_drug_map.entrySet().iterator(); iter.hasNext(); ) 	{
    		Map.Entry<String, Map<String, Integer>> entry = iter.next();
    		System.out.println(entry.getValue() + "|" + entry.getKey() + "|" + pdb_drug_map_temp.get(entry.getKey()));
    	}*/	    
		System.out.println("End Time: " + dateFormat.format(new Date()) );        
		
	}
	
	/**
	 * The classic PageRank damping factor(0.15) unfairly favors the old papers in paper citation networks. See paper (Finding scientific gems with Google’s PageRank algorithm, 2007, Chen et al.)
	 * Because the notion of centrality is elusive and defies any attempt of generalization. Therefore we generate a chart for different values of PageRank DampingFactor and try to estimate the 
	 * best value of Damping Factor for our paper citation networks, empirically. 
	 * @throws IOException 
	 */
	private static void generateChartForDumpingFactor() throws IOException {

		//Read 
		Map<Integer, String> id_year_map = new HashMap<Integer, String>(); // (key: nodeId value: year)
	    BufferedReader reader2 = Files.newBufferedReader(Paths.get("/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network.net"), ENCODING);
		String line2 = null; 
	    while ((line2 = reader2.readLine()) != null) {
	    	if (line2.startsWith("*Arcs"))
    			break;	    		
	    	//skip the header line and any empty lines
	    	if (line2.startsWith("*") || line2.trim().equals("")) //first line or last line
	    		continue;

	    	Integer id = Integer.valueOf(line2.substring(0, line2.indexOf(" ")).trim());
	    	String[] tokens  = line2.split(Pattern.quote("||")); 
	    	String pubYear = tokens[2].trim();
	    	id_year_map.put(id, pubYear);
	    }
		
		
		//Map<String, Integer> citeSort_map = new HashMap<String, Integer>(); // (key: nodeId value: count)
		Map<String, YearCitations> years_map = new HashMap<String, YearCitations>(); // (key: nodeId value: count)

		double totalCiteCount = 0; 
		double totalPageRank = 0.0;
		double numOfPapers = 0; 
		int nocite = 0; int noRef = 0; int noCitenoRef=0;
		//Step 1: Read the networkMetrics file
	    BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/ali/Documents/BioCaddie/data/citation/april_29/cites_refs/paper_citation_network_metrics(d=0.7).txt"), ENCODING);
		String line = null; 
	    while ((line = reader.readLine()) != null) {
	    	//skip the header line and any empty lines
	    	if (line.startsWith("*") || line.trim().equals("")) //first line or last line
	    		continue;
	    		    	
			String[] tokens  = line.split(Pattern.quote("||")); 
			Integer nodeId   = Integer.valueOf(tokens[0].trim());
		    Double citeCount  = Double.valueOf(tokens[1].trim());
		 //   Double refCount  = Double.valueOf(tokens[2].trim());
		    Double pageRank = Math.abs(Double.valueOf(tokens[3].trim()) * 11431109.0 - 1.0);
		   
		 /*   if (citeCount <= 0.0){
		    	nocite++;
		    	continue;
		    }
		   */ 
		    String pubYear  = id_year_map.get(nodeId);
		    
		    //if (citeCount  0.0)	;
		  //  if (refCount == 0.0)	noRef++;
		//    if (citeCount == 0.0 && refCount == 0.0)	noCitenoRef++;
	    	//if (Integer.valueOf(pubYear) > 2015 || Integer.valueOf(pubYear) < 1950)
	    	//	continue;
		    
		    numOfPapers++;
		    totalCiteCount += citeCount;  
		    totalPageRank += pageRank;
		    
		    if (years_map.containsKey(pubYear)){  	
		    	YearCitations yearCitations = years_map.get(pubYear);
		    	yearCitations.totalCitationCount += citeCount ;
		    	yearCitations.totalPageRank += pageRank;
		    	yearCitations.numOfPapers++;
		    }else{		    	
		    	years_map.put(pubYear, new YearCitations(pubYear, citeCount, pageRank, 1.0, 0.0));
		    }		    
	    }
	    
    	System.out.println("nocite: "+ nocite);
    	System.out.println("numOfPapers: "+ numOfPapers);
    	System.out.println("totalCiteCount: "+ totalCiteCount);
    	System.out.println("totalPageRank: "+ totalPageRank);
	    double averageCiteCount = totalCiteCount / numOfPapers;
    	System.out.println("averageCiteCount: "+ averageCiteCount);

	    Map<String, YearCitations> sorted_years_map = new TreeMap<String, YearCitations>(years_map);
	    years_map = sorted_years_map;
	    double numOfYears = years_map.size();

	    for (Iterator<Map.Entry<String, YearCitations>> iter = years_map.entrySet().iterator(); iter.hasNext(); ) {	    		
	    	YearCitations yearCitations = iter.next().getValue();

	    	//System.out.println(yearCitations.year + "|" + yearCitations.totalCitationCount + "|"+ yearCitations.numOfPapers + "|"+ yearCitations.totalPageRank);
	    	System.out.println(yearCitations.year +"|" +yearCitations.numOfPapers.intValue() + "|"+ yearCitations.totalCitationCount.intValue() + "|" + (yearCitations.totalCitationCount / yearCitations.numOfPapers)/averageCiteCount +"|"+ yearCitations.totalPageRank /yearCitations.numOfPapers );
	    }
	    
	}
}

class YearCitations {	
	String year; 
	Double totalCitationCount;
	Double totalPageRank;
	Double numOfPapers;
	Double numOfDanglingPapers;	
	   
	YearCitations(){ }
	YearCitations(String p_year, Double p_totalCitationCount, Double p_totalPageRank, Double p_numOfPapers, Double p_numOfDanglingPapers){
		year = p_year;
		totalCitationCount = p_totalCitationCount;
		totalPageRank = p_totalPageRank;
		numOfPapers = p_numOfPapers;
		numOfDanglingPapers = p_numOfDanglingPapers;		
    }
}

class NodeSort{	
	Integer id;

	String name;

	Integer citeCnt;
	Integer citeCntOrder;

	Double pageRank;
	Integer pageRankOrder;

	NodeSort(){}
	NodeSort(Integer p_id, Integer p_citeCnt, Double p_pageRank){
		id = p_id;
		citeCnt = p_citeCnt;
		pageRank = p_pageRank;
	}

    public static Comparator<NodeSort> COMPARE_BY_CITECNT = new Comparator<NodeSort>() {
        public int compare(NodeSort one, NodeSort other) {
            return other.citeCnt.compareTo(one.citeCnt);
        }
    };	
    public static Comparator<NodeSort> COMPARE_BY_PAGERANK = new Comparator<NodeSort>() {
        public int compare(NodeSort one, NodeSort other) {
            return other.pageRank.compareTo(one.pageRank);
        }
    };	    
}
