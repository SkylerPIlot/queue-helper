package com.queuehelper;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 This class can only have a single instance of itself and is called/constructed by a Queue object to handle all Http calls
 from the client
 */

public class BASHTTPClient implements QueueHelperHTTPClient
{
	private static BASHTTPClient client;

	private String apikey;

	private static final String HOST_PATH = "vrqgs27251.execute-api.eu-west-2.amazonaws.com";

	private HttpUrl apiBase;

	private OkHttpClient Basclient;

	//TODO Deobfuscate
	private String CustIDQuery;
	private String RetrieveCSVQuery;
	private String UPDATE_OPTION_GNC;
	private String UPDATE_OPTION_ATQ;
	private String UPDATE_OPTION_PRI;
	private String UPDATE_OPTION_NAM;
	private String UPDATE_OPTION_FORMI;
	private String UPDATE_OPTION_QN;
	private String UPDATE_OPTION_D;
	private String UPDATE_OPTION_QHN;
	private String UPDATE_OPTION_C;
	private String UPDATE_OPTION_M;
	private String UPDATE_OPTION_R;
	private String OptionQuery;
	private String CustomerNameQuery;
	private String basephp;

	private BASHTTPClient(String apikey) throws IOException
	{
		this.Basclient = new OkHttpClient();
		this.apikey = apikey;
		this.apiBase = new HttpUrl.Builder().scheme("https").host(BASHTTPClient.HOST_PATH).addPathSegment("Bas_Queuehelper").build();
		String[] pathsArray = this.getFilePaths();
		this.updateFilePaths(pathsArray);
	}

	public static BASHTTPClient getInstance(String apikey) throws IOException
	{
		if(BASHTTPClient.client == null){
			BASHTTPClient.client = new BASHTTPClient(apikey);
		}
		else{
			BASHTTPClient.client.setAPikey(apikey);
		}
		return BASHTTPClient.client;
	}

	public void setAPikey(String apikey)
	{
		this.apikey = apikey;
	}


	public void clearFilePaths(){
		RetrieveCSVQuery = "";

		UPDATE_OPTION_GNC = "";

		UPDATE_OPTION_ATQ = "";

		UPDATE_OPTION_PRI = "";

		UPDATE_OPTION_NAM = "";

		UPDATE_OPTION_FORMI = "";

		UPDATE_OPTION_QN = "";

		UPDATE_OPTION_D = "";

		UPDATE_OPTION_QHN = "";

		UPDATE_OPTION_C = "";

		UPDATE_OPTION_M = "";

		UPDATE_OPTION_R = "";

		OptionQuery = "";

		CustomerNameQuery = "";

		basephp = "";

		CustIDQuery = "";
	}

	private void updateFilePaths(String[] paths){
		this.RetrieveCSVQuery = paths[0];
		this.UPDATE_OPTION_GNC = paths[1];
		this.UPDATE_OPTION_ATQ = paths[2];
		this.UPDATE_OPTION_PRI = paths[3];
		this.UPDATE_OPTION_NAM = paths[4];
		this.UPDATE_OPTION_FORMI = paths[5];
		this.UPDATE_OPTION_QN = paths[6];
		this.UPDATE_OPTION_D = paths[7];
		this.UPDATE_OPTION_QHN = paths[8];
		this.UPDATE_OPTION_C = paths[9];
		this.UPDATE_OPTION_M = paths[10];
		this.UPDATE_OPTION_R = paths[11];
		this.OptionQuery = paths[12];
		this.CustomerNameQuery = paths[13];
		this.basephp = paths[14];
		this.CustIDQuery = paths[15];
	}

	private String[] getFilePaths() throws IOException {

		OkHttpClient client = Basclient;
		HttpUrl url = apiBase.newBuilder()
			.addPathSegment("grabfilestrings")
			.build();

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url)
			.header("Content-Type", "application/json")
			.header("x-api-key", this.apikey)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			return response.body().string().split("\"")[3].split(",");
		}
	}
	@Override
	public String getCustomerID(String name) throws IOException
	{

		OkHttpClient client = Basclient;
		HttpUrl url = apiBase.newBuilder()
			.addPathSegment("bas")
			.addPathSegment(basephp)
			.addQueryParameter(CustIDQuery, name.replace(' ', ' '))
			.build();

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url)
			.header("Content-Type", "application/json")
			.header("x-api-key", this.apikey)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			return response.body().string();
		}

	}
	@Override
	public boolean markCustomer(int option, String name) throws IOException
	{
		OkHttpClient client = Basclient;
		HttpUrl url = apiBase.newBuilder()
			.addPathSegment("bas")
			.addPathSegment(basephp)
			.addQueryParameter(OptionQuery, option + "")
			.addQueryParameter(CustomerNameQuery, Text.removeTags(Text.sanitize(name)).replace(' ', ' '))
			.build();

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url)
			.header("Content-Type", "application/json")
			.header("x-api-key", this.apikey)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			return response.isSuccessful();
		}

	}
	@Override
	public List<String[]> readCSV(List<String[]> csv) throws IOException{

		int PRIORITY = 2;
		int RNAMES = 4;
		int PNAME = 3;
		int STATUS = 0;
		int ID = 1;
		int ITEM = 5;
		int NOTES = 7;
		OkHttpClient client = Basclient;
		HttpUrl url = apiBase.newBuilder()
			.addPathSegment("bas")
			.addPathSegment(basephp)
			.addQueryParameter(RetrieveCSVQuery, "1")
			.build();

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url)
			.header("Content-Type", "application/json")
			.header("x-api-key", this.apikey)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			csv.clear();

			String[] CSVLines = response.body().string().split("\n");
			for(String line : CSVLines ){
				String[] LineItems = line.split(",");
				if(LineItems[0].equals("Current time:") || LineItems[0].equals("Last edited:") || LineItems[0].equals("Status")){
					continue;
				}
				else{
					csv.add(new String[] {LineItems[PRIORITY], LineItems[PRIORITY].equals("R") ? LineItems[RNAMES] : LineItems[PNAME], LineItems[STATUS], LineItems[ID], LineItems[ITEM], LineItems[NOTES]});
				}
			}
			return csv;
		}

	}

	@Override
	public NavigationButton getNavButton()
	{
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panellogo.png");

		NavigationButton navButton = NavigationButton.builder()
			.tooltip("BAS queue + options")
			.icon(icon)
			.priority(10)
			//.panel(BasQueuePanel)
			.build();
		return navButton;
	}


	public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException
	{
		OkHttpClient client = Basclient;
		HttpUrl url = apiBase.newBuilder()
			.addPathSegment("bas")
			.addPathSegment(basephp)
			.addQueryParameter(UPDATE_OPTION_D, urlList.toString())
			.addQueryParameter(UPDATE_OPTION_QHN, Text.sanitize(name))
			.build();

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url)
			.header("Content-Type", "application/json")
			.header("x-api-key", this.apikey)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			return response.isSuccessful();
		}
	}



}
