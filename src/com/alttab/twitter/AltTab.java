package com.alttab.twitter;

import java.util.List;

import twitter4j.Status;

public class AltTab {
	public static void main(String args[]) {
		AltTabTwitter altTabTwitter = new AltTabTwitter("google");
		
		/*
		 * Task - to implement [Junior]
		 */

		// Get all tweets per user
		List<Status> allTweetsPerUser = altTabTwitter.getAllTweetsPerUser();
		altTabTwitter.showTweets(allTweetsPerUser);
		
		// Find all tweets with images
		List<Status> allTweetsWithImages = altTabTwitter.findAllTweetsWithImages();
		altTabTwitter.showTweets(allTweetsWithImages);
		
		
		/*
		 * Task - to implement [Senior]
		 */
		
		// Save the fetched tweets in JSON/XML
		altTabTwitter.saveTweetsToXML(allTweetsPerUser);
		
		// Save all associated external assets like image or video
		altTabTwitter.saveExternalAssets(allTweetsPerUser);
	}
}
