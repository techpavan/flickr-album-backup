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
public class Album {

    private String title;
    private String description;
    private Date dateCreate;
    private Date dateUpdate;
    private int photoCount;
    private int videoCount;
    private String id;
    private int viewCount;
    private String url;
    private int commentCount;

    @Builder.Default
    private List<Media> mediaList = new ArrayList<>();

    @Builder.Default
    private List<CommentInfo> commentList = new ArrayList<>();


    public String toHtml() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class='row'>");
        stringBuilder.append("<div class='cell' data-title='title'><a href='./album_" + this.getId() + ".html'>" + this.getTitle() + "</a></div>");
        stringBuilder.append("<div class='cell' data-title='description'>" + this.getDescription() + "</div>");
        stringBuilder.append("<div class='cell' data-title='dateCreate'>" + this.getDateCreate() + "</div>");
        stringBuilder.append("<div class='cell' data-title='dateUpdate'>" + this.getDateUpdate() + "</div>");
        stringBuilder.append("<div class='cell' data-title='photoCount'>" + this.getPhotoCount() + "</div>");
        stringBuilder.append("<div class='cell' data-title='videoCount'>" + this.getVideoCount() + "</div>");
        stringBuilder.append("<div class='cell' data-title='id'><a href='" + this.getUrl() + "'>" + this.getId() + "</a></div>");
        stringBuilder.append("<div class='cell' data-title='viewCount'>" + this.getViewCount() + "</div>");
        String commentHtml = this.commentCount > 0 ? "<a href='./comments_album_" + this.getId() + ".html'>" + this.getCommentCount() + "</a>" : "0";
        stringBuilder.append("<div class='cell' data-title='commentCount'>" + commentHtml + "</div>");
        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }
}
