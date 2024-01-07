package com.team4.memorygame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LoadImagesTask implements Runnable{
    private Context context;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    private String url;
    boolean useGlide;
    boolean useJsoup;
    Handler mHandler = new Handler();
    private ArrayList<String> images;
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    TextView textView;
    TextView helpText;
    Button startButton;
    Thread imageProcess;

    LoadImagesTask(Context context,String url) {
        // Trim trailing slashes in constructor
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.context = context;
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        useGlide = sharedPreferences.getString("glide", "No").equals("Yes");
        useJsoup = sharedPreferences.getString("jsoup", "No").equals("Yes");
    }

    @Override
    public void run() {
        try {
            if (!URLUtil.isValidUrl(url)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Invalid URL, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            startUI();

            if (useJsoup) {
                // Included because it is interesting, in our tests it was found
                // that difference in performance was trivial (~15 ms vs ~60 ms)
                // but jsoup parses more accurately
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select("img[src~=(?i).(png|jpe?g)]");
                images = (ArrayList<String>) elements.stream()
                        .map(e -> e.attr("src").startsWith("http") ? e.attr("src") : url + e.attr("src"))
                        .collect(Collectors.toList());
            } else {
                images = extractAllImgSrcFromUrl(url);
            }

            String fetchPreference = sharedPreferences.getString("fetch", "All");

            // For limiting image parsing depending on user preferences, only limits image parsing not element collection
            // Choice of design based off of ease of implementation and performance bottleneck
            if (! fetchPreference.equals("All") && Integer.parseInt(fetchPreference) < images.size()) {
                // Limits images to be parsed by removing excess images
                images.subList(Integer.parseInt(fetchPreference), images.size()).clear();
            }

            progressBar.setMax(images.size());
            for (int i = 0; i < images.size(); i++) {
                if (Thread.interrupted()) {
                    concludeUI(false);
                    break;
                }

                String sourceAttribute = images.get(i);
                updateUI(i + 1, sourceAttribute);
            }

            concludeUI(true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            concludeUI(false);
        }

    }

    private void startUI() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.getRecycledViewPool().clear();
                recyclerAdapter.clearImages();
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                textView.setText("Starting to process images..");
                textView.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.INVISIBLE);
                helpText.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void updateUI(int progress, String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bmp;
                    if (useGlide) {
                        bmp = Glide.with(context).asBitmap().load(url).submit().get();
                    } else {
                        bmp = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
                    }
                    Bitmap finalBmp = bmp;
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setProgress(progress);
                            textView.setText(String.format(Locale.ENGLISH, "Downloading image %d of %d...", progress, images.size()));
                            recyclerAdapter.addImage(url, finalBmp);
                        }
                    });
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }).run();


    }

    private void concludeUI(boolean success) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    Toast.makeText(context, "Images processed!", Toast.LENGTH_SHORT).show();
                } else {
                    recyclerAdapter.clearImages();
                    Toast.makeText(context, "Image loading failed!", Toast.LENGTH_SHORT).show();
                }

                imageProcess = null;
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                if (success) {
                    startButton.setVisibility(View.VISIBLE);
                    helpText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private ArrayList<String> extractAllImgSrcFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);

        // Act like browser to prevent 403 error response
        URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla");
        urlConnection.setReadTimeout(5000);
        urlConnection.setConnectTimeout(5000);

        // Grab html source
        // Store in string
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        StringBuilder html = new StringBuilder();
        while ((line = in.readLine()) != null) {
            html.append(line);
        }
        in.close();
        // Extract all src attributes
        // Filter those containing .jpg .jpeg .png
        // Append the url prefix for relative image sources
        ArrayList<String> allSrc = new ArrayList<>();
        String relativeImgPrefix = url.getProtocol() + "://" + url.getAuthority();
        Pattern srcTagPattern = Pattern.compile("src=\"(.*?)\"");
        Matcher srcTagMatcher = srcTagPattern.matcher(html);
        while (srcTagMatcher.find()) {
            String srcTag = srcTagMatcher.group(0);
            if (srcTag != null && (srcTag.contains(".jpg") || srcTag.contains(".jpeg") || srcTag.contains(".png"))) {
                String src = srcTag.substring(5, srcTag.length() - 1);
                if (!src.startsWith("http")) {
                    src = relativeImgPrefix + src;
                }
                allSrc.add(src);
            }
        }

        return allSrc;
    }
}
