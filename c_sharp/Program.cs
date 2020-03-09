using System;
using System.IO;

using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
// using runtime.osx.10.10-x64.CoreCompat.System.Drawing;

namespace c_sharp
{
    class Program
    {
        static void Main(string[] args)
        {
            // 设置APPID/AK/SK
            var APP_ID = "";
            var API_KEY = "";
            var SECRET_KEY = "";
            var URL = "";
            var client = new Baidu.Aip.EasyDL.EasyDL(APP_ID,  API_KEY, SECRET_KEY);
           
            // 使用ImgaeSharp 读取图片
            var image = Image.Load<Rgba32>("hou1.jpg");

            // 使用系统库读取图片binary
            var image_bytes = File.ReadAllBytes("hou1.jpg");
           
            // 使用图片binary调用分割接口，可能会抛出网络等异常，请使用try/catch捕获
            var result = client.requestImage(URL, image_bytes);
            
            // 获取结果
            //Console.WriteLine(result);
            var results = result["results"];

            // 原始图片宽和高
            int height = image.Height;
            int width = image.Width;

            foreach(var val in results){

                // 获取返回结果中mask字符串
                string mask_str = (string)val["mask"];

                // mask是一个和原图大小相同的数组，true位置表示此像素有效，false表示无效
                bool[] mask = new bool[height * width];
                
                // 利用mask字符串和宽高进行解码
                var obj = new Baidu.Aip.EasyDL.MaskUitls(mask_str, height, width);
                obj.rleDecode(mask, 1);
               
               // 可视化结果，mask中为true的位置打上红色
                for(int i = 0; i < width; i++){
                    for(int j = 0; j < height; j++){
                        if(mask[i * height + j] == true){
                            image[i, j] = Rgba32.Red;
                        }
                    }
                }
            }
            image.Save("output.jpg"); 
        }
    }
}



