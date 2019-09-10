# EasyDL 图像分割 c++ sdk

## Prerequisites
### ubuntu 16.04
### 安装依赖库curl(需要支持ssl) 
### openssl ()
### jsoncpp(>1.6.2版本，0.x版本将不被支持 https://github.com/open-source-parsers/jsoncpp)

## Usage
修改main.cpp中的app_id, ak(api key), sk(secret key), url值，将其替换为自己的key。

然后编译：
```make```

生成的./main文件，入参为input_image_path和output_image_path，分别为输入图片地址和保存图片地址。

## Demo图
输入图片：

![Demo](assets/hou1.jpg)

输出图片：

![Demo](assets/hou1_results.jpg)

