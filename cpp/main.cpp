#include <string>
#include <typeinfo>
#include <time.h>
#include <assert.h>

extern "C"{
  #include "mask_utils/maskApi.h"
  }
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "aip/base/base.h"
#include "aip/base/base64.h"
#include "aip/base/http.h"
#include "aip/base/utils.h"
#include "aip/easydl.h"


int main(int argc, char** argv){
  // set parameters
  std::string app_id = ""; // app id
  std::string ak(""); // api key
  std::string sk(""); // secret key
  std::string url(""); // url
  const char* input_img_path = argv[1];
  std::string output_img_path(argv[2]);
  
  aip::EasyDL * client = new aip::EasyDL(app_id, ak, sk);
  std::string file_content;
  aip::get_file_content(input_img_path, &file_content);
  Json::Value result = client->easydl_request_image(url, file_content, aip::null);
  const Json::Value results = result["results"];

  cv::Mat img = cv::imread(input_img_path);
  std::cout<<img.channels()<<std::endl;
  int height = img.rows;
  int width = img.cols;
  srand(time(NULL));
  for (unsigned index = 0; index < results.size(); ++index ){
    // Generate random color
    int r = rand(); 
    std::cout<<"r:"<<r<<std::endl; 
    cv::RNG rng(r);
    cv::Scalar random_color = cv::Scalar(rng.uniform(0,255), rng.uniform(0, 255), rng.uniform(0, 255));
    std::cout<< random_color << std::endl;

    // Draw rectangle
    int x1 = int(results[index]["location"]["left"].asInt());
    int y1 = int(results[index]["location"]["top"].asInt());
    int w = int(results[index]["location"]["width"].asInt());
    int h = int(results[index]["location"]["height"].asInt());
    int x2 = x1 + w;
    int y2 = y1 + h;
    cv::Point p1(x1, y1);
    cv::Point p2(x2, y2);
    cv::rectangle(img, p1, p2, random_color);

    // Put text
    float score = results[index]["score"].asFloat();
    std::string label = results[index]["name"].asString();
    char buffer[50];
    int n = sprintf(buffer, "%s score: %.4f", label.c_str(), score);
    const std::string put_text = buffer;
    cv::Point p_org(x1, y1 - 10);
    cv::putText(img, put_text, p_org, cv::FONT_HERSHEY_PLAIN, 0.7, random_color, 1);

    // Draw mask
    std::string mask_string = results[index]["mask"].asString();

    char* mask_str = strdup(mask_string.c_str());
    RLE rle_obj;
    rleFrString(&rle_obj, mask_str, height, width);
    
    // Check if encoded str equals originral str
    char* encoded_str = rleToString(&rle_obj);

    // Get image mask
    unsigned char mask[height * width];
    rleDecode(&rle_obj, mask, 1);
   
    // check mask sum
    int cnt=0;
    for(int i=0; i< height*width; i++){
      if(mask[i] != 0)cnt++;
    }
    std::cout<<cnt<<std::endl;
    
    // plot image with mask and score
    for (int col = 0; col < width; col++){
      for (int row = 0; row < height; row++){
        // When mask is not equal 0, plot this point.
        if (mask[col * height + row] != 0){
          cv::Vec3b color = img.at<cv::Vec3b>(cv::Point(col,row));
          color[0] = char(int(float(color[0]) * 0.5 + float(random_color[0])* 0.5));
          color[1] = char(int(float(color[1]) * 0.5 + float(random_color[1])* 0.5));
          color[2] = char(int(float(color[2]) * 0.5 + float(random_color[2])* 0.5));
          img.at<cv::Vec3b>(cv::Point(col,row)) = color;
        }
      }
    }
  }
  cv::imwrite(output_img_path, img);
  std::cout << "Output image saved successfully!" << std::endl;
  return 0;
}  
