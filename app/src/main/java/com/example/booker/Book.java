package com.example.booker;

/**
 * Book class for storing data and metadata of books.
 * Firestore can insert the data from a book document directly into one of these.
 */
public class Book {
	private String title;
	private String description;
	private String status; // Change this data type if another is more convenient
	private String ISBN;
	
	// Constructor for Firestore's .toObject()
	public Book() {}
	
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
}
