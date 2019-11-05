/**
 * 本文件展示了解码的完整过程，关于 RLE 和变种后的 LEB128，详细参考：
 * https://en.wikipedia.org/wiki/LEB128
 * https://en.wikipedia.org/wiki/Run-length_encoding
 */

/**
 * @typedef {number[][]} DecodedMask
 */

/**
 * 将一维数组转换成二维数组
 * @param {number} h 图像高度
 * @param {number[]} arr 一维数组
 *
 * @returns {DecodedMask} 大小为 h * w 的二维数组
 */
function untransform(h, arr) {
    /** @type {number[][]} */
    const memo = [];

    for (let i = 0; i < arr.length; i += h) {
        for (let j = 0; j < h; j++) {
            if (!memo[j]) {
                memo[j] = [];
            }
            memo[j].push(arr[i + j]);
        }
    }

    return memo;
}

/**
 * 解码 rle 格式成一维数组
 * @param {number[]} arr rle 编码后的 array
 *
 * @return {(0 | 1)[]} 和图像像素点数量相等的 0/1 数组
 */
function unrle(arr) {
    const memo = [];
    let j;
    let k;
    let v = false;

    for (j = 0; j < arr.length; j++) {
        for (k = 0; k < arr[j]; k++) {
            memo.push(v ? 1 : 0);
        }
        v = !v;
    }

    return memo;
}

/**
 * 和 LEB128 很像，但是使用 6bit，ascii 48-111
 * @param {string} s leb128 变种编码后的字符串
 *
 * @return {number[]}
 */
function unleb(s) {
    let m = 0;
    let p = 0;

    /** @type {number} */
    let k;
    /** @type {number} */
    let x;
    let more = false;
    const memo = [];

    m = 0;

    while (s[p]) {
        x = 0;
        k = 0;
        more = true;
        while (more) {
            const c = s[p].charCodeAt(0) - 48;
            x |= (c & 0x1f) << (5 * k);
            more = !!(c & 0x20);
            p++;
            k++;
            if (!more && c & 0x10) {
                x |= -1 << (5 * k);
            }
        }
        if (m > 2) {
            x += memo[m - 2];
        }
        memo[m++] = x;
    }

    return memo;
}

/**
 * helper function 将 a(b(c(d))) 转换成 pipe(d, c, b, a)
 */
function pipe(initialValue, ...fns) {
    return fns.reduce((memo, fn) => fn(memo), initialValue);
}

/**
 * 编码过程如下：
 * 0. 我们将 mask 以同等像素大小的二进制数组来表示，1 代表该像素点被标注，0 代表未标注
 * 1. 将二进制 0/1 的二维数组（height * width）通过尾部拼接成一维
 * 2. 将 1 中得到的一维数组进行 rle 编码，压缩大小。生成压缩后的一维数组
 * 3. 将 2 中得到的一维数组进行 leb128 编码，生成字符串减少内存占用
 *
 * 整个解码过程就是以上的逆过程
 */
function decodeMask(encodedStr, h) {
    return pipe(
        encodedStr,
        unleb,
        unrle,
        untransform.bind(undefined, h)
    );
}

module.exports = decodeMask;
