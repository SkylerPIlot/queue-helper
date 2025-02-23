	/*
	 * Copyright (c) 2019, SkylerPIlot <https://github.com/SkylerPIlot>
	 * All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 * 1. Redistributions of source code must retain the above copyright notice, this
	 *    list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
	package com.queuehelper;
	import okhttp3.FormBody;
	import java.util.List;
	import java.util.ArrayList;
	import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
	import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
	import software.amazon.awssdk.services.s3.S3Client;
	import software.amazon.awssdk.services.s3.model.PutObjectRequest;
	import software.amazon.awssdk.services.s3.model.GetObjectRequest;
	import software.amazon.awssdk.services.s3.model.GetObjectResponse;
	import software.amazon.awssdk.core.ResponseInputStream;
	import software.amazon.awssdk.core.sync.RequestBody;
	import software.amazon.awssdk.regions.Region;
	import java.nio.charset.StandardCharsets;
	import java.time.LocalDateTime;
	import java.time.format.DateTimeFormatter;
	import java.io.IOException;
	import java.awt.image.BufferedImage;
	import java.io.IOException;
	import java.util.List;
	import net.runelite.api.FriendsChatRank;
	import net.runelite.api.events.ChatMessage;
	import net.runelite.client.ui.NavigationButton;
	import net.runelite.client.util.ImageUtil;
	import net.runelite.client.util.Text;
	import okhttp3.Call;
	import okhttp3.Callback;
	import okhttp3.HttpUrl;
	import okhttp3.OkHttpClient;
	import okhttp3.Request;
	import okhttp3.Response;
	import java.net.URLEncoder;

	/**
	 This Class handles all IO communication to the backend
	 */

	public class BASHTTPClient implements QueueHelperHTTPClient
	{
		private static BASHTTPClient client;

		private String apikey;

		private static final String HOST_PATH = "vrqgs27251.execute-api.eu-west-2.amazonaws.com";

		private HttpUrl apiBase;

		private OkHttpClient Basclient;

		private AwsBasicCredentials testCredentials;

		private String CustIDQuery;
		private String RetrieveCSVQuery;
		private String UPDATE_OPTION_GNC;
		private String UPDATE_OPTION_ATQ;
		private String UPDATE_OPTION_PRI;
		private String UPDATE_OPTION_NAM;
		private String UPDATE_OPTION_FORMI;
		private String UPDATE_OPTION_QN;
		private String csvList;
		private String UPDATE_OPTION_QHN;
		private String UPDATE_OPTION_C;
		private String UPDATE_OPTION_M;
		private String UPDATE_OPTION_R;
		private String OptionQuery;
		private String CustomerNameQuery;
		private String basephp;



		private BASHTTPClient(String apikey, OkHttpClient basclient) throws IOException
		{
			this.Basclient = basclient;
			this.apikey = apikey;
			this.apiBase = new HttpUrl.Builder().scheme("https").host(BASHTTPClient.HOST_PATH).addPathSegment("Bas_Queuehelper").build();
			String[] pathsArray = this.getFilePaths();
			this.updateFilePaths(pathsArray);
		}

		public static BASHTTPClient getInstance(String apikey,OkHttpClient basclient) throws IOException
		{
			if(BASHTTPClient.client == null){
				BASHTTPClient.client = new BASHTTPClient(apikey, basclient);
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

			csvList = "";

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
			this.csvList = paths[7];
			this.UPDATE_OPTION_QHN = paths[8];
			this.UPDATE_OPTION_C = paths[9];
			this.UPDATE_OPTION_M = paths[10];
			this.UPDATE_OPTION_R = paths[11];
			this.OptionQuery = paths[12];
			this.CustomerNameQuery = paths[13];
			this.basephp = paths[14];
			this.CustIDQuery = paths[15];
			 testCredentials = AwsBasicCredentials.create(
					 RetrieveCSVQuery,
					 UPDATE_OPTION_GNC
			);

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
		@Deprecated
		public String getCustomerID(String name) throws IOException
		{
			return "null";//please use another method to get ID this is not useful anymore

		}
		@Override
		public boolean markCustomer(int option, String name) throws IOException
		{
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("queue")
					.build();

			/*
				3 == offline
				0 == cooldown
				2 == done
				1 == in progress
			}*/


			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("username",name)
					.header("action", String.valueOf(option))
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { response.close(); }
			});
			return true;

		}
		@Override
		public List<String[]> readCSV(List<String[]> csv) throws IOException {
			// Define the bucket and key for the CSV file in the forwarded folder
			String bucketName = "rl2qdatatestcsv";
			String key = "rl2q forwarded data/queue.csv";


			// Create an S3 client to retrieve the object
			try (S3Client s3 = S3Client.builder()
					.region(Region.EU_WEST_2)
					.credentialsProvider(StaticCredentialsProvider.create(testCredentials))
					.build()) {

				GetObjectRequest getObjectRequest = GetObjectRequest.builder()
						.bucket(bucketName)
						.key(key)
						.build();

				try (ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(getObjectRequest)) {
					// Read the CSV content as a String (assumes UTF-8 encoding)
					String csvContent = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);

					// Clear the provided list (which may be used by your side panel update)
					csv.clear();
					String[] CSVLines = csvContent.split("\n");

					// Process each line from the CSV
					for (String line : CSVLines) {
						// Skip header lines (assuming the header starts with "Priority")
						if (line.trim().startsWith("Priority")) {
							continue;
						}

						// Split the line on commas
						String[] LineItems = line.split(",");

						// Make sure we have at least 6 columns (one for each field)
						if (LineItems.length < 6) {
							continue;
						}

						// Trim values to remove any extra whitespace
						String priority = LineItems[0].trim();
						String customerName = LineItems[1].trim();
						String status = LineItems[2].trim();
						String id = LineItems[3].trim();
						String item = LineItems[4].trim();
						String notes = LineItems[5].trim();

						// Add the row to the CSV list that will update the side panel
						csv.add(new String[]{priority, customerName, status, id, item, notes});
					}
					return csv;
				}
			} catch (Exception e) {
				throw new IOException("Error fetching CSV from S3: " + e.getMessage(), e);
			}
		}


		@Override
		public NavigationButton getNavButton()
		{
			final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panellogo.png");

			NavigationButton navButton = NavigationButton.builder()
					.tooltip("BAS queue + options")
					.icon(icon)
					.priority(2)
					//.panel(BasQueuePanel)
					.build();
			return navButton;
		}


		public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException {
			String bucketName = "rl2qdatatestcsv";
			String key = "rl2q received data/" + generateFileName(name); // Save in the correct folder

			// Convert the CSV content to a String
			String csvContent = urlList.toString();

			// Replace non-breaking spaces with regular spaces
			csvContent = csvContent.replace("\u00A0", " ");

			// Remove the #[number] parts if desired (e.g., "Gizzy#1" becomes "Gizzy")
			csvContent = csvContent.replaceAll("#-?\\d+", "");

			// Set your test credentials (only for testing; do not hardcode in production)

			//TODO Credentials go here

			try (S3Client s3 = S3Client.builder()
					.region(Region.EU_WEST_2)
					.credentialsProvider(StaticCredentialsProvider.create(testCredentials))
					.build()) {

				s3.putObject(
						PutObjectRequest.builder()
								.bucket(bucketName)
								.key(key)
								.contentType("text/csv; charset=utf-8")  // Set proper content type and charset
								.build(),
						RequestBody.fromString(csvContent, StandardCharsets.UTF_8)
				);

				System.out.println("File uploaded to S3: " + key);
				return true;
			} catch (Exception e) {
				System.err.println("S3 upload failed: " + e.getMessage());
				return false;
			}
		}

		private String generateFileName(String name) {
			return "queue.csv";
		}

		@Override//TODO make this work(L0l)
		public boolean addCustomer(String itemName, String priority, String custName, String addedBy) throws IOException
		{
			OkHttpClient client = Basclient;

			// Build the URL for your Google Form submission.
			HttpUrl url = new HttpUrl.Builder()
					.scheme("https")
					.host("docs.google.com")
					.addPathSegment("forms")
					.addPathSegment("d")
					.addPathSegment("e")
					// Replace with your actual Google Form ID from the URL:
					// https://docs.google.com/forms/d/e/1FAIpQLSc06_IrTbleP0uZBiOt1yMcI5kvOrvkzgaVLLmEDRLqJSSoVg/viewform
					.addPathSegment("1FAIpQLSc06_IrTbleP0uZBiOt1yMcI5kvOrvkzgaVLLmEDRLqJSSoVg")
					.addPathSegment("formResponse")
					.build();

			// Build the form-encoded request body with the proper entry IDs.
			okhttp3.RequestBody formBody = new FormBody.Builder()
					.add("entry.1481518570", priority)
					.add("entry.1794472797", custName)
					.add("entry.1391010025", itemName)
					.add("entry.1284888696", addedBy)
					.add("entry.1260617128", formsource)
					.build();

			// Create a POST request to the Google Form.
			Request request = new Request.Builder()
					.url(url)
					.post(formBody)
					.header("User-Agent", "RuneLite")
					.build();

			try (Response response = client.newCall(request).execute())
			{
				return response.isSuccessful();
			}
		}
		private static final String formsource = "BASPlugin";

		@Override
		public boolean sendChatMsgDiscord(ChatMessage chatmessage) throws IOException
		{
			String unhashedMsg = chatmessage.getName() + chatmessage.getMessage() + (((int)(chatmessage.getTimestamp()/10)*10));

			int hasedMsg = unhashedMsg.hashCode();
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("disc")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("username",chatmessage.getName().replace(' ', ' '))
					.header("msg",chatmessage.getMessage())
					.header("hash",String.valueOf(hasedMsg))
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { response.close(); }
			});
			return true;
		}

	}
