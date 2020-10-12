package com.baidu.aip.easydl.segmentation;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SegmentResult {

    private final int left;
    private final int top;
    private final int width;
    private final int height;

    private final String maskStr;
    private final String name;
    private final double score;

    private final int imgWidth;
    private final int imgHeight;

    public Boolean[] maskBits;

    public SegmentResult(int left, int top, int width, int height, String maskStr, String name, double score,
            int imgWidth, int imgHeight) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        this.maskStr = new String(maskStr);
        this.name = new String(name);
        this.score = score;

        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;

        this.maskBits = (new MaskDecoder(this.maskStr, this.imgWidth, this.imgHeight)).rleDecode();
    }

    public int getSegmentLeft() {
        return left;
    }

    public int getSegmentTop() {
        return top;
    }

    public int getSegmentWidth() {
        return width;
    }

    public int getSegmentHeight() {
        return height;
    }

    public String getSegmentMask() {
        return maskStr;
    }

    public String getSegmentName() {
        return name;
    }

    public double getSegmentScore() {
        return score;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void applyMask(BufferedImage bufImg, double alpha) {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        applyMask(bufImg, alpha, r, g, b);
    }

    public void applyMask(BufferedImage bufImg, double alpha, int r, int g, int b) {
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (maskBits[i * imgHeight + j] == true) {
                    Color c = new Color(bufImg.getRGB(i, j));
                    int nr = (int) (c.getRed() * (1 - alpha) + r * alpha);
                    int ng = (int) (c.getGreen() * (1 - alpha) + g * alpha);
                    int nb = (int) (c.getBlue() * (1 - alpha) + b * alpha);
                    Color nc = new Color(nr, ng, nb);
                    bufImg.setRGB(i, j, nc.getRGB());
                }
            }
        }
    }

    public String toSring() {
        return "SegmentResult(label=" + name + ", score=" + score + ", location(left=" + left + ", top=" + top
                + ", width=" + width + ", height=" + height + "))";
    }

}
