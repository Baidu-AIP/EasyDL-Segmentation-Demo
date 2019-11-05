const http = require('http');
const fs = require('fs');
const path = require('path');
const https = require('https');
const qs = require('querystring');
const settings = require('./settings.json');
const callCloudAPI = require('./callCloudAPI');

const homePage = fs.readFileSync(path.join(__dirname, 'index.html'), 'utf8');

/**
 * 将 stream 中的 data 提取出来，并用 JSON 解析
 * @param {import('stream').Readable} message
 *
 * @return {Promise<any>}
 */
function collectJSONBody(message) {
    return new Promise(resolve => {
        let body = '';

        message.on('data', chunk => (body += chunk.toString()));
        message.on('end', () => {
            resolve(body ? JSON.parse(body) : undefined);
        });
    });
}

/**
 * 使用 ak/sk 交换获得 token
 *
 * @return {Promise<string>}
 */
function getAccessToken() {
    const param = qs.stringify({
        grant_type: 'client_credentials',
        client_id: settings.ak,
        client_secret: settings.sk
    });

    return new Promise(resolve => {
        https.get(
            {
                hostname: 'aip.baidubce.com',
                path: '/oauth/2.0/token?' + param,
                agent: false
            },
            res => {
                collectJSONBody(res).then(remote => resolve(remote.access_token));
            }
        );
    });
}

/**
 * 调用 EasyDL 的 SDK，获取 infer 结果
 *
 * @param {{image: string; token: string; resolution: {width: number; height: number}}} payload
 */
function callAPI(payload) {
    return callCloudAPI({
        url: settings.api,
        resolution: payload.resolution,
        image: payload.image,
        token: payload.token
    });
}

// helper：组装 request 和 response
/**
 * 组装 Context，便于 server 处理
 *
 * @param {import('http').IncomingMessage} request
 * @param {import('http').ServerResponse} response
 *
 * @return {Promise<{path: string | undefined; body: any; response: import('http').ServerResponse}>}
 */
function assembleContext(request, response) {
    return new Promise(resolve => {
        collectJSONBody(request).then(body => {
            resolve({
                path: request.url,
                body,
                response
            });
        });
    });
}

/**
 * 可以视为 Route
 *
 * @param {{body: any, path: undefined | string; response: import('http').ServerResponse}} ctx
 *
 * @return {void}
 */
function handleRoute(ctx) {
    const response = ctx.response;

    switch (ctx.path) {
        case '/api':
            response.writeHead(200, {'Content-Type': 'text/json'});
            getAccessToken()
                .then(token => callAPI({image: ctx.body.image, token, resolution: ctx.body.resolution}))
                .then(remoteBody => {
                    response.write(JSON.stringify(remoteBody));
                    response.end();
                });
            break;
        default:
            response.writeHead(200, {'Content-Type': 'text/html'});
            response.write(homePage);
            response.end();
    }
}

const server = http.createServer(function(request, response) {
    return assembleContext(request, response).then(handleRoute);
});

const port = process.env.PORT || 7000;
server.listen(port);

console.info(`Demo 启动成功，请访问：http://127.0.0.1:${port}`);
