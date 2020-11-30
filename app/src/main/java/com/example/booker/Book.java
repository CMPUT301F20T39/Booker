package com.example.booker;

import android.net.Uri;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Book class for storing data and metadata of books.
 * Firestore can insert the data from a book document directly into one of these.
 * Description is defined as: title, author, and ISBN
 */
public class Book implements Serializable {
	private String title;
	private String status;
	private String ISBN;
	private String author;
	private String ownerUsername;
	private String ownerEmail;
	private String UID;
	private List<String> requesterList;
	private String imageURI;
	private List<Double> coordinates;
	private boolean scannedByBorrower;
	private boolean scannedByOwner;

	/**
	 * Constructor for Firestore's .toObject()
	 */
	public Book() {}

	/**
	 * constructor for set parameters
	 * @param title
	 * @param status
	 * @param ISBN
	 * @param author
	 */
	public Book(String title, String status, String ISBN, String author) {
		this.title = title;
		this.status = status;
		this.ISBN = ISBN;
		this.author = author;
		this.requesterList = Arrays.asList(); // allows a user to be the 0th index instead of an empty string
		this.coordinates = Arrays.asList(); // no location set yet
		this.scannedByBorrower = false;
		this.scannedByOwner = false;
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

	/**
	 * gets a book's picture identifier (storage location)
	 * @return imageURI
	 */
	public String getImageURI() {
		return imageURI;
	}

	/**
	 * sets a book's picture identifier (storage location)
	 * @return imageURI
	 */
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
		data.put("status", status);
		data.put("ISBN", ISBN);
		data.put("author", author);
		data.put("ownerUsername", ownerUsername);
		data.put("ownerEmail", ownerEmail);
		data.put("UID", UID);
		data.put("requesterList", requesterList);
		data.put("imageURI", imageURI);
		data.put("coordinates", coordinates);
		data.put("scannedByBorrower", scannedByBorrower);
		data.put("scannedByOwner", scannedByOwner);
		return data;
	}

	/**
	 * Get book's requester list
	 * @return book's requester list
	 */
	public List<String> getRequesterList() {
		return requesterList;
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

	/**
	 * gets a list of double that represent coordinates (latitude, longitude)
	 * @return coordinates
	 */
	public List<Double> getCoordinates() {
		return coordinates;
	}

	/**
	 * sets a list of double that represent coordinates (latitude, longitude)
	 * @return coordinates
	 */
	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * gets the book's latitude
	 * @return latitude
	 */
	public Double getLatitude() {
		if (this.hasCoordinates()) {
			return coordinates.get(0);
		}
		return -1.0; // -1 for unsuccessful
	}

	/**
	 * gets the book's longitude
	 * @return longitude
	 */
	public Double getLongitude() {
		if (this.hasCoordinates()) {
			return coordinates.get(1);
		}
		return -1.0; // -1 for unsuccessful
	}

	/**
	 * checks if a book has coordinates
	 * @return true if not empty, false otherwise
	 */
	public boolean hasCoordinates() {
		return !coordinates.isEmpty();
	}

	/**
	 * sets a list of requesters directly (string usernames)
	 * @param requesterList
	 */
	public void setRequesterList(List<String> requesterList) {
		this.requesterList = requesterList;
	}

	/**
	 * checks if a book has been scanned by a borrower
	 * @return true if scanned by borrower, false otherwise
	 */
	public boolean isScannedByBorrower() {
		return scannedByBorrower;
	}

	/**
	 * sets borrower scanned status for a book
	 * @param scannedByBorrower
	 */
	public void setScannedByBorrower(boolean scannedByBorrower) {
		this.scannedByBorrower = scannedByBorrower;
	}

	/**
	 * checks if a book has been scanned by an owner
	 * @return true if scanned by owner, false otherwise
	 */
	public boolean isScannedByOwner() {
		return scannedByOwner;
	}

	/**
	 * sets owner scanned status for a book
	 * @param scannedByOwner
	 */
	public void setScannedByOwner(boolean scannedByOwner) {
		this.scannedByOwner = scannedByOwner;
	}

}