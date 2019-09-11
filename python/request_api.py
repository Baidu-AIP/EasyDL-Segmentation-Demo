# encoding:utf-8
import urllib2
import base64
import json

import pycocotools.mask as mask_util
import cv2
import numpy as np

'''
easydl图像分割
'''

request_url=""
img_path = ""
with open(img_path, 'r') as f:
    img_str = f.read()

img_str = base64.b64encode(img_str)
params = "{\"image\":\"" + img_str + "\"}"

# access_token = '[调用鉴权接口获取的token]'
access_token = ''
request_url = request_url + "?access_token=" + access_token
request = urllib2.Request(url=request_url, data=params)
request.add_header('Content-Type', 'application/json')
response = urllib2.urlopen(request)
content = response.read()

results = json.loads(content)['results']

ori_img = cv2.imread(img_path).astype(np.float32)
height, width = ori_img.shape[:2]

alpha = 0.5
for item in results:
    # Draw bbox
    x1 = int(item["location"]["left"])
    y1 = int(item["location"]["top"])
    w = int(item["location"]["width"])
    h = int(item["location"]["height"])
    x2 = x1 + w
    y2 = y1 + h

    cv2.rectangle(ori_img, (x1, y1), (x2, y2), (0,255,0), 2)
    cv2.putText(ori_img, "{} score: {}".format(item["name"], round(float(item["score"]),4)), (x1, y1 - 10), cv2.FONT_HERSHEY_PLAIN, 0.7, (255, 255, 255), 1)


    # Draw mask
    rle_obj = {"counts": item['mask'],
               "size": [height, width]}
    mask = mask_util.decode(rle_obj)


    new_rle_obj = mask_util.encode(mask)

    random_color = np.array([np.random.random()* 255.0,
                             np.random.random()* 255.0,
                             np.random.random()* 255.0])

    idx = np.nonzero(mask)

    ori_img[idx[0], idx[1], :] *= 1.0 - alpha
    ori_img[idx[0], idx[1], :] += alpha * random_color

ori_img = ori_img.astype(np.uint8)
cv2.imshow("img", ori_img)
cv2.waitKey()
