const https = require('https');
const url = require('url');
const decodeMask = require('./decodeMask');

/**
 * @typedef {string} Base64
 * @typedef {string} AccessToken
 * @typedef {{width: number; height: number}} Resolution
 */

/**
 * @typedef  Detection
 * @property {string} name
 * @property {number} score
 * @property {import('./decodeMask').DecodedMask} mask
 * @property {{left: number; top: number; width: number; height: number}} location
 */

/**
 * @typedef {{log_id: number; results: Detection[]}} Success
 * @typedef {{error_code: number; error_msg: string}} Failure
 */

//
/**
 * 要去掉 base64 的头部 data:image/.*;base64,
 * @param {string} src
 */
function trimBase64Head(src) {
    const chunks = src.split(',');
    return chunks.length > 1 ? chunks[1] : src;
}

/**
 * 调用 EasyDL API，获取识别结果
 * @param {{url: string; image: Base64; token: AccessToken, resolution: Resolution}} payload
 * @return {Promise<Success | Failure>}
 */
function callCloudAPI(payload) {
    const urlParts = url.parse(payload.url);
    const image = trimBase64Head(payload.image);

    const data = JSON.stringify({image});
    const options = {
        hostname: urlParts.hostname,
        port: urlParts.protocol === 'https:' ? 443 : 80,
        path: `${urlParts.path}?access_token=${payload.token}`,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': `${data.length}`
        }
    };

    return new Promise(resolve => {
        /**
         * @param {import('http').IncomingMessage} res
         */
        function collectResponse(res) {
            let body = '';

            res.on('data', chunk => (body += chunk.toString()));
            res.on('end', () => {
                /** */
                const remote = body ? JSON.parse(body) : undefined;
                if (remote && 'results' in remote) {
                    remote.results.forEach(r => {
                        r.mask = decodeMask(r.mask, payload.resolution.height);
                    });
                }

                resolve(remote);
            });
        }

        const req = https.request(options, collectResponse);

        req.write(data);
        req.end();
    });
}

module.exports = callCloudAPI;
