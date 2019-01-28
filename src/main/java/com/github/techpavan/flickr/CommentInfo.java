/*
 * Copyright (c) 2019.
 * This code is released under The 3-Clause BSD License.
 * https://github.com/techpavan
 */

package com.github.techpavan.flickr;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CommentInfo {
    private String id;

    private String authorName;

    private Date dateCreate;

    private String text;

    public String toHtml() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class='row'>");
        stringBuilder.append("<div class='cell' data-title='description'>" + this.getId() + "</div>");
        stringBuilder.append("<div class='cell' data-title='dateUpdate'>" + this.getAuthorName() + "</div>");
        stringBuilder.append("<div class='cell' data-title='photoCount'>" + this.getDateCreate() + "</div>");
        stringBuilder.append("<div class='cell' data-title='videoCount'>" + this.getText() + "</div>");
        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }
}
