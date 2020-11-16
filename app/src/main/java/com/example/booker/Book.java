package com.example.booker;

import android.net.Uri;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Book class for storing data and metadata of books.
 * Firestore can insert the data from a book document directly into one of these.
 */
public class Book implements Serializable {
	private String title;
	private String description;
	private String status;
	private String ISBN;
	private String author;
	private String ownerUsername;
	private String ownerEmail;
	private String UID;
	private List<String> requesterList;
	private String imageURI;

	/**
	 * Constructor for Firestore's .toObject()
	 */
	public Book() {}

	/**
	 * constructor for set parameters
	 * @param title
	 * @param description
	 * @param status
	 * @param ISBN
	 * @param author
	 */
	public Book(String title, String description, String status, String ISBN, String author) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.ISBN = ISBN;
		this.author = author;
		this.requesterList = Arrays.asList(); // allows a user to be the 0th index instead of an empty string
	}

	/**
	 * gets a book's title
	 * @return book's title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * gets a book's owner email
	 * @return book's owner email
	 */
	public String getOwnerEmail() {
		return ownerEmail;
	}

	/**
	 * sets a book's owner email
	 * @param ownerEmail
	 */
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public String getImageURI() {
		return imageURI;
	}

	public void setImageURI(String imageURI) {
		this.imageURI = imageURI;
	}

	/**
	 * sets a book's title
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * gets a book's description
	 * @return book's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * sets a book's description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * gets a book's status
	 * @return book's status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * sets a book's status
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * gets a book's ISBN
	 * @return book's ISBN
	 */
	public String getISBN() {
		return ISBN;
	}

	/**
	 * sets a book's ISBN
	 * @param ISBN
	 */
	public void setISBN(String ISBN) {
		this.ISBN = ISBN;
	}

	/**
	 * gets a book's author
	 * @return book's author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * sets a book's author
	 * @param author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * gets a book's owner username
	 * @return book's owner username
	 */
	public String getOwnerUsername() {
		return ownerUsername;
	}

	/**
	 * sets a book's owner username
	 * @param ownerUsername
	 */
	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}

	/**
	 * gets a book's UID
	 * @return book's UID
	 */
	public String getUID() {
		return UID;
	}

	/**
	 * sets a book's UID
	 * @param UID
	 */
	public void setUID(String UID) {
		this.UID = UID;
	}

	/**
	 * Stores book's data in a compact data structure
	 * @return data in hashmap
	 */
	public HashMap<String, Object> getDataHashMap() {
		HashMap<String, Object> data = new HashMap<>();
		data.put("title", title);
		data.put("description", description);
		data.put("status", status);
		data.put("ISBN", ISBN);
		data.put("author", author);
		data.put("ownerUsername", ownerUsername);
		data.put("ownerEmail", ownerEmail);
		data.put("UID", UID);
		data.put("requesterList", requesterList);
		data.put("imageURI", imageURI);
		return data;
	}

	/**
	 * Get book's requester list
	 * @return book's requester list
	 */
	public List<String> getRequesterList() {
		return requesterList;
	}

	public void setRequesterArray(List<String> requesterList) {
		this.requesterList = requesterList;
	}

	/**
	 * add a requester to the requester list
	 * @param requesterUsername
	 */
	public void addRequester(String requesterUsername) {
		requesterList.add(requesterUsername);
	}

	/**
	 * checks if a book contains a requester
	 * @param requesterUsername
	 * @return boolean based on if requester exists in list
	 */
	public boolean containsRequester(String requesterUsername) {
		return requesterList.contains(requesterUsername);
	}

	/**
	 * get number of requesters
	 * @return number of requesters
	 */
	public int numRequesters() {
		return requesterList.size();
	}

	/**
	 * remove a requester from requester list
	 * @param requesterUsername
	 */
	public void removeRequester(String requesterUsername) {
		if (containsRequester(requesterUsername)) {
			requesterList.remove(requesterUsername);
		}
	}

	/**
	 * deletes all requesters except for one
	 * @param requesterUsername
	 */
	public void leaveOneRequester(String requesterUsername) {
		if (containsRequester(requesterUsername)) {
			requesterList.clear();
			requesterList.add(requesterUsername);
		}
	}

}