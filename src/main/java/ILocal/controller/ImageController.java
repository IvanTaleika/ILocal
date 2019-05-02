package ILocal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;

@RequestMapping("/image")
@RestController
public class ImageController {

    @Value("${file.image.path}")
    private String uploadPath;

    @GetMapping
    public void getAvatar(HttpServletResponse response, @RequestParam String href) throws IOException {
        if(href == null || href.equals("")){
            response.sendError(400, "Null href");
            return;
        }
        new File(uploadPath).mkdirs();
        File image = new File("temp/images/" + href);
        if (image.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(image.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + image.getName() + "\""));
            response.setContentLength((int) image.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(image));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            //image.delete();
            inputStream.close();
        } else {
            response.sendError(417);
        }
    }
}
