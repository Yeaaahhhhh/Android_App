
package com.example.quirky;

import java.util.Date;

/**
 * This is the comment that will be stored to a Database. The Comment will have
 * the content of the comment, the user name, and the time it was made.
 *
 * @author Raymart Bless C. Datuin
 *
 */
public class Comment implements Comparable<Comment>{

    private String content, uname;
    private Date timestamp;

    public Comment(String content, String uname, Date timestamp) {
        this.content = content;
        this.uname = uname;
        this.timestamp = timestamp;

    }

    /**
     * This is used for sorting the Comments by time. So that in an Array
     * They will be sorted by time basis, so Oldest first then youngest.
     * @param compareComment
     * @return 0 if same date. >0 if after the date. <0 if before the date.
     */
    public int compareTo(Comment compareComment) {
        return this.timestamp.compareTo(compareComment.getTimestamp());
    }

    public String getContent() {
        return content;
    }

    public String getUname() {
        return uname;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // Would prolly be good if the user wants to change user name but not
    // needed for this assignment
    public void setUname(String uname) {
        this.uname = uname;
    }
}
