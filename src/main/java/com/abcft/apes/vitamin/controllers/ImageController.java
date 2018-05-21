package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.FileUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.glassfish.jersey.media.multipart.*;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

/**
 * Created by zhyzhu on 17-4-25.
 */
@Path("/")
public class ImageController extends BaseController{
    private static Logger logger = Logger.getLogger(ImageController.class);

    /**
     * 上传图片
     * @return
     */
    @Path("v1/image/upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject uploadImage(
             FormDataMultiPart form
    ) throws Exception
    {
        for (FormDataBodyPart part : form.getFields("file")) {
            FormDataContentDisposition file = part.getFormDataContentDisposition();
            String imgFormat = part.getMediaType().toString().split("/")[1];
            logger.info("imgFormat:"+imgFormat);
            if(imgFormat.equals("png") || imgFormat.equals("jpeg")
                    || imgFormat.equals("jpg")) {
                InputStream inputStream = part.getEntityAs(InputStream.class);

                String imgUrl = FileUtil.uploadImage(inputStream, imgFormat);
                if (StringUtils.isEmpty(imgUrl)) {
                    logger.error("upload image error");
                }
                Document res = new Document()
                        .append("url", imgUrl);

                return getResponse(true, res);
            }else {
                logger.error("image format error");
            }
        }

        return getResponse(false);
    }

    /**
     * 获取图片
     * @param name
     * @return
     */
    @GET
    @Path("v1/images/{name: [a-zA-Z_0-9\\.-]+}")
    @Produces({"image/png", "image/jpeg", "image/gif"})
    public Response image(
        @PathParam("name") String name
    ) {
        File outDir = FileUtil.getImageDirectory();
        File image = new File(outDir, name);
        if (image.exists()) {
            return Response.ok(image).build();
        } else {
            logger.error(image.getAbsolutePath() + " not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
