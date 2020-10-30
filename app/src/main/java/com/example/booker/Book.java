package com.example.booker;

/**
 * Book class for storing data and metadata of books.
 * Firestore can insert the data from a book document directly into one of these.
 */
public class Book {
	private String title;
	private String description;
	private BookStatus status;  //enum mught be more convenient than string type
	private String ISBN;
	private String author;
	private String ownerUsername;

	// Constructor for Firestore's .toObject()
	public Book() {}

	public Book(String title, String description, BookStatus status, String ISBN, String author) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.ISBN = ISBN;
		this.author = author;
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
	
	public BookStatus getStatus() {
		return status;
	}
	
	public void setStatus(BookStatus status) {
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
}
