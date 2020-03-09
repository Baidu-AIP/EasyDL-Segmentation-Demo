/**************************************************************************
* Microsoft COCO Toolbox.      version 2.0
* Data, paper, and tutorials available at:  http://mscoco.org/
* Code written by Piotr Dollar and Tsung-Yi Lin, 2015.
Copyright (c) 2014, Piotr Dollar and Tsung-Yi Lin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.

**************************************************************************/

using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json.Linq;

namespace Baidu.Aip.EasyDL
{
    public class RLEObject{
        public int height;
        public int width;
        public int m;
        public int[] cnts;

    }
    public class MaskUitls
    {
        public RLEObject rle_obj = new RLEObject();
        public MaskUitls(string mask_str, int height, int width)
        {
            int m=0, p=0, k; 
            int x; 
            int more; 
            m = mask_str.Length;
            int[] cnts = new int[m];
            m=0;
            while( p < mask_str.Length ) {
                x=0; k=0; more=1;
                while( more != 0 ) {
                char c = (char)((int)mask_str[p] - 48); 
                x |= (c & 0x1f) << 5*k;
                more = c & 0x20; 
                p++; 
                k++;
                if((more == 0) && ((c & 0x10) != 0)) x |= -1 << 5*k;
                }
                if(m>2) x += (int) cnts[m-2]; 
                cnts[m++]=(int) x;
            }
            rleInit(height, width, m, cnts);
        }

        public void rleInit(int h, int w, int m, int[] cnts) {
            rle_obj.height = h; 
            rle_obj.width = w; 
            rle_obj.m = m;
            if(m == 0){
                rle_obj.cnts = null;
            }
            else{
                rle_obj.cnts = new int[m];
            }
            int j; 
            if(cnts != null){
                for(j=0; j<m; j++) {
                    rle_obj.cnts[j] = cnts[j];
                }
            } 
        }

        public void rleDecode(bool[] M, int n ) {
            int j, k; 
            bool v = false; 
            int idx = 0;
            for( j=0; j< rle_obj.m; j++ ) {
                for( k = 0; k < rle_obj.cnts[j]; k++ ) {
                    M[idx] = v;
                    idx++;      
                }
                v=!v; 
            }
        }
    }
}