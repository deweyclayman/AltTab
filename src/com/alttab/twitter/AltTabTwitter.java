package com.alttab.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class AltTabTwitter {
	private Twitter twitter = null;
	private String user = null;
	
	public AltTabTwitter(String user) {
		/*
		 * Created the new Twitter account @AltTabTesting for testing purposes
		 * and also created consumer and access token credentials for use.
		 * Permissions for these credentials have been limited to read-only.
		 */
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("DXdastO8RppPaPFie9zjHFvWs")
		  .setOAuthConsumerSecret("B8tahZ2YhF2cMD6meURbtrdfNMV0mW2e9JYdwF6eCkAVyTI1cQ")
		  .setOAuthAccessToken("785555464322895872-qPRHrSAASm1t2SYEtA8bv1GGdeqwUvG")
		  .setOAuthAccessTokenSecret("7183CECKbk0LFDnRMJgXZ8FZqmHa7Gw6dFoHJt1Lymxxa");
		
		TwitterFactory tf = new TwitterFactory(cb.build());
		
		twitter = tf.getInstance();
		
		this.user = user;
	}

	public List<Status> getAllTweetsPerUser() {
		/*
		 * NOTES: 
		 *     1.) The getUserTimeline() method for the Twitter object can only return about 
		 *         20 tweets by default.
		 *     2.) Therefore resorted to using the Paging object to get ALL tweets and then
		 *         check each tweet for the existence of a non-empty MediaEntities array.
		 *         However, there is a limitation of 3200+/- tweets when using the Paging 
		 *         object.
		 */
		
		System.out.println("\nStart getting all tweets for user " + user +"...");
		
		int pageNumber = 1;
		List<Status> statuses = new LinkedList<Status>();
		
		while (true) {
			try {
				int size = statuses.size(); 
				Paging page = new Paging(pageNumber++, 100);
				
				statuses.addAll(twitter.getUserTimeline(user, page));
				
				if (statuses.size() == size) {
					break;
				}
			} catch(TwitterException e) {
				e.printStackTrace();
			}
		}

		System.out.println("\n...End getting all tweets for user " + user +".");
		
		return statuses;
	}
	
	public List<Status> findAllTweetsWithImages() {
		/*
		 * NOTES: 
		 *     1.) Query object can only return a limit of 100 tweets when the requirement 
		 *         was to get all.
		 *     2.) Using the filter "filter:images" for a Query object returned tweets that 
		 *         technically had images, but the MediaEntities array was empty. 
		 *     3.) Therefore resorted to using the Paging object to get ALL tweets and then
		 *         check each tweet for the existence of a non-empty MediaEntities array.
		 *         However, there is a limitation of 3200+/- tweets when using the Paging 
		 *         object.
		 *     4.) Check the expandedURL of the MediaEntity. If it contains "video" it is a
		 *         video and is ignored, but if it contains "photo" it is an image and is 
		 *         added to the list of tweets to be returned.
		 *     5.) For #4, wanted to check the type attribute of the MediaEntity, but the
		 *         value was photo, even if the tweet contained a video.
		 */
		
		System.out.println("\nStart finding all tweets with images for user " + user +"...");

		int pageNumber = 1;
		List<Status> statuses = new LinkedList<Status>();
		
		while (true) {
			try {
				int size = statuses.size(); 
				Paging page = new Paging(pageNumber++, 100);
				List<Status> timeline = twitter.getUserTimeline(user, page);
				
				int notAddedStatusCt = 0;
				for (Status status : timeline) {
					// check if the tweet has any media entities associated with it
					if (status.getMediaEntities().length > 0) {
						for (MediaEntity mediaEntity : status.getMediaEntities()) {
							// check if any of the media entities is an image
							if (mediaEntity.getExpandedURL().indexOf("photo") != -1) {
								// if media entity is an image, add the tweet to the list
								// no need to check the remaining media entities since at 
								// least one was an image
								statuses.add(status);
								break;
							} else {
								notAddedStatusCt++;
							}
						}
					}
				}
				
				if ((statuses.size() + notAddedStatusCt) == size) {
					break;
				}
			} catch(TwitterException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("\n...End finding all tweets with images for user " + user +".");

		return statuses;
	}
	
	public void saveTweetsToXML(List<Status> statuses) {
		System.out.println("\nStart saving tweets to XML file for user " + user +"...\n");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        
        try {
        	builder = factory.newDocumentBuilder();
        	
            Document xml = builder.newDocument();
            Element root = xml.createElementNS("http://alttab.com/twitter/" + user, "Twitter");
            
            xml.appendChild(root);
 
            for (Status status : statuses) {
                Element tweet = xml.createElement("Tweet");
                
                tweet.appendChild(xml.createTextNode(String.valueOf(status)));
               
                root.appendChild(tweet);
            }
            
            // output XML to file
            String fileName = "twitter-" + user + "_" + new Date().getTime() + ".xml";
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            DOMSource source = new DOMSource(xml);
            File xmlFile = new File(fileName);
            FileOutputStream xmlFileStream = new FileOutputStream(xmlFile, false); 
            StreamResult file = new StreamResult(xmlFileStream);
            transformer.transform(source, file); 
            
            System.out.println("Tweets successfully saved to file " + fileName + " for the user " + user + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n...End saving tweets to XML file for user " + user +".");
    }
	
	public void saveExternalAssets(List<Status> statuses) {
		System.out.println("\nStart saving external assets for user " + user +"...\n");

		for (Status status : statuses) {
			// check if the tweet has any media entities associated with it
			if (status.getMediaEntities().length > 0) {
				// if media entities exist, loop through and save each external asset
				for (MediaEntity mediaEntity : status.getMediaEntities()) {
					String mediaURL = mediaEntity.getMediaURL();
					String type = "photo";
					String fileName = 
							user.toUpperCase() + "-" + mediaURL.substring(mediaURL.lastIndexOf("/") + 1, mediaURL.lastIndexOf("."));
					
					// check if any of the media entity is an image
					if (mediaEntity.getExpandedURL().indexOf("video") != -1) {
						type = "video";
						// file name should have a video extension, but none of the attributes
						// of the media entity has the url for the video. It only references the 
						// image representing the video. Based on this, The image will be downloaded
						// for now and saved as a JPG file.
						fileName += " (" + type + ").jpg";
					} else {
						// photo media entities will be saved as with a JPG extension
						fileName += " (" + type + ").jpg";
					}
					
					System.out.println("Saving " + mediaURL + " (" + type +") as " + fileName + ".");

					Path path = Paths.get(fileName);
					
					try {
						URL url = new URL(mediaURL);
						
						try (InputStream in = url.openStream()) {
						    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch(MalformedURLException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
        System.out.println("\n...End saving tweets to XML file for user " + user +".");
	}
	
	public void showTweets(List<Status> statuses) {
		System.out.println("\nStart showing tweets for user " + user +"...\n");

		int statusCt = 0;
		
		for (Status status : statuses) {
			System.out.println("(" + ++statusCt + ") - " + status);
		}
		
		System.out.println("Total Tweets: " + statusCt);

		System.out.println("\n...End showing tweets for user " + user +".");
	}
}
