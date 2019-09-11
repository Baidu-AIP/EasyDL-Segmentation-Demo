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
#pragma once

typedef unsigned int uint;
typedef unsigned long siz;
typedef unsigned char byte;
typedef double* BB;
typedef struct { siz h, w, m; uint *cnts; } RLE;

/* Initialize/destroy RLE. */
void rleInit( RLE *R, siz h, siz w, siz m, uint *cnts );
void rleFree( RLE *R );

/* Initialize/destroy RLE array. */
void rlesInit( RLE **R, siz n );
void rlesFree( RLE **R, siz n );

/* Encode binary masks using RLE. */
void rleEncode( RLE *R, const byte *mask, siz h, siz w, siz n );

/* Decode binary masks encoded via RLE. */
void rleDecode( const RLE *R, byte *mask, siz n );

/* Compute union or intersection of encoded masks. */
void rleMerge( const RLE *R, RLE *M, siz n, int intersect );

/* Compute area of encoded masks. */
void rleArea( const RLE *R, siz n, uint *a );

/* Compute intersection over union between masks. */
void rleIou( RLE *dt, RLE *gt, siz m, siz n, byte *iscrowd, double *o );

/* Compute non-maximum suppression between bounding masks */
void rleNms( RLE *dt, siz n, uint *keep, double thr );

/* Compute intersection over union between bounding boxes. */
void bbIou( BB dt, BB gt, siz m, siz n, byte *iscrowd, double *o );

/* Compute non-maximum suppression between bounding boxes */
void bbNms( BB dt, siz n, uint *keep, double thr );

/* Get bounding boxes surrounding encoded masks. */
void rleToBbox( const RLE *R, BB bb, siz n );

/* Convert bounding boxes to encoded masks. */
void rleFrBbox( RLE *R, const BB bb, siz h, siz w, siz n );

/* Convert polygon to encoded mask. */
void rleFrPoly( RLE *R, const double *xy, siz k, siz h, siz w );

/* Get compressed string representation of encoded mask. */
char* rleToString( const RLE *R );

/* Convert from compressed string representation of encoded mask. */
void rleFrString( RLE *R, char *s, siz h, siz w );
