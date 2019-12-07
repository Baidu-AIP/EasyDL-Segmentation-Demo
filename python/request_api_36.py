#!/usr/bin/python3
# encoding:utf-8

import urllib.request
import urllib.parse
import base64
import json
import random
import pycocotools.mask as mask_util
import cv2
import numpy as np

'''
easydl图像分割
'''

def random_str(num):
    options = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    salt = ''
    for i in range(num):
        salt += random.choice(options)

    return salt

def get_image_json(img_path):
    with open(img_path, 'rb') as f:
        img_content = f.read()

    img_content = str(base64.b64encode(img_content), encoding='utf-8')
    post_body_params = "{\"image\":\"" + img_content + "\"}"

    return post_body_params

def get_access_token(host):
    response = urllib.request.urlopen(host)
    if response:
        access_token = json.loads(response.read())['access_token']
        # print('get access token successful:', access_token)
    else:
        print('get access token error, please check settings.')
    return access_token

def do_segementation(access_token, post_body_params):

    request_url='https://aip.baidubce.com/rpc/2.0/ai_custom/v1/segmentation/cutword'
    request_url = request_url + '?access_token=' + access_token

    # print('post_body_params', post_body_params)
    data = bytes(post_body_params.encode('utf-8'))

    request = urllib.request.Request(url=request_url, data=data)
    request.add_header('Content-Type', 'application/json')

    response = urllib.request.urlopen(request)
    content = response.read()
    results = json.loads(content)['results']

    return results

def save_seg_result_to_image(ori_image_path, seg_result):
    ori_img = cv2.imread(ori_image_path).astype(np.float32)
    height, width = ori_img.shape[:2]

    alpha = 0.5
    for item in seg_result:
        # Draw bbox
        x1 = int(item['location']['left'])
        y1 = int(item['location']['top'])
        w = int(item['location']['width'])
        h = int(item['location']['height'])
        x2 = x1 + w
        y2 = y1 + h

        cv2.rectangle(ori_img, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(ori_img, '{} score: {}'.format(item['name'], round(float(item['score']), 4)), (x1, y1 - 10),
                    cv2.FONT_HERSHEY_PLAIN, 0.7, (255, 255, 255), 1)

        # Draw mask
        rle_obj = {'counts': item['mask'],
                   'size': [height, width]}
        mask = mask_util.decode(rle_obj)

        new_rle_obj = mask_util.encode(mask)

        random_color = np.array([np.random.random() * 255.0,
                                 np.random.random() * 255.0,
                                 np.random.random() * 255.0])

        idx = np.nonzero(mask)

        ori_img[idx[0], idx[1], :] *= 1.0 - alpha
        ori_img[idx[0], idx[1], :] += alpha * random_color

    ori_img = ori_img.astype(np.uint8)
    # cv2.imshow('img', ori_img)
    dest_img = './result/final' + random_str(5) + '.png'
    cv2.imwrite(dest_img, ori_img, [int(cv2.IMWRITE_PNG_COMPRESSION), 5])
    print('final segmentation result writted to file: ', dest_img)

client_api_key = '你的API KEY'
client_secret_key = '你的Secret Key'
baidu_ai_host = ('https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id='
                + client_api_key + '&client_secret=' + client_secret_key)
ori_image_path = '你的图像文件'

access_token = get_access_token(baidu_ai_host)
print('start to segment image file: ', ori_image_path)
seg_result = do_segementation(access_token, get_image_json(ori_image_path))
save_seg_result_to_image(ori_image_path, seg_result)

