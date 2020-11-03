package com.example.booker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Book class for storing data and metadata of books.
 * Firestore can insert the data from a book document directly into one of these.
 */
public class Book {
	private String title;
	private String description;
	private String status;
	private String ISBN;
	private String author;
	private String ownerUsername;
	private String UID;
	private List<String> requesterList;

	// Constructor for Firestore's .toObject()
	public Book() {}

	public Book(String title, String description, String status, String ISBN, String author) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.ISBN = ISBN;
		this.author = author;
		this.requesterList = Arrays.asList("");
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getISBN() {
		return ISBN;
	}
	
	public void setISBN(String ISBN) {
		this.ISBN = ISBN;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public String getOwnerUsername() {
		return ownerUsername;
	}

	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}

	public String getUID() {
		return UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}

	// update this when adding new attributes
	public HashMap<String, Object> getDataHashMap() {
		HashMap<String, Object> data = new HashMap<>();
		data.put("title", title);
		data.put("description", description);
		data.put("status", status);
		data.put("ISBN", ISBN);
		data.put("author", author);
		data.put("ownerUsername", ownerUsername);
		data.put("UID", UID);
		data.put("requesterList", requesterList);
		return data;
	}

	public List<String> getRequesterList() {
		return requesterList;
	}

	public void setRequesterArray(List<String> requesterList) {
		this.requesterList = requesterList;
	}

	public void addRequester(String requesterUsername) {
		requesterList.add(requesterUsername);
	}

	public boolean containsRequester(String requesterUsername) {
		return requesterList.contains(requesterUsername);
	}
}
