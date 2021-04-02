package com.springprojs.webscraper.services;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.springprojs.webscraper.models.Reviewer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HomePageService {
    private final ChromeDriver driver;
    public static List<Reviewer> DB = new ArrayList<>();
    
    public void scrape(final String url) throws IOException{
        driver.get(url);
        String[] pages = new String[]{};
        
        WebElement reviewsParent = driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul"));
        List<WebElement> reviews = reviewsParent.findElements(By.tagName("li"));
        
        java.util.Iterator<WebElement> i = reviews.iterator();
        int ctr = 0;
        while(i.hasNext()) {
            UUID id = UUID.randomUUID();
            WebElement review = i.next();
            ctr += 1;
            String name = review.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul/li["+ctr+"]/div/div[1]/div/div[1]/div/div/div[2]/div[1]/span/a")).getText();
            String avatar = review.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul/li["+ctr+"]/div/div[1]/div/div[1]/div/div/div[1]/div/div/a/img")).getAttribute("src").toString().replace("60s.jpg", "ls.jpg");
            Hashtable<String, String> emotion = generateEmotion(avatar);
            
            DB.add(new Reviewer(id, name, avatar, emotion)); //Store data in memoryDatabase
        }
        
        try{
            WebElement page_info = driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/div[4]/div[2]/span"));
            pages = page_info.getText().split(" of ");
        } catch (IOError error) {}        

        if (pages.length != 0) {
            while(Integer.parseInt(pages[0]) != Integer.parseInt(pages[1])){
                driver.get(url + "?start=" + (ctr * Integer.parseInt(pages[0])));
                reviewsParent = driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul"));
                reviews = reviewsParent.findElements(By.tagName("li"));
                
                i = reviews.iterator();
                ctr = 0;
                while(i.hasNext()) {
                    UUID id = UUID.randomUUID();
                    WebElement review = i.next();
                    ctr += 1;
                    String name = review.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul/li["+ctr+"]/div/div[1]/div/div[1]/div/div/div[2]/div[1]/span/a")).getText();
                    String avatar = review.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/ul/li["+ctr+"]/div/div[1]/div/div[1]/div/div/div[1]/div/div/a/img")).getAttribute("src").toString().replace("60s.jpg", "ls.jpg");
                    Hashtable<String, String> emotion = generateEmotion(avatar);
                    DB.add(new Reviewer(id, name, avatar, emotion )); //Store data in memoryDatabase
                }

                WebElement page_info = driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[3]/yelp-react-root/div/div[3]/div/div/div[2]/div/div[1]/div[2]/section[2]/div[2]/div/div[4]/div[2]/span"));
                pages = page_info.getText().split(" of ");
            }
        }
        driver.quit();
    }

    public List<Reviewer> getAllReviewers(){
        return DB;
    }

    private Hashtable<String, String> generateEmotion(String avatarPath) throws IOException {
        Hashtable<String, String> emotion = new Hashtable<String, String>();
        List<AnnotateImageRequest> requests = new ArrayList<>();
            
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(avatarPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
        
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    emotion.put("joyLikelihood", "error");
                    emotion.put("sorrowLikelihood", "error");
                    return emotion;
            }
        
            // For full list of available annotations, see http://g.co/cloud/vision/docs
            for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
                emotion.put("joyLikelihood", annotation.getJoyLikelihood().toString());
                emotion.put("sorrowLikelihood", annotation.getSorrowLikelihood().toString());
                }
            }
        }
        return emotion;
    }
}
