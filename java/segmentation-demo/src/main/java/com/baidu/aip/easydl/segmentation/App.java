package com.baidu.aip.easydl.segmentation;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.Iterator;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.baidu.aip.easydl.segmentation.common.ConnUtil;
import com.baidu.aip.easydl.segmentation.common.TokenHolder;
import com.baidu.aip.easydl.segmentation.common.DemoException;

import org.json.JSONObject;

public class App {
    // 填写的管理控制台中安全认证的 Access Key 如 "xcbSVCvCyul5************"
    // 若已申请了 TOKEN 则不必填写
    private final String ACCESS_KEY = "...";

    // 填写的管理控制台中安全认证的 Secret Key 如 "eoekAtqoubpZglD*****************"
    // 若已申请了 TOKEN 则不必填写
    private final String SECRET_KEY = "...";

    // 由 ACCESS_KEY 以及 SECRET_KEY 通过鉴权接口获取的 口令
    // 如 "24.963214f7944a49802466cc47f84c18a4.*******.**********.******-********"
    // 若没有申请 TOKEN，将第一次运行时申请到的 TOKEN 填于此处
    private String TOKEN = null;

    // 线上图像分割模型的接口
    private String MODEL_URL = "https://aip.baidubce.com/...";

    // 需要识别的图像
    private final String INPUT_FILEPATH = "../testcase/horseTest01.jpg";

    // 识别后的新图像
    private final String OUTPUT_FOLDER = "../testcase/horseTest01_result/";
    private final String OUTPUT_FILENAME = "horseTest01";

    private final String SCOPE = "brain_all_scope";

    public static void main(String[] args) throws IOException, DemoException {
        App demo = new App();
        demo.run();
    }

    public void run() throws IOException, DemoException {
        if (TOKEN == null) {
            System.out.println("0. 若 TOKEN 为 null，则使用 ACCESS_KEY, SECRET_KEY 发送请求获取\n");
            TokenHolder holder = new TokenHolder(ACCESS_KEY, SECRET_KEY, SCOPE);
            holder.resfresh();
            TOKEN = holder.getToken();
            System.out.println("获得新 TOKEN： " + TOKEN);
            System.out.println("可直接使用该 TOKEN 与模型接口交互，不需要再重复申请");
            System.out.println("----------");
        }

        System.out.println("1. 读取图片数据\n");

        File imgFile = new File(INPUT_FILEPATH);
        String imageFormat = getImageFormat(imgFile);
        if (imageFormat != "JPEG" && imageFormat != "png") {
            System.out.println("图像格式不支持，该 Demo 仅支持 jpg 与 png");
            return;
        } else if (imageFormat == "JPEG") {
            imageFormat = "jpg";
        }

        BufferedImage bufImg = ImageIO.read(imgFile);
        int width = bufImg.getWidth();
        int height = bufImg.getHeight();
        String imgBase64String = getBase64String(imgFile);
        System.out.println("读取图像： " + INPUT_FILEPATH);
        System.out.println("宽度：" + width + ", " + "高度：" + height);
        System.out.println("类型：" + imageFormat);
        System.out.println("----------");

        System.out.println("2. 向模型 API 发送请求\n");
        String responseString = getModelURLJsonPostResponse(TOKEN, imgBase64String);
        System.out.println("----------");

        System.out.println("3-1. 从回应解析出识别后的各个区域\n");
        SegmentationManager m = new SegmentationManager(responseString, width, height);
        System.out.println(m + "\n");

        for (int i = 0; i < m.getResultsLength(); i++) {
            BufferedImage newBufImg = getBufferedImageCopy(bufImg);

            SegmentResult r = m.getSegmentResult(i);
            System.out.println("  识别结果" + i + ": " + r.toSring());

            r.applyMask(newBufImg, 0.5);

            String OUTPUT_FILEPATH = OUTPUT_FOLDER + OUTPUT_FILENAME + "_segment" + i + "." + imageFormat;
            saveImage(newBufImg, OUTPUT_FILEPATH, imageFormat);
            System.out.println("  写入图像： " + OUTPUT_FILEPATH + "\n");
        }
        System.out.println("----------");

        System.out.println("3-2. 将所有区域应用于原图像\n");
        m.applyResultMasks(bufImg, 0.5);

        String OUTPUT_FILEPATH = OUTPUT_FOLDER + OUTPUT_FILENAME + "_segment_all." + imageFormat;
        saveImage(bufImg, OUTPUT_FILEPATH, imageFormat);
        System.out.println("写入图像： " + OUTPUT_FILEPATH);
        System.out.println("----------");
    }

    private String getModelURLJsonPostResponse(String token, String base64EncodedString)
            throws IOException, DemoException {

        JSONObject params = new JSONObject();
        params.put("image", base64EncodedString);

        String getMaskListURL = MODEL_URL + "?access_token=" + token;
        HttpURLConnection conn = (HttpURLConnection) new URL(getMaskListURL).openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);
        conn.getOutputStream().write(params.toString().getBytes());
        conn.getOutputStream().close();
        String responseString = ConnUtil.getResponseString(conn);

        params.put("image", "base64Encode(getFileContent(FILENAME))");
        System.out.println("url is : " + getMaskListURL + "\n");
        System.out.println("params is :" + params.toString());

        return responseString;
    }

    private byte[] getFileContent(File imgFile) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(imgFile);
            return ConnUtil.getInputStreamContent(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getBase64String(File imgFile) throws IOException {
        byte[] imgBytes = getFileContent(imgFile);
        String encodedString = new String(Base64.getEncoder().encode(imgBytes), StandardCharsets.UTF_8);
        return encodedString;
    }

    private BufferedImage getBufferedImageCopy(BufferedImage bufImg) {
        BufferedImage copy = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), bufImg.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(bufImg, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public void saveImage(BufferedImage bufImg, String outputFilename, String imageFormat) throws IOException {
        File fout = new File(outputFilename);
        ImageIO.write(bufImg, imageFormat, fout);
    }

    public String getImageFormat(File imgFile) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(imgFile);
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        String formatName = null;
        while (imageReaders.hasNext()) {
            ImageReader reader = (ImageReader) imageReaders.next();
            formatName = reader.getFormatName();
        }
        return formatName;
    }
}
