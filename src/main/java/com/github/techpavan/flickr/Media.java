/*
 * Copyright (c) 2019.
 * This code is released under The 3-Clause BSD License.
 * https://github.com/techpavan
 */

package com.github.techpavan.flickr;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class Media {

    private String id;
    private String owner;
    private String title;
    private String description;
    private Date datePosted;
    private Date dateTaken;
    private Date lastUpdate;
    private String url;
    private String originalFormat;
    private String mediaType;
    private int originalWidth;
    private int originalHeight;
    private int commentCount;

    @Builder.Default
    private List<CommentInfo> commentList = new ArrayList<>();

    public String toHtml() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class='row'>");
        stringBuilder.append("<div class='cell' data-title='id'><a href='" + this.getUrl() + "'>" + this.getId() + "</a></div>");
        stringBuilder.append("<div class='cell' data-title='owner'>" + this.getOwner() + "</div>");
        stringBuilder.append("<div class='cell' data-title='title'>" + this.getTitle() + "</div>");
        stringBuilder.append("<div class='cell' data-title='description'>" + this.getDescription() + "</div>");
        stringBuilder.append("<div class='cell' data-title='datePosted'>" + this.getDatePosted() + "</div>");
        stringBuilder.append("<div class='cell' data-title='dateTaken'>" + this.getDateTaken() + "</div>");
        stringBuilder.append("<div class='cell' data-title='lastUpdate'>" + this.getLastUpdate() + "</div>");
        stringBuilder.append("<div class='cell' data-title='originalFormat'>" + this.getOriginalFormat() + "</div>");
        stringBuilder.append("<div class='cell' data-title='mediaType'>" + this.getMediaType() + "</div>");
        stringBuilder.append("<div class='cell' data-title='originalWidth'>" + this.getOriginalWidth() + "</div>");
        stringBuilder.append("<div class='cell' data-title='originalHeight'>" + this.getOriginalHeight() + "</div>");
        String commentHtml = this.commentCount > 0 ? "<a href='./comments_media_" + this.getId() + ".html'>" + this.getCommentCount() + "</a>" : "0";
        stringBuilder.append("<div class='cell' data-title='commentCount'>" + commentHtml + "</div>");
        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }
}
