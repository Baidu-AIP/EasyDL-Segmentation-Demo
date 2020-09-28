package com.baidu.aip.easydl.segmentation;

public class MaskDecoder {
    class RLEObject {
        public int height;
        public int width;
        public int m;
        public int[] cnts;
    }

    private RLEObject rle_obj = new RLEObject();

    public MaskDecoder(String mask_str, int height, int width) {
        int m = 0, p = 0, k;
        int x;
        int more;
        m = mask_str.length();
        int[] cnts = new int[m];
        m = 0;
        while (p < mask_str.length()) {
            x = 0;
            k = 0;
            more = 1;
            while (more != 0) {
                char c = (char) (mask_str.charAt(p) - 48);
                x |= (c & 0x1f) << 5 * k;
                more = c & 0x20;
                p++;
                k++;
                if ((more == 0) && ((c & 0x10) != 0))
                    x |= -1 << 5 * k;
            }
            if (m > 2)
                x += (int) cnts[m - 2];
            cnts[m++] = (int) x;
        }
        rleInit(height, width, m, cnts);
    }

    private void rleInit(int h, int w, int m, int[] cnts) {
        rle_obj.height = h;
        rle_obj.width = w;
        rle_obj.m = m;
        if (m == 0) {
            rle_obj.cnts = null;
        } else {
            rle_obj.cnts = new int[m];
        }
        int j;
        if (cnts != null) {
            for (j = 0; j < m; j++) {
                rle_obj.cnts[j] = cnts[j];
            }
        }
    }

    public Boolean[] rleDecode() {
        Boolean[] M = new Boolean[rle_obj.width * rle_obj.height];
        Boolean v = false;
        int j, k;
        int idx = 0;
        for (j = 0; j < rle_obj.m; j++) {
            for (k = 0; k < rle_obj.cnts[j]; k++) {
                M[idx] = v;
                idx++;
            }
            v = !v;
        }
        return M;
    }
}
