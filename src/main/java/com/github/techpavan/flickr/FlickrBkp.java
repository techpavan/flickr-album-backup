/*
 * Copyright (c) 2019.
 * This code is released under The 3-Clause BSD License.
 * https://github.com/techpavan
 */

package com.github.techpavan.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Extras;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.comments.Comment;
import com.flickr4java.flickr.photos.comments.CommentsInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.photosets.comments.PhotosetsCommentsInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * HTML template source: CSS Table Layout from https://colorlib.com/wp/css3-table-templates/
 */
@Slf4j
public class FlickrBkp {

    static PhotosetsCommentsInterface pscInterface;
    private static CommentsInterface commentsInterface;
    private static String basePath = null;
    private static String apiKey = null;
    private static String sharedSecret = null;
    private static Auth auth;

    private static ExecutorService executorService = Executors.newFixedThreadPool(50);

    public static void main(String[] args) throws FlickrException, IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        parseArgs(scanner, args);

        Flickr f = new Flickr(apiKey, sharedSecret, new REST());

        AuthInterface authInterface = f.getAuthInterface();
        Token token = authInterface.getRequestToken();
        String url = authInterface.getAuthorizationUrl(token, Permission.READ);

        log.info("Auth url: " + url);
        System.out.println("Attempting to open auth url. If browser does not open, access this URL while logged in and provide the code displayed in the format DDD-DDD-DDD.");
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url));
        }

        String tokenKey = scanner.nextLine();
        scanner.close();
        Token requestToken = authInterface.getAccessToken(token, new Verifier(tokenKey));

        auth = authInterface.checkToken(requestToken);
        RequestContext.getRequestContext().setAuth(auth);

        PhotosetsInterface photosetsInterface = f.getPhotosetsInterface();
        log.info("Obtaining album list");
        Photosets photosets = photosetsInterface.getList(auth.getUser().getId());
        List<Album> albumList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(photosets.getPhotosets().size());
        photosets.getPhotosets().stream().forEach(photoset -> populateAlbum(photoset, photosetsInterface, albumList, latch));
        latch.await();
        log.info("Obtained albums: " + albumList.size());

        // album is populated with all photo and video details.

        // collect comments
        pscInterface = f.getPhotosetsCommentsInterface();
        commentsInterface = f.getCommentsInterface();
        albumList.forEach(album -> collectComments(album));
        executorService.shutdown();

        writeCssFile();
        writeIndexFile(albumList, auth.getUser().getRealName() + " (" + auth.getUser().getUsername() + ")");
        writeAlbumFiles(albumList);
    }

    private static void parseArgs(Scanner scanner, String[] args) throws IOException {
        if (StringUtils.isEmpty(args[0])) {
            System.out.println("Execution invalid. Please execute run.bat / run.sh");
            System.exit(1);
        }
        Path reportsPath = Paths.get(args[0], "reports");
        Files.createDirectories(reportsPath);
        basePath = reportsPath.toString();
        log.info("Writing results to path: {}", basePath);

        if (args.length >= 3) {
            apiKey = args[1];
            sharedSecret = args[2];
        }

        if (args.length == 4 && "true".equals(args[3])) {
            Flickr.debugRequest = true;
            Flickr.debugStream = true;
        }

        if (apiKey == null) {
            System.out.println("Please enter API Key: ");
            apiKey = scanner.nextLine();
            System.out.println("\nPlease enter Shared Secret: ");
            sharedSecret = scanner.nextLine();
        }
    }

    private static void writeCssFile() {
        InputStream inputStream = FlickrBkp.class.getResourceAsStream("/style.css");
        String style = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
        writeFile(basePath + "style.css", style);
    }

    private static void writeAlbumFiles(List<Album> albumList) {
        albumList.forEach(album -> {
            StringBuilder stringBuilder = new StringBuilder("<html>");
            stringBuilder.append("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "\t<head>\n" +
                    "\t\t<meta charset='UTF-8'/>\n" +
                    "\t\t<link href='./style.css' media='all' rel='stylesheet' type='text/css'/>\n" +
                    "\t</head>\n" +
                    "\t<body>\n" +
                    "\t\t<h1 style='color: #fff;margin: 0 40px;text-align: center;padding: 20px 40px;'>Album: " + album.getTitle() + "</h1>\n" +
                    "\t\t<div class='wrapper'>\n" +
                    "\t\t\t<div class='table'>\n" +
                    "\t\t\t\t<div class='row header blue'>\n" +
                    "\t\t\t\t\t<div class='cell'>id</div>\n" +
                    "\t\t\t\t\t<div class='cell'>owner</div>\n" +
                    "\t\t\t\t\t<div class='cell'>title</div>\n" +
                    "\t\t\t\t\t<div class='cell'>description</div>\n" +
                    "\t\t\t\t\t<div class='cell'>datePosted</div>\n" +
                    "\t\t\t\t\t<div class='cell'>dateTaken</div>\n" +
                    "\t\t\t\t\t<div class='cell'>lastUpdate</div>\n" +
                    "\t\t\t\t\t<div class='cell'>originalFormat</div>\n" +
                    "\t\t\t\t\t<div class='cell'>mediaType</div>\n" +
                    "\t\t\t\t\t<div class='cell'>originalWidth</div>\n" +
                    "\t\t\t\t\t<div class='cell'>originalHeight</div>\n" +
                    "\t\t\t\t\t<div class='cell'>commentCount</div>\n" +
                    "\t\t\t\t</div>");
            album.getMediaList().forEach(media -> {
                stringBuilder.append(media.toHtml());
                if (media.getCommentCount() > 0) {
                    writeCommentFile("comments_media_" + media.getId() + ".html", "Comments for file: " + media.getTitle(), media.getCommentList());
                }
            });
            stringBuilder.append("</div></div></html>");
            writeFile(basePath + "album_" + album.getId() + ".html", stringBuilder.toString());
            log.debug("Done with album: " + album.getTitle());
        });
    }

    private static void writeFile(String fileName, String content) {
        try {
            FileWriter fw = new FileWriter(new File(fileName));
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            log.error("Error while writing file: {}", fileName, e);
        }
    }

    private static void writeIndexFile(List<Album> albumList, String userName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset='UTF-8'/>\n" +
                "\t\t<link href='./style.css' media='all' rel='stylesheet' type='text/css'/>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<h1 style='color: #fff;margin: 0 40px;text-align: center;padding: 20px 40px;'>List of Albums for: " + userName + "</h1>\n" +
                "\t\t<div class='wrapper'>\n" +
                "\t\t\t<div class='table'>\n" +
                "\t\t\t\t<div class='row header blue'>\n" +
                "\t\t\t\t\t<div class='cell'>Title</div>\n" +
                "\t\t\t\t\t<div class='cell'>Description</div>\n" +
                "\t\t\t\t\t<div class='cell'>DateCreate</div>\n" +
                "\t\t\t\t\t<div class='cell'>DateUpdate</div>\n" +
                "\t\t\t\t\t<div class='cell'>PhotoCount</div>\n" +
                "\t\t\t\t\t<div class='cell'>VideoCount</div>\n" +
                "\t\t\t\t\t<div class='cell'>Id</div>\n" +
                "\t\t\t\t\t<div class='cell'>ViewCount</div>\n" +
                "\t\t\t\t\t<div class='cell'>CommentCount</div>\n" +
                "\t\t\t\t</div>");
        albumList.forEach(album -> {
            stringBuilder.append(album.toHtml());
            if (album.getCommentCount() > 0) {
                writeCommentFile("comments_album_" + album.getId() + ".html", "Comments for album: " + album.getTitle(), album.getCommentList());
            }
        });
        stringBuilder.append("</div></div></body></html>");
        writeFile(basePath + "index.html", stringBuilder.toString());
    }

    private static void writeCommentFile(String fileName, String title, List<CommentInfo> commentList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset='UTF-8'/>\n" +
                "\t\t<link href='./style.css' media='all' rel='stylesheet' type='text/css'/>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<h1 style='color: #fff;margin: 0 40px;text-align: center;padding: 20px 40px;'>" + title + "</h1>\n" +
                "\t\t<div class='wrapper'>\n" +
                "\t\t\t<div class='table'>\n" +
                "\t\t\t\t<div class='row header blue'>\n" +
                "\t\t\t\t\t<div class='cell'>Id</div>\n" +
                "\t\t\t\t\t<div class='cell'>AuthorName</div>\n" +
                "\t\t\t\t\t<div class='cell'>DateCreate</div>\n" +
                "\t\t\t\t\t<div class='cell'>Text</div>\n" +
                "\t\t\t\t</div>");
        commentList.forEach(commentInfo -> stringBuilder.append(commentInfo.toHtml()));
        stringBuilder.append("</div></div></body></html>");
        writeFile(basePath + fileName, stringBuilder.toString());
    }

    private static void collectComments(Album album) {
        try {
            if (album.getCommentCount() != 0) {
                album.getCommentList().addAll(pscInterface.getList(album.getId()).stream()
                        .map(comment -> convertToCommentInfo(comment)).collect(Collectors.toList()));
            }
            CountDownLatch latch = new CountDownLatch(album.getMediaList().size());
            album.getMediaList().forEach(media -> collectMediaComments(media, latch));
            log.debug("Initiated media comment collection for album: {}", album.getTitle());
            latch.await();
            log.debug("Finished media comment collection for album: " + album.getTitle());
        } catch (Exception e) {
            log.debug("Error collecting comments for album: {}", album.getTitle(), e);
        }
    }

    private static void collectMediaComments(Media media, CountDownLatch latch) {
        executorService.execute(() -> {
            try {
                RequestContext.getRequestContext().setAuth(auth);
                media.getCommentList().addAll(commentsInterface.getList(media.getId()).stream()
                        .map(comment -> convertToCommentInfo(comment)).collect(Collectors.toList()));
                media.setCommentCount(media.getCommentList().size());
                log.debug("Collected media comment for: {} ({})", media.getTitle(), media.getId());
            } catch (Exception e) {
                log.error("Error collecting comment for media: {} ({})", media.getTitle(), media.getId(), e);
            } finally {
                latch.countDown();
            }
        });
    }

    private static CommentInfo convertToCommentInfo(Comment comment) {
        return CommentInfo.builder().id(comment.getId())
                .authorName(comment.getAuthorName())
                .dateCreate(comment.getDateCreate())
                .text(comment.getText()).build();
    }

    private static void populateAlbum(Photoset photoset, PhotosetsInterface photosetsInterface, List<Album> albumList, CountDownLatch latch) {
        Album album = Album.builder().title(photoset.getTitle())
                .description(photoset.getDescription())
                .dateCreate(new Date(Long.parseLong(photoset.getDateCreate()) * 1000))
                .dateUpdate(new Date(Long.parseLong(photoset.getDateUpdate()) * 1000))
                .photoCount(photoset.getPhotoCount())
                .videoCount(photoset.getVideoCount())
                .id(photoset.getId())
                .viewCount(photoset.getViewCount())
                .url(photoset.getUrl())
                .commentCount(photoset.getCommentCount())
                .build();
        albumList.add(album);
        log.info("Album info populated: {}", album.getTitle());
        executorService.execute(() -> {
            RequestContext.getRequestContext().setAuth(auth);
            populatePhotos(album, photosetsInterface);
            log.info("Photos in album populated: {}", album.getTitle());
            latch.countDown();
        });
    }

    private static void populatePhotos(Album album, PhotosetsInterface photosetsInterface) {
        int totalCount = album.getPhotoCount() + album.getVideoCount();
        try {
            PhotoList<Photo> photoList = photosetsInterface.getPhotos(album.getId(), Extras.ALL_EXTRAS,
                    Flickr.PRIVACY_LEVEL_NO_FILTER, totalCount, 1);
            photoList.stream().forEach(photo -> album.getMediaList().add(Media.builder()
                    .id(photo.getId())
                    .owner(photo.getOwner().getUsername())
                    .title(photo.getTitle())
                    .description(photo.getDescription())
                    .datePosted(photo.getDatePosted())
                    .dateTaken(photo.getDateTaken())
                    .lastUpdate(photo.getLastUpdate())
                    .url(photo.getUrl())
                    .originalFormat(photo.getOriginalFormat())
                    .mediaType(photo.getMedia())
                    .originalWidth(photo.getOriginalWidth())
                    .originalHeight(photo.getOriginalHeight())
                    .commentCount(photo.getComments())
                    .build()));
        } catch (Exception e) {
            log.error("Error while fetching photos for album: {}", album.getTitle(), e);
        }
    }
}
