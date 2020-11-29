package com.example.booker;

import java.util.Objects;

public class State {
    private BookStatus bookStatus;

    public State() {
        this(BookStatus.AVAILABLE);
    }

    /**
     * @param bookStatus: the book's status
     */

    public State(BookStatus bookStatus) {
        this.bookStatus = bookStatus;
    }

    public BookStatus getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(BookStatus bookStatus) {
        this.bookStatus = bookStatus;
    }

}

